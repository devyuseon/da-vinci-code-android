package com.hsu.davincicode;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class NetworkUtils implements Serializable {

    private UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj;
    boolean isCancel = false;
    SendChatMsgTask sendChatMsgTask;


    public NetworkUtils(NetworkObj networkObj) {
        this.networkObj = networkObj;
    }

    public void logout() {
        new Thread() {
            public void run() {
                try {
                    ChatMsg cm = new ChatMsg(userInfo.getUserName(), "LOGOUT", "bye");
                    sendChatMsg(cm);
                    networkObj.getOos().close();
                    networkObj.getOis().close();
                    networkObj.getSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void sendChatMsg(ChatMsg cm) {
        sendChatMsgTask = new SendChatMsgTask();
        sendChatMsgTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cm);
    }

    public ChatMsg readChatMsg() {
        ChatMsg cm = new ChatMsg("", "", "");

        ReadChatMsgTask readChatMsgTask = new ReadChatMsgTask();

        try {
            cm = readChatMsgTask.execute().get();
        } catch (ExecutionException | InterruptedException e) {
        }

        return cm;
    }

    class SendChatMsgTask extends AsyncTask<ChatMsg, String, Void> {

        private int count = 0;

        @Override
        protected Void doInBackground(ChatMsg... param) {
            if(isCancelled())
                return null;
            while (count == 0) {
                ChatMsg cm = param[0];
                try {
                    networkObj.getOos().writeObject(cm.code);
                    networkObj.getOos().writeObject(cm.UserName);
                    networkObj.getOos().writeObject(cm.data);
                    Log.d("ToServer", String.format("code: %s / userName: %s / data: %s / list: %s", cm.code, cm.UserName, cm.data, cm.list.toString()));
                    count++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            System.out.println("task cancelled!!!!!!!!!!");
        }
    }

    class ReadChatMsgTask extends AsyncTask<String, String, ChatMsg> {

        @Override
        protected ChatMsg doInBackground(String... strings) {
            ChatMsg cm = new ChatMsg("", "", "");
            try {
                cm.code = (String) networkObj.getOis().readObject();
                cm.UserName = (String) networkObj.getOis().readObject();
                cm.data = (String) networkObj.getOis().readObject();
                if (cm.code.matches("ROOMLIST") || cm.code.matches("ROOMUSERLIST") || cm.code.matches("READY") || cm.code.matches("GAMEOVER")) {
                    cm.list.clear();
                    cm.list = (ArrayList<String>) networkObj.getOis().readObject();
                }
            } catch (StreamCorruptedException e) {
                Log.w("ServerError", e);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                logout();
                Log.w("ServerError", e);
                e.printStackTrace();
            }
            return cm;
        }
    }

}

