package com.hsu.davincicode;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.hsu.davincicode.databinding.ActivityRoomListBinding;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RoomListActivity extends AppCompatActivity {
    private ActivityRoomListBinding binding;
    ArrayList<Room> roomList = new ArrayList<>();

    private UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj;
    private NetworkUtils networkUtils;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityRoomListBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        userName = userInfo.getUserName();
        networkObj = userInfo.getNetworkObj();
        networkUtils = new NetworkUtils(networkObj);

        binding.tvCurUser.setText(String.format("접속중 : %s", userName));

        roomList.add(new Room("room1", 1, 4));
        roomList.add(new Room("room2", 4, 4));

        RoomListAdapter roomListAdapter = new RoomListAdapter(roomList);
        binding.recyclerViewRoomList.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewRoomList.setAdapter(roomListAdapter);

        binding.btnRoomlistLogout.setOnClickListener(v -> {
            networkUtils.logout();
            startLoginActivity();
        });

        binding.btnRoomlistCreateRoom.setOnClickListener(v -> creatRoomDialog());
    }

    public void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void creatRoomDialog() {
        final String[] roomName = new String[1];
        final String[] roomPw = new String[1];
        final String[] roomCount = new String[1];

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_room, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button ok_btn = dialogView.findViewById(R.id.btn_dialog_room_create);
        Button cancle_btn = dialogView.findViewById(R.id.btn_dialog_dismiss);

        ok_btn.setOnClickListener(v -> {
            EditText name = dialogView.findViewById(R.id.et_dialog_roomname);
            EditText pw = dialogView.findViewById(R.id.et_dialog_roompw);
            EditText count = dialogView.findViewById(R.id.et_dialog_roomcount);

            roomName[0] = name.getText().toString();
            roomPw[0] = pw.getText().toString();
            roomCount[0] = count.getText().toString();

            if (Integer.parseInt(roomCount[0]) <= 4 && Integer.parseInt(roomCount[0]) > 1) {
                String msg = String.format("%s//%s//%s", roomName[0], roomCount[0], roomPw[0]);
                ChatMsg obj = new ChatMsg(userName, "400", msg);
                networkUtils.sendChatMsg(obj); // 서버로 msg 전송

                alertDialog.dismiss();
            } else {
                Snackbar.make(dialogView, "인원수는 2 이상 4 이하여야 합니다.", Snackbar.LENGTH_LONG).show();
            }

        });

        cancle_btn.setOnClickListener(v -> alertDialog.dismiss());
    }

    // 뒤로가기 금지
    @Override
    public void onBackPressed() {

    }
}