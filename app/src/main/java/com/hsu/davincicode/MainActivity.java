package com.hsu.davincicode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.hsu.davincicode.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Handler handler; // 스레드에서 UI 작업하기 위한 핸들러

    private UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj;
    private NetworkUtils networkUtils;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        handler = new Handler();

        userName = userInfo.getUserName();
        networkObj = userInfo.getNetworkObj();
        networkUtils = new NetworkUtils(networkObj);

        binding.btnLogout.setOnClickListener(v -> {
            logOut();
        });

        binding.btnSend.setOnClickListener(v -> {
            sendMsgToServer();
            binding.etCode.setText("");
            binding.etData.setText("");
        });

        doReceive();
    }

    public void sendMsgToServer() {
        String code = binding.etCode.getText().toString();
        String msg = binding.etData.getText().toString();
        ChatMsg obj = new ChatMsg(userName, code, msg);
        networkUtils.sendChatMsg(obj); // 서버로 msg 전송
    }

    public void logOut() {
        networkUtils.logout();
        startActivity(new Intent(this, LoginActivity.class)); // 다시 로그인 화면으로 돌아감
    }

    // Server Message 수신
    public void doReceive() {
        new Thread() {
            public void run() {
                while (true) {
                    ChatMsg cm;
                    cm = networkUtils.readChatMsg();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            binding.tvReceivedMsg.setText(String.format("[%s] %s", cm.UserName, cm.data));
                        }
                    });
                }
            }
        }.start();
    }

}