package io.kurumi.ntt.bots;

import cn.hutool.json.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.bots.template.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.confs.*;
import io.kurumi.ntt.ui.request.*;

public abstract class UserBot {
    
    public UserData owner;
    
    public String name;

    public JSONObject data = new JSONObject();
    
    public ConfRoot root;
 
    public UserBot(UserData owner,String name) {
        
        this.owner = owner;
        this.name = name;
        
        BotControl.put(owner,this);
        
    }

    public abstract String type();
    public abstract void confs(ConfRoot confs);

    public abstract AbsResuest start(DataObject obj);
    
    public void interrupt() {
        
        BotControl.remove(owner,this);
        
    }
    
    public JSONObject toJSONObject() {
        
        return new JSONObject()
        .put("name",name)
        .put("type",type())
        .put("data",data);
        
    }
    
    public static <T extends UserBot> T create(String type,UserData userData,String name) {
        
        switch(type) {

                case SeeYouNextTimeBot.TYPE : return (T)new SeeYouNextTimeBot(userData,name);

        }
        
        return null;
        
    }
    
    public static <T extends UserBot> T fromJSONObject(UserData userData,JSONObject obj) {
       
        String name = obj.getStr("name");
 
        T bot = null;
        
        bot = create(obj.getStr("type"),userData,name);
        
        bot.data = obj.getJSONObject("data");
        
        return bot;
        
    }
   
}