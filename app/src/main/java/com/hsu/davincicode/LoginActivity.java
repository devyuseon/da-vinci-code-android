package com.hsu.davincicode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.hsu.davincicode.databinding.ActivityLoginBinding;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    private UserInfoViewModel userInfoViewModel;

    public Socket socket;
    public ObjectInputStream ois;
    public ObjectOutputStream oos;

    private NetworkUtils networkUtils;
    private NetworkObj networkObj;

    final String ip_addr = "10.0.2.2"; // Emulator PC의 127.0.0.1
    final int port_no = 30000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        userInfoViewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);

        binding.btnLogin.setOnClickListener(v -> new Thread() {
            public void run() {
                String userName = binding.etName.getText().toString();
                try {
                    socket = new Socket(ip_addr, port_no);
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    ois = new ObjectInputStream(socket.getInputStream());
                    networkObj = new NetworkObj(socket, ois, oos);
                    if(userInfoViewModel != null) {
                        userInfoViewModel.init(networkObj, userName);
                    }
                    networkUtils = new NetworkUtils(networkObj);
                    ChatMsg obj = new ChatMsg(userName, "100", "Hello");
                    networkUtils.sendChatMsg(obj, networkObj);
                    startMainActivity();
                } catch (IOException e) {
                    Log.w("Login", e);
                }
            }
        }.start());
    }

    public void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }
}