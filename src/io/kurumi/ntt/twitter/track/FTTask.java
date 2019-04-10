package io.kurumi.ntt.twitter.track;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import twitter4j.*;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.SData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.twitter.TApi;
import io.kurumi.ntt.twitter.TAuth;
import io.kurumi.ntt.twitter.archive.UserArchive;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.Html;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import io.kurumi.ntt.Env;

public class FTTask extends TimerTask {

    static FTTask INSTANCE = new FTTask();

    public static JSONObject enable = SData.getJSON("data","track",true);

	static HashMap<Long,LinkedList<Long>> frIndex = new HashMap<>();
    static HashMap<Long,LinkedList<Long>> flIndex = new HashMap<>();

	static HashMap<Long,LinkedList<Long>> frSubIndex = new HashMap<>();
    static HashMap<Long,LinkedList<Long>> flSubIndex = new HashMap<>();
    static HashMap<Long,LinkedList<Long>> frSubIndexC = new HashMap<>();
    static HashMap<Long,LinkedList<Long>> flSubIndexC  = new HashMap<>();

    static Timer timer;

    public static void start() {

        stop();

        timer = new Timer("NTT Twitter Track Task");
        timer.schedule(INSTANCE,new Date(),9 * 60 * 1000);

    }

    public static void stop() {

        if (timer != null) timer.cancel();

    }

    public static void save() {

        SData.setJSON("data","track",enable);

    }

    @Override
    public void run() {

		synchronized (INSTANCE) {

            frSubIndex = frSubIndexC;
            frSubIndexC = new HashMap<>();

            flSubIndex = flSubIndexC;
            flSubIndexC = new HashMap<>();

            synchronized (UTTask.pedding) {

               new Send(Env.DEVELOPER_ID,"pedding : " + pedding.size()).exec();
                
                UTTask.pedding.addAll(pedding);
                pedding.clear();

			}

		}

        for (Map.Entry<String,Object> entry : enable.entrySet()) {

            long userId = Long.parseLong(entry.getKey());

            if (!(boolean)entry.getValue()) continue;

            startUserStackAsync(userId);

        }

    }

    LinkedHashSet<Long> pedding = new LinkedHashSet<>();

    ExecutorService userTrackPool = Executors.newFixedThreadPool(3);

    private void startUserStackAsync(final long userId) {

        userTrackPool.execute(new Runnable() {

                @Override
                public void run() {

                    startUserStack(userId);

                }

            });



    }


    void startUserStack(Long userId) {

        try {

            if (!TAuth.avilable(userId)) {

                enable.remove(userId.toString());

                save();

                return;

            }

            Twitter api = TAuth.get(userId).createApi();

            User me = api.verifyCredentials();

            if (me == null) return;

            BotDB.saveUser(me);

            LinkedList<Long> flLast = flIndex.containsKey(api.getId()) ? flIndex.get(api.getId()) : null;

            LinkedList<Long> flLatest = TApi.getAllFoIDs(api,api.getId());

            flIndex.put(api.getId(),flLatest);

			synchronized (INSTANCE) {

                for (long id : flLatest) {

                    LinkedList<Long> subIndex = flSubIndexC.get(id);

                    if (subIndex == null) {

                        subIndex = new LinkedList<>();

                    }

                    subIndex.add(userId);

                    flSubIndexC.put(id,subIndex);

                    if (flLast != null) {

                        if (!flLast.remove(id)) {

                            newFollower(userId,api,id);

                        }

                    }

                }

			}

            if (flLast != null && flLast.size() > 0) {

                for (int index = 0;index < flLast.size();index ++) {

                    long id = flLast.get(index);

                    lostFollower(userId,api,id);

                }

            }

			LinkedList<Long> allFr = TApi.getAllFrIDs(api,api.getId());

			frIndex.put(api.getId(),allFr);

            for (long id : allFr) {

                LinkedList<Long> subIndex = frSubIndexC.get(id);

                if (subIndex == null) {

                    subIndex = new LinkedList<>();

                }

                subIndex.add(userId);

                frSubIndexC.put(id,subIndex);

            }

            pedding.addAll(allFr);
            pedding.addAll(flLatest);

			/* 

             if (pedding.size() > 10000) {

             pedding = pedding.subList(0,10000);

             }

             while (pedding.size() > 0) {

             List<Long> target;

             if (pedding.size() > 100) {

             target = pedding.subList(0,100);

             pedding = pedding.subList(99,pedding.size());

             } else {

             target = pedding;

             pedding.clear();

             }

             ResponseList<User> result = api.lookupUsers(ArrayUtil.unWrap(target.toArray(new Long[target.size()])));

             for (User tuser : result) UserArchive.saveCache(tuser);

             }

			 */

        } catch (TwitterException e) {

            if (e.getErrorCode() != 130) {

                BotLog.error("UserArchive ERROR",e);

            }

        }

    }

