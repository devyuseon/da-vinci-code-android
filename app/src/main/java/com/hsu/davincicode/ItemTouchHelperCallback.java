package com.hsu.davincicode;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private static final String TAG = "ItemTouchHelperCallback";


    private ItemTouchHelperListener listener;

    public ItemTouchHelperCallback(ItemTouchHelperListener listener) {
        this.listener = listener;
    }


    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int swipeFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int dragFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    public boolean isLongPressDragEnabled() {
        /*isLongPressDragEnabled*/
        //  true를 반환하도록 설정하면 롱클릭을 감지한다.
        return true;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

        /* onMove */
        // 리사이클러뷰,viewHolder, target(viewHolder 중 선택된 아이템)을 입력받아 움직임을 감지한다.
        // ItemTouchHelperListener의 onItemMove 메소드로 해당 아이템의 움직임을 감지한다.
        // onItemMove 메소드는 아이템이 움직이고 있는 가를 판별하고 boolean 값으로 반환한다.

        return listener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        /* onSwiped */
        // 리사이클러뷰의 뷰홀더와 움직일 방향을 입력받는다.
        // ItemTouchHelperListner의 onItemSwipe 메소드에 움직일 방향을 입력하여 swipe를 구현한다.

        listener.onItemSwipe(viewHolder.getAdapterPosition());
    }
}

