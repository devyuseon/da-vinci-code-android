package com.hsu.davincicode;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hsu.davincicode.R;
import com.hsu.davincicode.Room;

import java.util.ArrayList;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.ViewHolder> {

    private ArrayList<Room> roomList;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRoomName;
        private final TextView tvMaxCount;
        private final Button btnRequestRoomEnterance;

        private UserInfo userInfo = UserInfo.getInstance();
        private NetworkObj networkObj;
        private NetworkUtils networkUtils;
        private String userName;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            tvRoomName = view.findViewById(R.id.tv_room_name);
            tvMaxCount = view.findViewById(R.id.tv_max_count);
            btnRequestRoomEnterance = view.findViewById(R.id.btn_request_room_enterance);

            userName = userInfo.getUserName();
            networkObj = userInfo.getNetworkObj();
            networkUtils = new NetworkUtils(networkObj);

            btnRequestRoomEnterance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //final EditText etPw = new EditText(view.getContext());
                    View dialogView = View.inflate(view.getContext(), R.layout.dialog_input_passwd, null);
                    EditText etPw = dialogView.findViewById(R.id.et_dialog_passwd);
                    AlertDialog builder = new AlertDialog.Builder(view.getContext())
                            .setView(dialogView)
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String msg = String.format("%s//%s", tvRoomName.getText().toString(), etPw.getText().toString());
                                    ChatMsg obj = new ChatMsg(userName, "500", msg);
                                    networkUtils.sendChatMsg(obj); // 서버로 msg 전송
                                    dialog.dismiss();
                                }
                            }).show();
                }
            });
        }

        public TextView getTvRoomName() {
            return tvRoomName;
        }

        public TextView getTvMaxCount() {
            return tvMaxCount;
        }

        public Button getBtnRequestRoomEnterance() {
            return btnRequestRoomEnterance;
        }
    }

    public RoomListAdapter(ArrayList<Room> roomList) {
        this.roomList = roomList;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_roomlist, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getTvRoomName().setText(roomList.get(position).getRoomName());
        holder.getTvMaxCount().setText(String.format("%s/%s", roomList.get(position).getCurCount(), roomList.get(position).getMaxCount()));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return roomList.size();
    }
}