    String link = Html.a("姬生平","https://esu.wiki/姬生平");

    HashMap<Long,LinkedList<Long>> userBlock;
    HashMap<Long,LinkedList<Long>> userMute;


    String parseStatus(Twitter api,User user) {

        StringBuilder status = new StringBuilder();

        try {

            if (!api.showFriendship(api.getId(),user.getId()).isSourceFollowingTarget() && !user.isFollowRequestSent()) {

                if (user.isProtected()) status.append("这是一个是锁推用户 :)\n");

            }

        } catch (TwitterException e) {}

        // if (user.isFollowRequestSent()) status.append("乃发送了关注请求 :)\n");
        if (user.getStatusesCount() == 0) status.append("这个用户没有发过推 :)\n");
        if (user.getFavouritesCount() == 0) status.append("这个用户没有喜欢过推文 :)\n");
        if (user.getFollowersCount() < 20) status.append("这个用户关注者低 (").append(user.getFollowersCount()).append(")  :)\n");

        /*

         try {

         Relationship ship = api.showFriendship(user.getId(),917716145121009664L);

         if (ship.isTargetFollowingSource() && ship.isTargetFollowedBySource()) {

         status.append("这个用户与 ").append(link).append(" 互相关注 是萌萌的二次元 :)\n");

         } else if (ship.isSourceFollowingTarget()) {

         status.append("这个用户关注了 ").append(link).append(" :)\n");

         } else if (ship.isSourceFollowedByTarget()) {

         status.append("这个用户被 ").append(link).append(" 关注 是萌萌的二次元 :)\n");

         }

         } catch (TwitterException e) {}

         */

        String statusR = status.toString();

        if (statusR.endsWith("\n")) {

            statusR.substring(0,statusR.length() - 1);

        }

        return statusR;

    }

    void newFollower(Long userId,Twitter api,long id) {

        try {

            User follower = api.showUser(id);

            BotDB.saveUser(follower);

            Relationship ship = api.showFriendship(api.getId(),id);

            new Send(userId,(ship.isSourceFollowingTarget() ? "乃关注的 " : "") + TApi.formatUserNameHtml(follower) + " 关注了你 :)",parseStatus(api,follower)).enableLinkPreview().html().exec();

        } catch (TwitterException e) {

            if (BotDB.userExists(id)) {

                new Send(userId,BotDB.getUser(id).urlHtml() + " 关注你 , 但是该账号已经不存在了 :(").enableLinkPreview().html().exec();

            } else {

                new Send(userId,"用户 (" + id + ") 关注了你 , 但是该账号已经不存在了 :(").enableLinkPreview().html().exec();


            }

        }

    }

    void lostFollower(Long userId,Twitter api,long id) {

        try {

            User follower = api.showUser(id);
            BotDB.saveUser(follower);

            Relationship ship = api.showFriendship(api.getId(),id);

            if (ship.isSourceBlockingTarget()) {

                new Send(userId,TApi.formatUserNameHtml(follower) + " 取关并屏蔽了你 :)").enableLinkPreview().html().exec();

            } else if (follower.getFriendsCount() == 0) {

                new Send(userId,TApi.formatUserNameHtml(follower) + " 取关了你，但对方关注人数为空，可能是账号异常 :)").enableLinkPreview().html().exec();

            } else {

                new Send(userId,TApi.formatUserNameHtml(follower) + " 取关了你 :)").enableLinkPreview().html().exec();

            }

        } catch (TwitterException e) {

            if (BotDB.userExists(id)) {

                new Send(userId,BotDB.getUser(id).urlHtml() + " 取关了你 , 因为该账号已经不存在了 :(").enableLinkPreview().html().exec();

            } else {

                new Send(userId,"用户 (" + id + ") 取关了你 , 因为该账号已经不存在了 :(").enableLinkPreview().html().exec();

            }

        }



    }

}
