package com.hsu.davincicode;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.google.android.material.snackbar.Snackbar;
import com.hsu.davincicode.databinding.ActivityGameBinding;

import java.util.ArrayList;
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

    CardListAdapter myCardListAdapter;
    private final int MAXCARDSCOUNT = 26;
    private int leftCardsCount = MAXCARDSCOUNT;
    private ArrayList<String> userList = new ArrayList<>();
    private ArrayList<Card> myCardList = new ArrayList<>();
    private Map<String, ArrayList<Card>> userCardList = new HashMap<>();
    private Map<String, CardListAdapter> userCardListAdpater = new HashMap<>();
    private Map<String, RecyclerView> userRecyclerView = new HashMap<>();

    private Comparator<Card> sortCard;

    private CountDownTimer countDownTimer;

    private Handler handler = new Handler(); // 스레드에서 UI 작업하기 위한 핸들러
    private Boolean isDoReceiveRunning;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        binding.getRoot().setBackgroundColor(Color.parseColor("#808080"));
        getSupportActionBar().hide(); // 액션바 숨기기

        // 내 정보 설정
        userName = userInfo.getUserName();
        networkUtils = new NetworkUtils(networkObj);
        setMyRecyclerView(); // 내 카드 리스트 리사이클러뷰 세팅

        roomId = userInfo.getMyRoom().getRoomId();
        roomName = userInfo.getMyRoom().getRoomName();

        // 서버에 유저리스트 요청
        sendMsgToServer(new ChatMsg(userName, "ROOMUSERLIST", roomId));

        // Receive Task
        isDoReceiveRunning = true;
        ReceiveMsgTask receiveMsgTask = new ReceiveMsgTask();
        receiveMsgTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // 병렬 시행

        // 정렬 기준 설정
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
            networkUtils.sendChatMsg(new ChatMsg(userName, "READY", roomId));
        });
    }

    public void sendMsgToServer(ChatMsg cm) {
        networkUtils.sendChatMsg(cm);
    }

    public void setMyRecyclerView() {
        myCardListAdapter = new CardListAdapter(getApplicationContext(),myCardList, userName);
        binding.recyclerviewMycard.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.recyclerviewMycard.setAdapter(myCardListAdapter);
    }

    /* 리스트어답터, 리사이클러뷰 헤쉬맵으로 초기화. 키:유저네임, 값: */
    public void setUserRecyclerView(String user) {
        RecyclerView recyclerView = null;
        ArrayList<Card> cardList = userCardList.get(user);

        switch (userList.size()) {
            case 1:
                recyclerView = binding.recyclerviewPlayer1;
                break;
            // 2,3,4 추가할것
            default:
                System.out.println("플레이어가 한 명밖에 없음...!");
                break;
        }

        if (recyclerView != null) {
            CardListAdapter cardListAdapter = new CardListAdapter(getApplicationContext(), cardList, user);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
            recyclerView.setAdapter(cardListAdapter);

            userCardListAdpater.put(user, cardListAdapter);
            userRecyclerView.put(user, recyclerView);
        }

        if (userList.size() == userRecyclerView.size()) {
            sendMsgToServer(new ChatMsg(userName, "START", roomId));
        }

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

    public void setUserListCanMatch(Boolean isCanMatch) {
        for (String user : userList) {
            userCardListAdpater.get(user).setCanMatch(isCanMatch);
        }
    }

    public void notifyLeftCardsCount(String opt) {
        if(opt.equals("INIT")) {
            leftCardsCount -= myCardList.size() * 2;
        }
        if(opt.equals("DECREASE")) {
            leftCardsCount --;
        }

        Log.d("GAME", String.format("남은 카드는 %d개 입니다.",leftCardsCount));
    }

    // 카드 맞추기 or PASS 다이얼로그
    public void showPassOrMatchDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_choose_match_or_pass, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("카드 맞추기에 도전하시겠습니까?")
                .setView(dialogView)
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        Button btn_pass = dialogView.findViewById(R.id.btn_dialog_choose_pass);
        Button btn_match = dialogView.findViewById(R.id.btn_dialog_choose_match);

        btn_pass.setOnClickListener(v -> {
            sendMsgToServer(new ChatMsg(userName, "PASS", roomId));
            dialog.dismiss();
        });

        btn_match.setOnClickListener(v -> {
            setTimer(10);
            setUserListCanMatch(true);
            dialog.dismiss();
        });

    }

    public ArrayList<Card> initCardList(ArrayList<String> chatMsgList, ArrayList<Card> cardList, Boolean isMyCard) {
        for (String cardInfo : chatMsgList) {
            cardList.add(new Card(cardInfo.substring(0, 1), Integer.parseInt(cardInfo.substring(1)), isMyCard));
        }
        return cardList;
    }

    public void requestTakeCard() {
        if (leftCardsCount > 0)
            sendMsgToServer(new ChatMsg(userName, "TAKECARD", roomId));
        showPassOrMatchDialog();
    }

    public void setTakeCardBtnVisibility(int visibility) {
        binding.btnTakeBlackCard.setVisibility(visibility);
        binding.btnTakeWhiteCard.setVisibility(visibility);
    }

    //------------------------------------- 프로토콜 설정 -------------------------------------------//

    /* 유저 프로필 이미지뷰, 이름 텍스트뷰 설정 */
    public void ROOMLISTUSER(ChatMsg cm) {
        int playerCount = cm.list.size() - 1; // 나를 제외한 플레이어 수
        if (userList.size() == 0) {
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
        System.out.println("userList: " + userList);

    }

    public void READY(ChatMsg cm) {
        binding.btnReady.setVisibility(View.GONE);

        if (cm.UserName.equals(userName)) { // 내 카드 정보일때
            binding.btnReady.setVisibility(View.GONE);

            myCardList = initCardList(cm.list, myCardList, true);
            Collections.sort(myCardList, sortCard); // 정렬

            myCardListAdapter.notifyDataSetChanged();

            notifyLeftCardsCount("INIT");

        } else { // 나 제외 다른 유저 카드 리스트 초기화
            ArrayList<Card> cardList = new ArrayList<>();
            cardList = initCardList(cm.list, cardList, false);
            Collections.sort(cardList, sortCard); // 정렬
            userCardList.put(cm.UserName, cardList); // 키: username 값: user cardlist

            setUserRecyclerView(cm.UserName);
        }

        Log.d("CardList[내꺼]", myCardList.toString());
        Log.d("CardList[다른사람]", userCardList.toString());
    }

    public void TURN(ChatMsg cm) {
        if (cm.data.equals(userName)) { // 내 턴이면
            Snackbar.make(binding.getRoot(),  "당신의 턴입니다! 카드뽑기 버튼을 눌러주세요😊", Snackbar.LENGTH_SHORT).show();

            setTakeCardBtnVisibility(View.VISIBLE);
            binding.btnTakeBlackCard.setOnClickListener(v-> {
                requestTakeCard();
                setTakeCardBtnVisibility(View.INVISIBLE);
            });
            binding.btnTakeWhiteCard.setOnClickListener(v-> {
                requestTakeCard();
                setTakeCardBtnVisibility(View.INVISIBLE);
            });
        } else {
            Snackbar.make(binding.getRoot(), cm.data + "의 턴입니다.", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void TAKECARD(ChatMsg cm) {
        if (cm.UserName.equals(userName)) { // 내 카드 뽑기면
            Card card = new Card(cm.data.substring(0, 1), Integer.parseInt(cm.data.substring(1)), true);
            myCardList.add(card);
            Collections.sort(myCardList, sortCard);
            myCardListAdapter.notifyDataSetChanged();
            Snackbar.make(binding.getRoot(), "랜덤으로 카드 1장을 뽑았습니다.", Snackbar.LENGTH_SHORT).show();
        } else {
            Card card = new Card(cm.data.substring(0, 1), Integer.parseInt(cm.data.substring(1)), false);
            userCardList.get(cm.UserName).add(card);
            Collections.sort(userCardList.get(cm.UserName), sortCard);
            userCardListAdpater.get(cm.UserName).notifyDataSetChanged();
            Snackbar.make(binding.getRoot(), cm.UserName + "이 " + "카드 1장을 뽑았습니다.", Snackbar.LENGTH_SHORT).show();
        }
        notifyLeftCardsCount("DECREASE");
    }

    public void SUCCESS(ChatMsg cm) {
        if(cm.UserName.equals(userName))
            showPassOrMatchDialog();
        else
            Snackbar.make(binding.getRoot(), String.format("%s가 카드를 맞췄습니다!👏", cm.UserName), Snackbar.LENGTH_SHORT).show();
    }

    public void FAIL(ChatMsg cm) {
        if(cm.UserName.equals(userName))
            Snackbar.make(binding.getRoot(), String.format("카드 맞추기 실패😱 카드가 오픈됩니다..", cm.UserName), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(binding.getRoot(), String.format("%s가 카드 맞추기에 실패했습니다. %s의 카드가 오픈됩니다!", cm.UserName, cm.UserName), Snackbar.LENGTH_SHORT).show();
    }

    public void CARDOPEN(ChatMsg cm) {
        String cardOwner = cm.UserName;
        int cardIndex = Integer.parseInt(cm.data.trim());

        if(cardOwner.equals(userName)) {
            // 내 카드 오픈됨
        } else {
            userCardList.get(cardOwner).get(cardIndex).setIsOpened(true);
            userCardList.get(cardOwner).get(cardIndex).setIsNewOpened(true);
            userCardListAdpater.get(cardOwner).setCardList(userCardList.get(cardOwner));
            userCardListAdpater.get(cardOwner).notifyDataSetChanged();
        }
    }

    //--------------------------------------------------------------------------------------------//

    /* Receive Task */
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

                if (cm.code.matches("ROOMUSERLIST")) {
                    ROOMLISTUSER(cm);
                }
                if (cm.code.matches("READY")) {
                    READY(cm);
                }

                if (cm.code.matches("TURN")) {
                    TURN(cm);
                }

                if (cm.code.matches("TAKECARD")) {
                    TAKECARD(cm);
                }

                if (cm.code.matches("SUCCESS")) {
                    SUCCESS(cm);
                }

                if (cm.code.matches("FAIL")) {
                    FAIL(cm);
                }

                if (cm.code.matches("CARDOPEN")) {
                    CARDOPEN(cm);
                }
            });
        }
    }
}