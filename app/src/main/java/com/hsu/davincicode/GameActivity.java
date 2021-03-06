package com.hsu.davincicode;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.SnackbarContentLayout;
import com.hsu.davincicode.databinding.ActivityGameBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

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
    private int leftBlackCardsCount = MAXCARDSCOUNT / 2;
    private int leftWhiteCardsCount = MAXCARDSCOUNT / 2;
    private ArrayList<String> userList = new ArrayList<>();
    private ArrayList<Card> myCardList = new ArrayList<>();
    private Map<String, ArrayList<Card>> userCardList = new HashMap<>();
    private Map<String, CardListAdapter> userCardListAdpater = new HashMap<>();
    private Map<String, RecyclerView> userRecyclerView = new HashMap<>();

    private Comparator<Card> sortCard;

    //private CountDownTimer countDownTimer;

    private Handler handler = new Handler(); // ??????????????? UI ???????????? ?????? ?????????
    private Boolean isDoReceiveRunning;

    private RankingAdapter rankingAdapter;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        binding.getRoot().setBackgroundColor(Color.parseColor("#808080"));
        getSupportActionBar().hide(); // ????????? ?????????

        // ??? ?????? ??????
        userName = userInfo.getUserName();
        networkUtils = new NetworkUtils(networkObj);
        setMyRecyclerView(); // ??? ?????? ????????? ?????????????????? ??????

        roomId = userInfo.getMyRoom().getRoomId();
        roomName = userInfo.getMyRoom().getRoomName();

        // ????????? ??????????????? ??????
        sendMsgToServer(new ChatMsg(userName, "ROOMUSERLIST", roomId));

        // Receive Task
        isDoReceiveRunning = true;
        ReceiveMsgTask receiveMsgTask = new ReceiveMsgTask();
        receiveMsgTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // ?????? ??????

        // ?????? ?????? ??????
        sortCard = new Comparator<Card>() {
            @Override
            public int compare(Card o1, Card o2) {
                // o2 - o1 ???????????? ????????????, -1 ????????? ????????????

                if (o2.getCardNum() - o1.getCardNum() == 0) { // ?????? ?????? ?????????
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
        myCardListAdapter = new CardListAdapter(getApplicationContext(), myCardList, userName);
        LinearLayoutManager manager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelperCallback(myCardListAdapter));

        binding.recyclerviewMycard.setLayoutManager(manager);
        binding.recyclerviewMycard.setAdapter(myCardListAdapter);
        binding.recyclerviewMycard.setItemAnimator(new SlideInLeftAnimator());
        helper.attachToRecyclerView(binding.recyclerviewMycard);
        binding.recyclerviewMycard.addItemDecoration(new RecyclerViewDecoration_w(40));

        myCardListAdapter.setOnItemClickListener((view, position) -> {
            if (myCardListAdapter.getCanSelect() && !myCardList.get(position).getIsOpened()) {
                final Card card = myCardList.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle(String.format("%s %d??? ????????? ?????? ???????????????????", getColorString(card.getCardColor()), card.getCardNum()))
                        .setPositiveButton("??????!", (dialog, which) -> {
                            String msg = String.format("%s//%d", roomId, position);
                            sendMsgToServer(new ChatMsg(userName, "CARDSELECT", msg));
                            sendMsgToServer(new ChatMsg(userName, "TURN", roomId));
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    /* ??????????????????, ?????????????????? ??????????????? ?????????. ???:????????????, ???: */
    public void setUserRecyclerView(String user) {
        RecyclerView recyclerView = null;
        ArrayList<Card> cardList = userCardList.get(user);

        switch (userList.size()) {
            case 1:
                recyclerView = binding.recyclerviewPlayer1;
                break;
            /*case 2:
                recyclerView = binding.recyclerviewPlayer2;
                break;
            case 3:
                recyclerView = binding.recyclerviewPlayer3;
                break;*/
            default:
                System.out.println("??????????????? ??? ????????? ??????...!");
                break;
        }

        if (recyclerView != null) {
            CardListAdapter cardListAdapter = new CardListAdapter(getApplicationContext(), cardList, user);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
            recyclerView.setAdapter(cardListAdapter);
            recyclerView.addItemDecoration(new RecyclerViewDecoration_w(40));
            recyclerView.setHasFixedSize(true);

            userCardListAdpater.put(user, cardListAdapter);
            userRecyclerView.put(user, recyclerView);
            userCardListAdpater.get(user).setOnItemClickListener((view, position) -> {
                if (userCardListAdpater.get(user).getCanMatch() && !userCardList.get(user).get(position).getIsOpened()) {
                    showMatchWhatDialog(user, view, position);
                }
            });
        }

        if (userList.size() == userRecyclerView.size()) {
            sendMsgToServer(new ChatMsg(userName, "START", roomId));
        }

    }

    public String getColorString(String s) {
        if (s.equals("b")) return "?????????";
        if (s.equals("w")) return "??????";
        else return "";
    }

    public void showMatchWhatDialog(String user, View view, int position) {
        Card card = userCardList.get(user).get(position);

        View dialogView = View.inflate(this, R.layout.dialog_match, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle(String.format("%s, ??? ??? ????????????????", getColorString(card.getCardColor())))
                .setView(dialogView)
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        Button btn_match = dialogView.findViewById(R.id.btn_dialog_match);

        btn_match.setOnClickListener(v1 -> {
            EditText editText = dialogView.findViewById(R.id.et_dialog_match_num);
            String numberStr = editText.getText().toString().trim();
            if (numberStr.equals(""))
                Snackbar.make(view, "?????? ????????? ?????????.", Snackbar.LENGTH_SHORT).show();
            else {
                int number = Integer.parseInt(numberStr);
                if (number > 11 || number < -1) {
                    Snackbar.make(view, "0?????? 11????????? ??????, ?????? ????????? ?????? -1??? ????????? ?????????.", Snackbar.LENGTH_SHORT).show();
                } else {
                    String msg = user + "//" + card.getCardColor() + numberStr + "//" + position + "//" + userInfo.getMyRoom().getRoomId();
                    networkUtils.sendChatMsg(new ChatMsg(userInfo.getUserName(), "MATCHCARD", msg));
                    setUserListCanMatch(false);
                    dialog.dismiss();
                }
            }
        });
    }

    public void showRankingDialog(ArrayList<String> ranking) {
        View dialogView = View.inflate(this, R.layout.dialog_gameover, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setPositiveButton("?????????", (dialog, which) -> {
                    startActivity(new Intent(this, RoomListActivity.class));
                    finish();
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerview_rank);
        RankingAdapter rankingAdapter = new RankingAdapter(ranking);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(rankingAdapter);
        dialog.show();
    }

    public void setUserListCanMatch(Boolean isCanMatch) {
        for (String user : userList) {
            userCardListAdpater.get(user).setCanMatch(isCanMatch);
        }
    }

    public void notifyLeftCardsCount(String color, String opt) {
        if (opt.equals("INIT")) {
            leftCardsCount -= myCardList.size() * 2;
        }
        if (opt.equals("DECREASE")) {
            if (color.equals("b")) {
                leftBlackCardsCount--;
            } else {
                leftWhiteCardsCount--;
            }
            leftCardsCount--;
        }

        Log.d("GAME", String.format("?????? ??????: ????????? %d???, ?????? %d???, ??? %d???", leftBlackCardsCount, leftWhiteCardsCount, leftCardsCount));
    }

    public void initLeftCardByColor(ArrayList<Card> cardList) {
        int b_cnt = 0;
        int w_cnt = 0;
        for (Card card : cardList) {
            if (card.getCardColor().equals("b")) {
                b_cnt++;
            }
            if (card.getCardColor().equals("w")) {
                w_cnt++;
            }
        }
        leftBlackCardsCount -= b_cnt;
        leftWhiteCardsCount -= w_cnt;
    }

    // ?????? ????????? or PASS ???????????????
    public void showPassOrMatchDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_choose_match_or_pass, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("?????? ???????????? ?????????????????????????")
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
            //setTimer(10);
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

    public void setTakeBlackCardBtnVisibility(int visibility) {
        binding.btnTakeBlackCard.setVisibility(visibility);
    }

    public void setTakeWhiteCardBtnVisibility(int visibility) {
        binding.btnTakeWhiteCard.setVisibility(visibility);
    }

    public void setLeftBlackCardsVisibility(int visibility) {
        binding.ivCardBlackBottom.setVisibility(visibility);
        binding.ivCardBlackTop.setVisibility(visibility);
    }

    public void setLeftWhiteCardsVisibility(int visibility) {
        binding.ivCardWhiteBottom.setVisibility(visibility);
        binding.ivCardWhiteTop.setVisibility(visibility);
    }

    //------------------------------------- ???????????? ?????? -------------------------------------------//

    /* ?????? ????????? ????????????, ?????? ???????????? ?????? */
    public void ROOMLISTUSER(ChatMsg cm) {
        int playerCount = cm.list.size() - 1; // ?????? ????????? ???????????? ???
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

        if (cm.UserName.equals(userName)) { // ??? ?????? ????????????
            binding.btnReady.setVisibility(View.GONE);

            myCardList = initCardList(cm.list, myCardList, true);
            Collections.sort(myCardList, sortCard); // ??????

            myCardListAdapter.notifyDataSetChanged();

            notifyLeftCardsCount("INIT", "INIT");
            initLeftCardByColor(myCardList);

        } else { // ??? ?????? ?????? ?????? ?????? ????????? ?????????
            ArrayList<Card> cardList = new ArrayList<>();
            cardList = initCardList(cm.list, cardList, false);
            Collections.sort(cardList, sortCard); // ??????
            userCardList.put(cm.UserName, cardList); // ???: username ???: user cardlist
            setUserRecyclerView(cm.UserName);
            initLeftCardByColor(cardList);
        }

        Log.d("CardList[??????]", myCardList.toString());
        Log.d("CardList[????????????]", userCardList.toString());
    }

    public void TURN(ChatMsg cm) {
        if (cm.data.equals(userName)) { // ??? ?????????
            if (leftCardsCount > 0) {
                Snackbar.make(binding.getRoot(), "????????? ????????????! ???????????? ????????? ???????????????????", Snackbar.LENGTH_SHORT).show();

                if (leftBlackCardsCount > 0) {
                    setTakeBlackCardBtnVisibility(View.VISIBLE);
                    binding.btnTakeBlackCard.setOnClickListener(v -> {
                        sendMsgToServer(new ChatMsg(userName, "TAKECARD", roomId + "//b"));
                        showPassOrMatchDialog();
                        setTakeBlackCardBtnVisibility(View.INVISIBLE);
                        setTakeWhiteCardBtnVisibility(View.INVISIBLE);
                    });
                }

                if (leftWhiteCardsCount > 0) {
                    setTakeWhiteCardBtnVisibility(View.VISIBLE);
                    binding.btnTakeWhiteCard.setOnClickListener(v -> {
                        sendMsgToServer(new ChatMsg(userName, "TAKECARD", roomId + "//w"));
                        showPassOrMatchDialog();
                        setTakeBlackCardBtnVisibility(View.INVISIBLE);
                        setTakeWhiteCardBtnVisibility(View.INVISIBLE);
                    });
                }

            } else {
                Snackbar.make(binding.getRoot(), "????????? ????????????! ??? ?????? ?????? ????????? ????????????????", Snackbar.LENGTH_SHORT).show();
                showPassOrMatchDialog();
            }

        } else {
            Snackbar.make(binding.getRoot(), cm.data + "??? ????????????.", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void TAKECARD(ChatMsg cm) {
        Card card;
        if (cm.UserName.equals(userName)) { // ??? ?????? ?????????
            card = new Card(cm.data.substring(0, 1), Integer.parseInt(cm.data.substring(1)), true);
            myCardList.add(card);
            Collections.sort(myCardList, sortCard);
            myCardListAdapter.setCardList(myCardList);
            myCardListAdapter.notifyDataSetChanged();
            Snackbar.make(binding.getRoot(), "???????????? ?????? 1?????? ???????????????.", Snackbar.LENGTH_SHORT).show();
            Log.d("MyCardList", myCardList.toString());
        } else {
            card = new Card(cm.data.substring(0, 1), Integer.parseInt(cm.data.substring(1)), false);
            userCardList.get(cm.UserName).add(card);
            Collections.sort(userCardList.get(cm.UserName), sortCard);
            userCardListAdpater.get(cm.UserName).notifyDataSetChanged();
            Snackbar.make(binding.getRoot(), cm.UserName + "??? " + "?????? 1?????? ???????????????.", Snackbar.LENGTH_SHORT).show();
        }
        notifyLeftCardsCount(card.getCardColor(), "DECREASE");

        if (leftBlackCardsCount == 0) setLeftBlackCardsVisibility(View.INVISIBLE);
        if (leftWhiteCardsCount == 0) setLeftWhiteCardsVisibility(View.INVISIBLE);
    }

    public void SUCCESS(ChatMsg cm) {
        if (cm.UserName.equals(userName)) {
            Snackbar.make(binding.getRoot(), "????????? ??????!????", Snackbar.LENGTH_SHORT).show();

            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (leftCardsCount > 0)
                    showPassOrMatchDialog();
                else {
                    sendMsgToServer(new ChatMsg(userName, "TURN", roomId));
                }
            }, 2000);

        } else
            Snackbar.make(binding.getRoot(), String.format("%s??? ????????? ???????????????!????", cm.UserName), Snackbar.LENGTH_SHORT).show();
    }


    public void FAIL(ChatMsg cm) {
        if (cm.UserName.equals(userName))
            Snackbar.make(binding.getRoot(), String.format("?????? ????????? ?????????? ????????? ???????????????..", cm.UserName), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(binding.getRoot(), String.format("%s??? ?????? ???????????? ??????????????????. %s??? ????????? ???????????????!", cm.UserName, cm.UserName), Snackbar.LENGTH_SHORT).show();

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            sendMsgToServer(new ChatMsg(userName, "TURN", roomId));
        }, 2000);

    }

    public void CARDOPEN(ChatMsg cm) {
        String cardOwner = cm.UserName;
        int cardIndex = Integer.parseInt(cm.data.trim());

        if (cardOwner.equals(userName)) {
            myCardListAdapter.setCanSelect(false);
            myCardList.get(cardIndex).setIsOpened(true);
            myCardList.get(cardIndex).setIsNewOpened(true);
            myCardListAdapter.setCardList(myCardList);
            myCardListAdapter.notifyDataSetChanged();
        } else {
            userCardList.get(cardOwner).get(cardIndex).setIsOpened(true);
            userCardList.get(cardOwner).get(cardIndex).setIsNewOpened(true);
            userCardListAdpater.get(cardOwner).setCardList(userCardList.get(cardOwner));
            userCardListAdpater.get(cardOwner).notifyDataSetChanged();
        }
    }

    public void JOKER(ChatMsg cm) {
        String[] data = cm.data.split("//");
        String color = data[1];
        Boolean isOpened = Boolean.getBoolean(data[2]);
        int from = Integer.parseInt(data[3]);
        int to = Integer.parseInt(data[4]);
        String owner = data[5];

        if (owner.equals(userName)) {
            Log.d("MyCardList", myCardList.toString());
        } else {
            Card card = new Card(color, -1, false);
            card.setIsJocker(true);
            card.setIsOpened(isOpened);

            userCardList.get(owner).remove(from);
            userCardList.get(owner).add(to, card);
            userCardListAdpater.get(owner).setCardList(userCardList.get(owner));
            userCardListAdpater.get(owner).notifyDataSetChanged();
        }

    }

    public void CARDSELECT(ChatMsg cm) {
        Snackbar.make(binding.getRoot(), "????????? ???????????????. ????????? ????????? ??????????????????", Snackbar.LENGTH_SHORT).show();
        myCardListAdapter.setCanSelect(true);
    }

    public void LOOSE(ChatMsg cm) {
        if (cm.UserName.equals(userName)) {
            Snackbar.make(binding.getRoot(), "????????? ?????? ????????? ?????????????????????.", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(binding.getRoot(), String.format("%s??? ?????? ????????? ?????????????????????.", cm.UserName), Snackbar.LENGTH_SHORT).show();
            switch (userList.indexOf(cm.UserName)) {
                case 0:
                    binding.ivPlayer1LooseCheck.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    binding.ivPlayer2LooseCheck.setVisibility(View.VISIBLE);
            }
        }
    }

    public void GAMEOVER(ChatMsg cm) {
        showRankingDialog(cm.list);
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

                if (cm.code.matches("JOKER")) {
                    JOKER(cm);
                }

                if (cm.code.matches("CARDSELECT")) {
                    CARDSELECT(cm);
                }

                if (cm.code.matches("LOOSE")) {
                    LOOSE(cm);
                }

                if (cm.code.matches("GAMEOVER")) {
                    GAMEOVER(cm);
                }
            });
        }
    }

    public class RecyclerViewDecoration_w extends RecyclerView.ItemDecoration {

        private final int divWidth;

        public RecyclerViewDecoration_w(int divWidth) {
            this.divWidth = divWidth;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.right = divWidth;
        }
    }

    public class RecyclerViewDecoration_h extends RecyclerView.ItemDecoration {

        private final int divHeight;

        public RecyclerViewDecoration_h(int divHeight) {
            this.divHeight = divHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.top = divHeight;
        }
    }
}