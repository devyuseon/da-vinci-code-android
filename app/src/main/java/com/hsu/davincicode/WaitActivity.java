package com.hsu.davincicode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.hsu.davincicode.databinding.ActivityWaitBinding;

import java.util.ArrayList;

public class WaitActivity extends AppCompatActivity {
    private ActivityWaitBinding binding;

    private String roomId;
    private String roomName;

    private UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj;
    private NetworkUtils networkUtils;
    private String userName;

    private Handler handler; // 스레드에서 UI 작업하기 위한 핸들러

    private UserListAdapter userListAdapter;
    private ArrayList<String> userList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityWaitBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        roomId = bundle.getString("roomId");
        roomName = bundle.getString("roomName");

        handler = new Handler();

        userName = userInfo.getUserName();
        networkObj = userInfo.getNetworkObj();
        networkUtils = new NetworkUtils(networkObj);

        userListAdapter = new UserListAdapter(userList);
        binding.recyclerviewUserlist.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerviewUserlist.setAdapter(userListAdapter);

        doReceive();

        networkUtils.sendChatMsg(new ChatMsg(userName, "ROOMUSERLIST", "roomuserlist"));

        binding.tvCurUser.setText(String.format("접속중 : %s", userName));
        binding.tvWaitActTitle.setText(String.format("[%s] 대기실", roomName));

    }

    // Server Message 수신
    public void doReceive() {
        new Thread() {
            public void run() {
                while (true) {
                    ChatMsg cm;
                    cm = networkUtils.readChatMsg();
                    Log.d("From Server", String.format("code: %s / userName: %s / data: %s / roomList: %s", cm.code, cm.UserName, cm.data, cm.arrayList));

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cm.code.equals("ROOMUSERLIST")) { // 유저 목록 수신
                                userList = cm.arrayList;
                            }

                        }
                    });

                }
            }
        }.start();
    }

}