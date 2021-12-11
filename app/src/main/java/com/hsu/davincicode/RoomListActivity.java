package com.hsu.davincicode;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.hsu.davincicode.databinding.ActivityRoomListBinding;

import java.io.IOException;
import java.util.ArrayList;

public class RoomListActivity extends AppCompatActivity {
    private ActivityRoomListBinding binding;
    ArrayList<Room> roomList = new ArrayList<>();
    RoomListAdapter roomListAdapter;

    private UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj = NetworkObj.getInstance();
    private NetworkUtils networkUtils;
    private String userName;

    private Handler handler; // 스레드에서 UI 작업하기 위한 핸들러

    private Thread th;
    private Boolean isDoReceiveRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityRoomListBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        userName = userInfo.getUserName();
        networkUtils = new NetworkUtils(networkObj);

        //doReceive(); // 서버에서 받는 스레드 실행
        isDoReceiveRunning = true;
        ReceiveMsgTask receiveMsgTask = new ReceiveMsgTask();
        receiveMsgTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        handler = new Handler();

        networkUtils.sendChatMsg(new ChatMsg(userName, "ROOMLIST", "roomListRequest"));

        binding.tvCurUser.setText(String.format("💚 %s", userName)); // 접속중인 유저 표시

        // 리사이클러뷰 세팅
        roomListAdapter = new RoomListAdapter(roomList);
        binding.recyclerViewRoomList.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewRoomList.setAdapter(roomListAdapter);

        // 로그아웃
        binding.btnRoomlistLogout.setOnClickListener(v -> {
            networkUtils.logout();
            startLoginActivity();
            finish();
        });

        // 방 생성하기 다이얼로그 실행
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
                ChatMsg obj = new ChatMsg(userName, "ROOMCREATE", msg);
                networkUtils.sendChatMsg(obj); // 서버로 msg 전송

                alertDialog.dismiss();
            } else {
                Snackbar.make(dialogView, "인원수는 2 이상 4 이하여야 합니다.", Snackbar.LENGTH_LONG).show();
            }

        });

        cancle_btn.setOnClickListener(v -> alertDialog.dismiss());
    }

    class ReceiveMsgTask extends AsyncTask<ChatMsg, String, Void> {


        @Override
        protected Void doInBackground(ChatMsg... strings) {

            while (isDoReceiveRunning) {
                ChatMsg cm = networkUtils.readChatMsg();

                if (!cm.code.isEmpty()) {
                    Log.d("FromServer[RoomListActivity]", String.format("code: %s / userName: %s / data: %s / list: %s", cm.code, cm.UserName, cm.data, cm.list));
                    publishProgress(cm);
                }
            }
            return null;
        }

        private void publishProgress(ChatMsg cm) {
            handler.post(() -> {
                if (cm.code.equals("ROOMLIST")) { // 방 목록 수신
                    for (String roomInfo : cm.list) {
                        String[] data = roomInfo.split("//");
                        roomList.add(new Room(data[0], data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3])));
                        roomListAdapter.notifyItemInserted(roomList.size());
                    }
                }
                if (cm.code.equals("ROOMCREATE")) { // 방 생성시 리사이클러뷰 갱신
                    String[] data = cm.data.split("//");
                    Room newRoom = new Room(data[0], data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                    roomList.add(newRoom);
                    roomListAdapter.notifyItemInserted(roomList.size());
                }
                if (cm.code.equals("ROOMIN")) { // 방 참가시 리사이클러뷰 갱신
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
            });
        }
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
        isDoReceiveRunning = false; // RoomListActivity의 Doreceive 스레드 종료
        networkUtils.sendChatMsgTask.cancel(false);
        startActivity(intent);
        finish();
    }

    // 뒤로가기 금지
    @Override
    public void onBackPressed() {

    }
}