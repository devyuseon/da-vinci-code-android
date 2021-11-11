package com.hsu.davincicode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import com.hsu.davincicode.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private NetworkObj networkObj;
    private NetworkUtils networkUtils;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        UserInfoViewModel userInfoViewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);
        networkObj = userInfoViewModel.getNetworkObj().getValue();
        networkUtils = new NetworkUtils(networkObj);
        userName = userInfoViewModel.getUserName().getValue();

        binding.btnLogout.setOnClickListener(v -> {
            ChatMsg obj = new ChatMsg(userName, "400", "Bye");
            networkUtils.Logout(obj);
        });

        /*binding.btnSend.setOnClickListener(v -> {
            String msg = binding.etMsg.getText().toString();
            ChatMsg obj = new ChatMsg(userName, "200", msg);
            networkUtils.sendChatMsg(obj, networkObj);
        });*/
    }
}