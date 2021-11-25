package com.hsu.davincicode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.hsu.davincicode.databinding.ActivityWaitBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaitActivity extends AppCompatActivity {
    private ActivityWaitBinding binding;

    private String roomId;
    private String roomName;

    private UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj = NetworkObj.getInstance();
    private NetworkUtils networkUtils;
    private String userName;

    private WaitListAdapter waitListAdapter;
    private ArrayList<String> userList = new ArrayList<>();

    private Handler handler; // 스레드에서 UI 작업하기 위한 핸들러

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
        networkUtils = new NetworkUtils(networkObj);

        doReceive();
        ChatMsg cm = new ChatMsg(userName, "ROOMUSERLIST", roomId);
        networkUtils.sendChatMsg(cm);

        binding.tvCurUser.setText(String.format("💚 %s", userName));
        binding.tvWaitActTitle.setText(String.format("【%s】 대기실", roomName));

        waitListAdapter = new WaitListAdapter(userList);
        binding.recyclerViewWaitUserList.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewWaitUserList.setAdapter(waitListAdapter);

        // 로그아웃 버튼
        binding.btnWaitLogout.setOnClickListener(v -> {
            networkUtils.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // 방 나가기 버튼
        binding.btnWaitRoomout.setOnClickListener(v -> {
            ChatMsg cm2 = new ChatMsg(userName, "ROOMOUT", roomId);
            networkUtils.sendChatMsg(cm2);
        });

    }

    public void doReceive() {
        new Thread() {
            public void run() {
                while (true) {
                    ChatMsg cm;
                    cm = networkUtils.readChatMsg();
                    if (cm != null)
                        Log.d("FromServer[WaitActivity]", String.format("code: %s / userName: %s / data: %s / list: %s", cm.code, cm.UserName, cm.data, cm.list.toString()));

                    handler.post(() -> {
                        if (cm.code.matches("ROOMUSERLIST")) { // 방 목록 수신
                            binding.textView3.setText(String.format("code: %s / userName: %s / data: %s / list: %s", cm.code, cm.UserName, cm.data, cm.list.toString()));
                        }
                    });
                }
            }
        }.start();
    }
}