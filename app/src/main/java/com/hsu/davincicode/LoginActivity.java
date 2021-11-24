package com.hsu.davincicode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hsu.davincicode.databinding.ActivityLoginBinding;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    public Socket socket;
    public ObjectInputStream ois;
    public ObjectOutputStream oos;

    private NetworkUtils networkUtils;
    private NetworkObj networkObj;
    private String userName = "";

    final String ip_addr = "10.0.2.2"; // Emulator PC의 127.0.0.1
    final int port_no = 30000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> {
            userName = binding.etName.getText().toString();
            new Thread() {
                public void run() {
                    try {
                        // 네트워크 관련 설정 ..
                        socket = new Socket(ip_addr, port_no);
                        oos = new ObjectOutputStream(socket.getOutputStream());
                        ois = new ObjectInputStream(socket.getInputStream());

                        // 싱글턴인 NetworkObj 인스턴스 초기화
                        NetworkObj networkObj = NetworkObj.getInstance();
                        networkObj.init(socket, ois, oos);

                        // 싱글턴인 UserInfo 인스턴스 초기화
                        UserInfo userInfo = UserInfo.getInstance();
                        userInfo.init(userName);

                        // login 정보 서버에 전달
                        ChatMsg obj = new ChatMsg(userName, "100", "Hello");

                        networkUtils = new NetworkUtils(networkObj);
                        networkUtils.sendChatMsg(obj);

                        //startMainActivity();
                        startRommListActivity();

                    } catch (IOException e) {
                        Log.w("Login", e);
                    }
                }
            }.start();
        });
    }

    public void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
    public void startRommListActivity() {
        startActivity(new Intent(this, RoomListActivity.class));
        finish();
    }
}