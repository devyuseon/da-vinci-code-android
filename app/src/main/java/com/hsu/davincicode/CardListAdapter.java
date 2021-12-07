package com.hsu.davincicode;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.ViewHolder> {

    private ArrayList<Card> cardList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View view) {
            super(view);

            // 클릭 리스너 구현
        }

    }

    public CardListAdapter(ArrayList<Card> cardList) {
        this.cardList = cardList;
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
