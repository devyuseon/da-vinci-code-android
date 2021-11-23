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

        binding.tvCurUser.setText(String.format("접속중 : %s", userName));
        binding.tvWaitActTitle.setText(String.format("[%s] 대기실", roomName));

        waitListAdapter = new WaitListAdapter(userList);
        binding.recyclerViewWaitUserList.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewWaitUserList.setAdapter(waitListAdapter);
    }

    public void doReceive() {
        new Thread() {
            public void run() {
                while (true) {
                    ChatMsg cm;
                    cm = networkUtils.readChatMsg();
                    Log.d("From Server", String.format("code: %s / userName: %s / data: %s / list: %s", cm.code, cm.UserName, cm.data, cm.list.toString()));

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cm.code.matches("ROOMUSERLIST")) { // 방 목록 수신
                                //userList.add(cm.data);
                                //waitListAdapter.notifyItemInserted(userList.size());
                            }
                        }
                    });

                }
            }
        }.start();
    }
}