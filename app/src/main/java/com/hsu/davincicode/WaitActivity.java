package com.hsu.davincicode;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.hsu.davincicode.databinding.ActivityWaitBinding;

public class WaitActivity extends AppCompatActivity {
    private ActivityWaitBinding binding;

    private String roomId;
    private String roomName;

    private UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj;
    private NetworkUtils networkUtils;
    private String userName;

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
        networkObj = userInfo.getNetworkObj();
        networkUtils = new NetworkUtils(networkObj);

        binding.tvCurUser.setText(String.format("접속중 : %s", userName));
        binding.tvWaitActTitle.setText(String.format("[%s] 대기실", roomName));
    }
}