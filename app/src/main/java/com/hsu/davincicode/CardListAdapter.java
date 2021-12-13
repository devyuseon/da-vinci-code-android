package com.hsu.davincicode;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

import android.app.AlertDialog;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.ViewHolder> {

    private static String owner;

    private static Context context;
    private ArrayList<Card> cardList;
    private static Boolean canMatch = false;
    private static Boolean canOpen = false;

    private static UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj = NetworkObj.getInstance();
    private static NetworkUtils networkUtils;

    public static void setCanMatch(Boolean isCanMatch) {
        canMatch = isCanMatch;
    }

    public void cardOpen(ArrayList<Card> newCardList) {
        this.cardList = newCardList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View view) {
            super(view);
            // 클릭 리스너 구현
            ImageView ivCard = view.findViewById(R.id.iv_card);
            ivCard.setOnClickListener(v -> {
                if (canMatch) {
                    View dialogView = View.inflate(context, R.layout.dialog_match, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("카드 색상, 숫자를 맞춰주세요!")
                            .setView(dialogView)
                            .setCancelable(false);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    Button btn_match = dialogView.findViewById(R.id.btn_dialog_match);

                    btn_match.setOnClickListener(v1 -> {
                        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup_dialog_match);
                        EditText editText = dialogView.findViewById(R.id.et_dialog_match_num);
                        String color = "";
                        String numberStr = editText.getText().toString().trim();
                        if(numberStr.equals(""))
                            Snackbar.make(view, "값을 입력해 주세요.", Snackbar.LENGTH_LONG).show();
                        else {
                            int number = Integer.parseInt(numberStr);
                            switch (radioGroup.getCheckedRadioButtonId()) {
                                case R.id.radio_BLACK:
                                    color = "b";
                                    break;
                                case R.id.radio_WHITE:
                                    color = "w";
                            }
                            if (number > 11 || number < -1) {
                                Snackbar.make(view, "0이상 11이하의 숫자, 또는 조커일 경우 -1을 입력해 주세요.", Snackbar.LENGTH_LONG).show();
                            } else {
                                String msg = owner + "//" + color + numberStr + "//" + getAdapterPosition() + "//" + userInfo.getMyRoom().getRoomId();
                                networkUtils.sendChatMsg(new ChatMsg(userInfo.getUserName(), "MATCHCARD", msg));
                                dialog.dismiss();
                            }
                        }

                    });
                }
            });
        }

    }

    public CardListAdapter(Context context, ArrayList<Card> cardList, String userName) {
        this.context = context;
        this.cardList = cardList;
        this.owner = userName;
        networkUtils = new NetworkUtils(networkObj);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_cardlist, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Card card = cardList.get(position);
        String _imageId;

        if (card.getIsJocker()) { // 조커일 경우
            _imageId = "card_" + card.getCardColor() + "j";
        } else { // 일반 카드일 경우
            _imageId = "card_" + card.getCardColor() + card.getCardNum();
        }
        // 카드 종류에 맞게 drawable 아이디 설정
        int imageId = holder.itemView.getContext().getResources().getIdentifier(_imageId, "drawable", holder.itemView.getContext().getPackageName());

        if (card.getIsMyCard()) { // 내 카드면 카드 정보대로 카드 셋팅
            holder.itemView.setBackgroundResource(imageId);
        } else { // 남의 카드면 오픈되었을 경우에만 카드 정보대로 카드 셋팅
            if (card.getIsOpened()) {
                if(card.getIsNewOpened()) {
                    int finalImageId = imageId;
                    holder.itemView.animate().withLayer()
                            .rotationY(90)
                            .setDuration(150)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    holder.itemView.setBackgroundResource(finalImageId);
                                    holder.itemView.setRotationY(-90);
                                    holder.itemView.animate().withLayer()
                                            .rotationY(0)
                                            .setDuration(500)
                                            .start();
                                }
                            }).start();
                    card.setIsNewOpened(false);
                }
                else
                    holder.itemView.setBackgroundResource(imageId);

            } else {
                _imageId = "card_" + card.getCardColor() + "unknown";
                imageId = holder.itemView.getContext().getResources().getIdentifier(_imageId, "drawable", holder.itemView.getContext().getPackageName());

                holder.itemView.setBackgroundResource(imageId);


            }
        }

    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }
}
