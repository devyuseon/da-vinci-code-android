package com.hsu.davincicode;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private ArrayList<String> ranking;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        ImageView ivMedal;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            tvName = view.findViewById(R.id.tv_ranking_name);
            ivMedal = view.findViewById(R.id.iv_medal);
        }

        public TextView getTvName() {
            return tvName;
        }

        public ImageView getIvMedal() {
            return ivMedal;
        }
    }

    public RankingAdapter(ArrayList<String> ranking) {
        this.ranking = ranking;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_ranking, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvName.setText(ranking.get(position));
        switch (position) {
            case 0: // 1등
                holder.ivMedal.setImageResource(R.drawable.gold);
                break;
            case 1: // 2등
                holder.ivMedal.setImageResource(R.drawable.sliver);
                break;
            case 2: // 3등
                holder.ivMedal.setImageResource(R.drawable.bronze);
                break;
            case 3:
                holder.ivMedal.setVisibility(View.GONE);
                break;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return ranking.size();
    }
}
