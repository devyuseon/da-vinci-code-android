package com.hsu.davincicode;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
    RoomListAdapter roomListAdapter;

    private UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj;
    private NetworkUtils networkUtils;
    private String userName;

    private Handler handler; // 스레드에서 UI 작업하기 위한 핸들러

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityRoomListBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        userName = userInfo.getUserName();
        networkObj = userInfo.getNetworkObj();
        networkUtils = new NetworkUtils(networkObj);

        doReceive();
        handler = new Handler();
        networkUtils.sendChatMsg(new ChatMsg(userName, "300", "roomListRequest"));

        binding.tvCurUser.setText(String.format("접속중 : %s", userName));

        roomListAdapter = new RoomListAdapter(roomList);
        binding.recyclerViewRoomList.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewRoomList.setAdapter(roomListAdapter);

        binding.btnRoomlistLogout.setOnClickListener(v -> {
            networkUtils.logout();
            startLoginActivity();
            finish();
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

    // Server Message 수신
    public void doReceive() {
        new Thread() {
            public void run() {
                while (true) {
                    ChatMsg cm;
                    cm = networkUtils.readChatMsg();
                    Log.d("From Server", String.format("code: %s / userName: %s / data: %s / roomList: %s", cm.code, cm.UserName, cm.data, cm.roomListData));

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cm.code.equals("300")) { // 방 목록 수신
                                for (String roomInfo : cm.roomListData) {
                                    String[] data = roomInfo.split("//");
                                    roomList.add(new Room(data[0], data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3])));
                                    roomListAdapter.notifyItemInserted(roomList.size());
                                }
                            }
                            if (cm.code.equals("400")) { // 방 생성시
                                String[] data = cm.data.split("//");
                                Room newRoom = new Room(data[0], data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                                roomList.add(newRoom);
                                roomListAdapter.notifyItemInserted(roomList.size());

                                if (cm.UserName.equals(userName)) { // 내가 방 생성을 요청했을 경우 나는 참가
                                    joinRoom(newRoom);
                                }
                            }
                            if (cm.code.equals("500")) { // 방 참가시
                                String[] data = cm.data.split("//");
                                Room newRoom = new Room(data[0], data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                                int newCurCount = newRoom.getCurCount();
                                String roomId = data[1];
                                int newIndex = findRoomIndexById(roomId);
                                roomList.get(newIndex).setCurCount(newCurCount);
                                roomListAdapter.notifyItemChanged(newIndex);

                                if (cm.UserName.equals(userName)) { // 내가 방 참가 할 경우
                                    joinRoom(newRoom);
                                }
                            }
                        }
                    });

                }
            }
        }.start();
    }

    public int findRoomIndexById(String roomId) {
        int i;
        for (i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).getRoomId().equals(roomId)) break;
        }
        return i;
    }

    public void joinRoom(Room room) {
        userInfo.setMyRoom(room);
        Intent intent = new Intent(this, WaitActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("roomName", room.getRoomName());
        bundle.putString("roomId", room.getRoomId());
        intent.putExtras(bundle);
        startActivity(intent);
    }
    // 뒤로가기 금지
    @Override
    public void onBackPressed() {

    }
}