package com.hsu.davincicode;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.hsu.davincicode.databinding.ActivityGameBinding;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {
    private ActivityGameBinding binding;

    private String roomId;
    private String roomName;

    private UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj = NetworkObj.getInstance();
    private NetworkUtils networkUtils;
    private String userName;

    private ArrayList<String> userList = new ArrayList<>();

    private Handler handler; // 스레드에서 UI 작업하기 위한 핸들러
    private Boolean isDoReceiveRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        if (getIntent().hasExtra("roodId") && getIntent().hasExtra("roomName")) {
            roomId = getIntent().getStringExtra("roomId");
            roomName = getIntent().getStringExtra("roomName");
        }

        handler = new Handler();

        userName = userInfo.getUserName();
        networkUtils = new NetworkUtils(networkObj);

        if (getIntent().hasExtra("roomId") && getIntent().hasExtra("roomName")) {
            roomId = getIntent().getStringExtra("roomId");
            roomName = getIntent().getStringExtra("roomName");
        }

        //doReceive();
        isDoReceiveRunning = true;
        ReceiveMsgTask receiveMsgTask = new ReceiveMsgTask();
        receiveMsgTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        handler = new Handler();

        ChatMsg cm = new ChatMsg(userName, "ROOMUSERLIST", roomId);
        networkUtils.sendChatMsg(cm);

        binding.btnReady.setOnClickListener(v -> {
            ChatMsg cm1 = new ChatMsg(userName, "READY", roomId);
            networkUtils.sendChatMsg(cm1);
        });
    }

    class ReceiveMsgTask extends AsyncTask<ChatMsg, String, Void> {


        @Override
        protected Void doInBackground(ChatMsg... strings) {

            while (isDoReceiveRunning) {
                ChatMsg cm = networkUtils.readChatMsg();

                if (!cm.code.isEmpty()) {
                    Log.d("FromServer[GameActivity]", String.format("code: %s / userName: %s / data: %s / list: %s / cards: %s", cm.code, cm.UserName, cm.data, cm.list.toString(), cm.cards.toString()));
                    publishProgress(cm);
                }
            }
            return null;
        }

        private void publishProgress(ChatMsg cm) {
            handler.post(() -> {
                if (cm.code.matches("ROOMUSERLIST") && cm.UserName.equals(userName)) {
                    int playerCount = cm.list.size() - 1; // 나를 제외한 플레이어 수
                    int i = 0;
                    TextView[] tvUserNames = {binding.tvPlayer1Name, binding.tvPlayer2Name, binding.tvPlayer3Name};
                    ImageView[] ivUserNames = {binding.ivPlayer1, binding.ivPlayer2, binding.ivPlayer3};
                    for (String name : cm.list) {
                        if(!name.equals(userName) && i < playerCount) {
                            tvUserNames[i].setText(name);
                            tvUserNames[i].setVisibility(View.VISIBLE);
                            ivUserNames[i].setVisibility(View.VISIBLE);
                            i++;
                        }
                    }
                }
                if (cm.code.matches("READY")) {
                    binding.btnReady.setVisibility(View.GONE);
                }
            });
        }
    }

}