package com.hsu.davincicode;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hsu.davincicode.databinding.ActivityGameBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {
    private ActivityGameBinding binding;

    private String roomId;
    private String roomName;

    private UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj = NetworkObj.getInstance();
    private NetworkUtils networkUtils;
    private String userName;

    CardListAdapter myCardListAdapter;
    CardListAdapter player1CardListAdapter;
    private ArrayList<String> userList = new ArrayList<>();
    private ArrayList<Card> myCardList = new ArrayList<>();
    private int leftCardCount = 24;
    private Map<String, ArrayList<Card>> userCardList = new HashMap<String, ArrayList<Card>>();
    private Comparator<Card> sortCard;

    private CountDownTimer countDownTimer;

    private Handler handler; // 스레드에서 UI 작업하기 위한 핸들러
    private Boolean isDoReceiveRunning;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        binding.getRoot().setBackgroundColor(Color.parseColor("#808080"));

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

        // 내 카드 리사이클러뷰 세팅
        myCardListAdapter = new CardListAdapter(myCardList);
        binding.recyclerviewMycard.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.recyclerviewMycard.setAdapter(myCardListAdapter);

        //doReceive();
        isDoReceiveRunning = true;
        ReceiveMsgTask receiveMsgTask = new ReceiveMsgTask();
        receiveMsgTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        handler = new Handler();

        ChatMsg cm = new ChatMsg(userName, "ROOMUSERLIST", roomId);
        networkUtils.sendChatMsg(cm);

        sortCard = new Comparator<Card>() {
            @Override
            public int compare(Card o1, Card o2) {
                // o2 - o1 리턴하면 내림차순, -1 곱하면 오름차순

                if (o2.getCardNum() - o1.getCardNum() == 0) { // 수가 같을 경우에
                    return compareByColor(o1.getCardColor(), o2.getCardColor());
                } else {
                    return (o2.getCardNum() - o1.getCardNum()) * (-1);
                }

            }

            public int compareByColor(String o1Color, String o2Color) {
                if (o1Color.equals(o2Color)) return 0;
                if (o2Color.equals("w") && o1Color.equals("b")) return -1;
                else return 1;
            }

        };

        binding.btnReady.setOnClickListener(v -> {
            ChatMsg cm1 = new ChatMsg(userName, "READY", roomId);
            networkUtils.sendChatMsg(cm1);
        });
    }

    public void setUserRecyclerView(ArrayList<Card> cardList) {
        // 다른 유저 카드 리사이클러뷰 세팅 ,, 아직 두번째, 세번째 플레이어 고려 안함
        player1CardListAdapter = new CardListAdapter(cardList);
        binding.recyclerviewPlayer1.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.recyclerviewPlayer1.setAdapter(player1CardListAdapter);
    }

    public void setTimer(int time) {
        binding.tvTime.setVisibility(View.VISIBLE);

        // 1000*timems(time초)동안 1000ms(1초)마다 실행
        CountDownTimer countDownTimer = new CountDownTimer(time * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvTime.setText(String.valueOf((int) (millisUntilFinished / 1000)));
            }

            @Override
            public void onFinish() {
                binding.tvTime.setText("timeover");
            }
        };

        countDownTimer.start();
    }

    public void cancleTimer() {
        binding.tvTime.setVisibility(View.INVISIBLE);
        countDownTimer.cancel();
    }

    public void showTakeOrMatchDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_choose_take_or_match, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("당신의 턴입니다. 어떤 것을 선택하시겠습니까?")
                .setView(dialogView)
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        Button btn_take = dialogView.findViewById(R.id.btn_dialog_choose_take);
        Button btn_match = dialogView.findViewById(R.id.btn_dialog_choose_match);

        btn_take.setOnClickListener(v -> {
            ChatMsg cm = new ChatMsg(userName, "TAKECARD", roomId);
            networkUtils.sendChatMsg(cm);
            dialog.dismiss();
        });

        btn_match.setOnClickListener(v -> {
            setTimer(10);
            dialog.dismiss();
        });
    }

    class ReceiveMsgTask extends AsyncTask<ChatMsg, String, Void> {


        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(ChatMsg... strings) {

            while (isDoReceiveRunning) {
                ChatMsg cm = networkUtils.readChatMsg();

                if (!cm.code.isEmpty()) {
                    Log.d("FromServer[GameActivity]", String.format("code: %s / userName: %s / data: %s / list: %s", cm.code, cm.UserName, cm.data, cm.list.toString()));
                    publishProgress(cm);
                }
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        private void publishProgress(ChatMsg cm) {
            handler.post(() -> {
                int playerCount = cm.list.size() - 1; // 나를 제외한 플레이어 수

                if (cm.code.matches("ROOMUSERLIST") && cm.UserName.equals(userName)) {

                    /* 유저 프로필 이미지뷰, 이름 텍스트뷰 설정 */
                    userList = cm.list;
                    userList.remove(userName);
                    int i = 0;
                    TextView[] tvUserNames = {binding.tvPlayer1Name, binding.tvPlayer2Name, binding.tvPlayer3Name};
                    ImageView[] ivUserNames = {binding.ivPlayer1, binding.ivPlayer2, binding.ivPlayer3};
                    for (String name : userList) {
                        if (!name.equals(userName) && i < playerCount) {
                            tvUserNames[i].setText(name);
                            tvUserNames[i].setVisibility(View.VISIBLE);
                            ivUserNames[i].setVisibility(View.VISIBLE);
                            i++;
                        }
                    }

                }
                if (cm.code.matches("READY")) {
                    binding.btnReady.setVisibility(View.GONE);

                    if (cm.UserName.equals(userName)) { // 내 카드 정보일때
                        binding.btnReady.setVisibility(View.GONE);

                        for (String cardInfo : cm.list) {
                            myCardList.add(new Card(cardInfo.substring(0, 1), Integer.parseInt(cardInfo.substring(1)), true));
                            Collections.sort(myCardList, sortCard);
                        }

                        myCardListAdapter.notifyDataSetChanged();
                    } else { // 나 제외 다른 유저 카드 리스트 초기화
                        ArrayList<Card> cardList = new ArrayList<>();
                        for (String cardInfo : cm.list) {
                            cardList.add(new Card(cardInfo.substring(0, 1), Integer.parseInt(cardInfo.substring(1)), false));
                        }
                        Collections.sort(cardList, sortCard);
                        userCardList.put(cm.UserName, cardList);

                        setUserRecyclerView(cardList);
                    }

                    Log.d("CardList[내꺼]", myCardList.toString());
                    Log.d("CardList[다른사람]", userCardList.toString());

                }

                if (cm.code.matches("TURN")) {
                    if (cm.UserName.equals(userName)) { // 내 턴이면
                        binding.tvTurn.setText("내 턴");
                    } else {
                        binding.tvTurn.setText(cm.data);
                    }

                    showTakeOrMatchDialog();
                }

                if (cm.code.matches("TAKECARD")) {
                    if (cm.UserName.equals(userName)) { // 내 카드 뽑기면
                        Card card = new Card(cm.data.substring(0,1), Integer.parseInt(cm.data.substring(1)), true);
                        myCardList.add(card);
                        Collections.sort(myCardList, sortCard);
                        myCardListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }
}