package com.hsu.davincicode;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class WaitListAdapter extends RecyclerView.Adapter<WaitListAdapter.ViewHolder> {

    private ArrayList<String> userList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserName;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            tvUserName = view.findViewById(R.id.tv_wait_user_name);
        }

        public TextView getTvUserName() {
            return tvUserName;
        }
    }

    public WaitListAdapter(ArrayList<String> userList) {
        this.userList = userList;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_waitlist, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvUserName.setText(userList.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return userList.size();
    }
}
