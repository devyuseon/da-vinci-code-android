package com.hsu.davincicode;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hsu.davincicode.databinding.ActivityGameBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class GameActivity extends AppCompatActivity {
    private ActivityGameBinding binding;

    private String roomId;
    private String roomName;

    private UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj = NetworkObj.getInstance();
    private NetworkUtils networkUtils;
    private String userName;

    private ArrayList<String> userList = new ArrayList<>();
    private ArrayList<Card> myCardList = new ArrayList<>();
    private int leftCardCount = 24;
    private Map<String, ArrayList<Card>> userCardList = new HashMap<String, ArrayList<Card>>();
    private Comparator<Card> sortCard;

    private Handler handler; // 스레드에서 UI 작업하기 위한 핸들러
    private Boolean isDoReceiveRunning;

    @RequiresApi(api = Build.VERSION_CODES.N)
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

                    /* 카드 정보 초기화 */
                    for (String cardInfo : cm.list) {
                        myCardList.add(new Card(cardInfo.substring(0,1), Integer.parseInt(cardInfo.substring(1))));
                    }

                    /* 나 제외 다른 유저 카드 리스트 초기화 */
                    for (String user: userList) {
                        ArrayList<Card> cardList = new ArrayList<>();
                        for (int i = 0; i < myCardList.size(); i++) {
                            cardList.add(new Card());
                        }
                        userCardList.put(user, cardList); // userCardList: <이름, 카드리스트>
                    }

                    Collections.sort(myCardList, sortCard);
                    Log.d("CardList[내꺼]", myCardList.toString());
                    Log.d("CardList[다른사람]", userCardList.toString());
                }
            });
        }
    }

}