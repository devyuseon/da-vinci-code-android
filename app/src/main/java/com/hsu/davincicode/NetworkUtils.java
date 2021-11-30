package com.hsu.davincicode;

import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class NetworkUtils implements Serializable {

    private UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj;

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
        new Thread() {
            public void run() {
                try {
                    networkObj.getOos().writeObject(cm.code);
                    networkObj.getOos().writeObject(cm.UserName);
                    networkObj.getOos().writeObject(cm.data);

                    Log.d("ToServer", String.format("code: %s / userName: %s / data: %s / list: %s", cm.code, cm.UserName, cm.data, cm.list.toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
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

    // 비동기처리. 백그라운드에서 동작함
    class ReadChatMsgTask extends AsyncTask<String, String, ChatMsg> {

        @Override
        protected ChatMsg doInBackground(String... strings) {
            ChatMsg cm = new ChatMsg("", "", "");
            try {
                cm.code = (String) networkObj.getOis().readObject();
                cm.UserName = (String) networkObj.getOis().readObject();
                cm.data = (String) networkObj.getOis().readObject();
                if (cm.code.matches("ROOMLIST") || cm.code.matches("ROOMUSERLIST")) {
                    cm.list.clear();
                    cm.list = (ArrayList<String>) networkObj.getOis().readObject();
                }
                if (cm.code.matches("READY")) {
                    cm.cards = (Map<String, Vector<Card>>) networkObj.getOis().readObject();
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

