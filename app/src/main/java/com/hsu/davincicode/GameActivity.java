package com.hsu.davincicode;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;
import com.hsu.davincicode.databinding.ActivityGameBinding;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {
    private ActivityGameBinding binding;

    private String roomId;
    private String roomName;

    private UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj = NetworkObj.getInstance();
    private NetworkUtils networkUtils;
    private String userName;

    private ArrayList<String> userList = new ArrayList<>();

    private Handler handler; // 스레드에서 UI 작업하기 위한 핸들러

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        roomId = bundle.getString("roomId");
        roomName = bundle.getString("roomName");

        handler = new Handler();

        userName = userInfo.getUserName();
        networkUtils = new NetworkUtils(networkObj);

        doReceive();
        ChatMsg cm = new ChatMsg(userName, "ROOMUSERLIST", roomId);
        networkUtils.sendChatMsg(cm);
    }

    public void doReceive() {
        new Thread() {
            public void run() {
                while (true) {
                    ChatMsg cm;
                    cm = networkUtils.readChatMsg();
                    if (cm != null)
                        Log.d("FromServer[GameActivity]", String.format("code: %s / userName: %s / data: %s / list: %s", cm.code, cm.UserName, cm.data, cm.list.toString()));

                    handler.post(() -> {
                        if (cm.code.matches("ROOMUSERLIST") && cm.UserName.equals(userName)) { // 방 목록 수신 ( 내 요청일 경우에만 )

                        }
                    });
                }
            }
        }.start();
    }
}