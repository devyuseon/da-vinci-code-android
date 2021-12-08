package com.hsu.davincicode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;
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

    private Boolean isDoReceiveRunning;

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

        isDoReceiveRunning = true;
        doReceive();
        ChatMsg cm = new ChatMsg(userName, "ROOMUSERLIST", roomId);
        networkUtils.sendChatMsg(cm);

        binding.tvCurUser.setText(String.format("💚 %s", userName));
        binding.tvWaitActTitle.setText(String.format("【%s】 대기실", roomName));

        // userList 리사이클러뷰
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

        // 게임 시작
        binding.btnGameStart.setOnClickListener(v -> {
            ChatMsg cm1 = new ChatMsg(userName, "GAMESTART", roomId);
            networkUtils.sendChatMsg(cm1);
        });

    }

    private void gameStart() {
        Intent gameIntent = new Intent(this, GameActivity.class);
        gameIntent.putExtra("roomId", roomId);
        gameIntent.putExtra("roomName", roomName);
        isDoReceiveRunning = false;
        startActivity(gameIntent);
        finish();
    }

    public void doReceive() {
        new Thread() {
            public void run() {
                while (isDoReceiveRunning) {
                    ChatMsg cm;
                    cm = networkUtils.readChatMsg();
                    if (cm != null)
                        Log.d("FromServer[WaitActivity]", String.format("code: %s / userName: %s / data: %s / list: %s", cm.code, cm.UserName, cm.data, cm.list.toString()));

                    handler.post(() -> {
                        if (cm.code.matches("ROOMUSERLIST")) { // 방 목록 수신 ( 내 요청일 경우에만 )
                            // 서버에서 전송을 두번 하므로 두번 반영 안되도록 설정
                            if (userList.size() == 0) {
                                userList.addAll(cm.list);
                                waitListAdapter.notifyDataSetChanged();
                            }
                        }
                        if (cm.code.matches("ROOMIN")) {
                            String[] data = cm.data.split("//");
                            if (data[1].trim().equals(roomId)) { // 내 방 roomIn 정보일때
                                userList.add(cm.UserName);
                                waitListAdapter.notifyDataSetChanged();
                                Snackbar.make(binding.getRoot(), cm.UserName + " 님이 입장하셨습니다.", Snackbar.LENGTH_LONG).show();
                            }
                        }
                        if (cm.code.matches("GAMESTART") && roomId.equals(cm.data.trim())) {
                            gameStart();
                        }
                    });
                }
            }
        }.start();
    }
}