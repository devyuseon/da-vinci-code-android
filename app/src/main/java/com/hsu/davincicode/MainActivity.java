package com.hsu.davincicode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.hsu.davincicode.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj;
    private NetworkUtils networkUtils;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        userName = userInfo.getUserName();
        networkObj = userInfo.getNetworkObj();
        networkUtils = new NetworkUtils(networkObj);

        binding.btnLogout.setOnClickListener(v -> {
            logOut();
        });

        binding.btnSend.setOnClickListener(v -> {
            sendMsgToServer();
        });
    }

    public void sendMsgToServer() {
        String msg = binding.etMsg.getText().toString();
        ChatMsg obj = new ChatMsg(userName, "200", msg);
        networkUtils.sendChatMsg(obj); // 서버로 msg 전송
    }

    public void logOut() {
        ChatMsg obj = new ChatMsg(userName, "400", "Bye");
        networkUtils.Logout(obj);
        startActivity(new Intent(this, LoginActivity.class)); // 다시 로그인 화면으로 돌아감
    }
}