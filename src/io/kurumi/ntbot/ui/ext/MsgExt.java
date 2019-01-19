package io.kurumi.ntbot.ui.ext;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import java.util.*;

public class MsgExt {

    public static boolean isCommand(Message msg) {

        return msg.text().startsWith("/");

    }

    public static String getCommandName(Message msg) {

        if (!msg.text().contains("/")) return null;

        String body = StrUtil.subAfter(msg.text(), "/", false);

        if (body.contains(" ")) {

            return StrUtil.subBefore(body, " ", false);

        } else if (body.contains("@")) {

            return StrUtil.subBefore(body, "@", false);

        } else {

            return body;

        }

    }

    public static String[] NO_PARAMS = new String[0];

    public static String[] getCommandParms(Message msg) {

        if (!msg.text().contains("/")) return null;

        String body = StrUtil.subAfter(msg.text(), "/", false);

        if (body.contains(" ")) {

            return StrUtil.subAfter(body, " ", false).split(" ");

        } else if (body.contains("@")) {

            return ("@" + StrUtil.subAfter(body, "@", false)).split(" ");

        } else {

            return NO_PARAMS;

        }

    }

    public static void delete(Message msg) {
        
        Constants.bot.execute(new DeleteMessage(msg.chat().id(), msg.messageId()));

    }
    
    public static class Answer {
        
        
        
    }

    public static class Edit {

        private Message msg;
        private CallbackQuery query;

        private EditMessageText edit;

        public Edit(Message msg) {

            this(msg, null);

        }

        public Edit(Message msg, String text) {

            this.msg = msg;
            if (text == null) text = msg.text();

            edit = new EditMessageText(msg.chat().id(), msg.messageId(), text);

        }

        public Edit(CallbackQuery query, String text) {

            this.query = query;

            edit = new EditMessageText(query.inlineMessageId(), text);

        }


        public Edit html() {

            edit.parseMode(ParseMode.HTML);

            return this;

        }

        public Edit marldown() {

            edit.parseMode(ParseMode.Markdown);

            return this;

        }
        
        public Edit disableWebPagePreview() {

            edit.disableWebPagePreview(true);

            return this;

        }

        private LinkedList<InlineKeyboardButton> inlineKeyboardButtons = new LinkedList<>();

        public Edit inlineOpenUrlButton(String button, String url) {

            inlineKeyboardButtons.add(new InlineKeyboardButton(button).url(url));

            return this;

        }

        public Edit inlineCallbackButton(String button, String callbackData) {

            inlineKeyboardButtons.add(new InlineKeyboardButton(button).callbackData(callbackData));

            //    InlineCallback.registerListener(callbackData,listener);

            return this;

        }

        public void edit() {

            if (inlineKeyboardButtons.size() != 0) {

                edit.replyMarkup(new InlineKeyboardMarkup(inlineKeyboardButtons.toArray(new InlineKeyboardButton[inlineKeyboardButtons.size()])));

            }

            Constants.bot.execute(edit);



        }


    }

    public static class Send {

        private SendMessage send;

        public Send(Chat chat, String content) {

            send = new SendMessage(chat.id(), content);

        }

        public Send(Message msg, String content) {

            this(msg.chat(), content);

            replyToMessageId(msg.messageId());

        }

        private int keyboaordType = 0;

        public Send hideKeyboard() {

            keyboaordType = 1;

            return this;

        }

        public Send removeKeyboard() {

            keyboaordType = 2;

            return this;

        }


        public Send html() {

            send.parseMode(ParseMode.HTML);

            return this;

        }

        public Send markdown() {

            send.parseMode(ParseMode.Markdown);

            return this;

        }

        public Send disableWebPagePreview() {

            send.disableWebPagePreview(true);

            return this;

        }

        public Send replyToMessageId(int id) {

            send.replyToMessageId(id);

            return this;

        }

        private LinkedList<KeyboardButton> keyboardButtons = new LinkedList<>();

        public Send keyBoardButton(String button) {

            keyboaordType = 3;

            keyboardButtons.add(new KeyboardButton(button));

            return this;

        }

        public Send keyboardButtonWithContactRequest(String button) {

            keyboardButtons.add(new KeyboardButton(button).requestContact(true));

            return this;

        }

        public Send keyboardButtonWithLocationRequest(String button) {

            keyboardButtons.add(new KeyboardButton(button).requestLocation(true));

            return this;

        }

        private LinkedList<InlineKeyboardButton> inlineKeyboardButtons = new LinkedList<>();

        public Send inlineOpenUrlButton(String button, String url) {

            inlineKeyboardButtons.add(new InlineKeyboardButton(button).url(url));

            return this;

        }

        public Send inlineCallbackButton(String button, String callbackData) {

            inlineKeyboardButtons.add(new InlineKeyboardButton(button).callbackData(callbackData));

            //    InlineCallback.registerListener(callbackData,listener);

            return this;

        }

        public void send() {

            if (keyboardButtons.size() != 0) {

                send.replyMarkup(new ReplyKeyboardMarkup(keyboardButtons.toArray(new KeyboardButton[keyboardButtons.size()])));

            } else if (inlineKeyboardButtons.size() != 0) {

                send.replyMarkup(new InlineKeyboardMarkup(inlineKeyboardButtons.toArray(new InlineKeyboardButton[inlineKeyboardButtons.size()])));

            } else {

                switch (keyboaordType) {

                    case 1: send.replyMarkup(new ReplyKeyboardHide());break;
                    case 2: send.replyMarkup(new ReplyKeyboardRemove());break;

                }

            }

            Constants.bot.execute(send);

        }

    }

}