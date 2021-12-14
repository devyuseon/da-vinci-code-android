package com.hsu.davincicode;


import android.content.Context;
import android.text.BoringLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

import android.app.AlertDialog;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.ViewHolder> implements ItemTouchHelperListener {

    private static String owner;

    private static Context context;
    private ArrayList<Card> cardList;
    private static Boolean canMatch = false;
    private static Boolean canOpen = false;
    private Boolean canSelect = false;


    private static UserInfo userInfo = UserInfo.getInstance();
    private NetworkObj networkObj = NetworkObj.getInstance();
    private static NetworkUtils networkUtils;

    private OnItemClickEventListener onItemClickEventListener;

    public void setOnItemClickListener(OnItemClickEventListener listener) {
        onItemClickEventListener = listener;
    }

    public interface OnItemClickEventListener {
        void onItemClick(View view, int position);
    }

    public void setCanSelect(Boolean canSelect) {
        this.canSelect = canSelect;
    }

    public static void setCanMatch(Boolean isCanMatch) {
        canMatch = isCanMatch;
    }

    public static Boolean getCanMatch(){
        return canMatch;
    }

    public void cardOpen(ArrayList<Card> newCardList) {
        this.cardList = newCardList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivCheck;
        private final ImageView ivCard;

        public ViewHolder(View view,final OnItemClickEventListener itemClickEventListener) {
            super(view);
            // 클릭 리스너 구현
            ivCard = view.findViewById(R.id.iv_card);
            ivCheck = view.findViewById(R.id.iv_check);

            ivCard.setOnClickListener(v -> {
                final int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    itemClickEventListener.onItemClick(v, position);
                }

            });
        }

        public ImageView getIVCard() {
            return ivCard;
        }

        public ImageView getIvCheck() {
            return ivCheck;
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

        return new ViewHolder(view, onItemClickEventListener);
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
            if (card.getIsOpened()) {
                holder.ivCheck.setVisibility(View.VISIBLE);
                if (card.getIsNewOpened()) {
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
            }
        } else { // 남의 카드면 오픈되었을 경우에만 카드 정보대로 카드 셋팅
            if (card.getIsOpened()) {
                if (card.getIsNewOpened()) {
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
                } else
                    holder.itemView.setBackgroundResource(imageId);

            } else {
                _imageId = "card_" + card.getCardColor() + "unknown";
                imageId = holder.itemView.getContext().getResources().getIdentifier(_imageId, "drawable", holder.itemView.getContext().getPackageName());

                holder.itemView.setBackgroundResource(imageId);


            }
        }

    }

    @Override
    public boolean onItemMove(int from_position, int to_position) {
        Card card = cardList.get(from_position);
        if (card.getIsJocker()) {
            cardList.remove(from_position);
            cardList.add(to_position, card);
            notifyItemMoved(from_position, to_position);

            String msg = String.format("%s//%s//%s//%s//%s", userInfo.getMyRoom().getRoomId(), card.getCardColor(), card.getIsOpened(), from_position, to_position);
            networkUtils.sendChatMsg(new ChatMsg(userInfo.getUserName(), "JOKER", msg));

            return true;
        } else return false;
    }

    @Override
    public void onItemSwipe(int position) {

    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }
}
