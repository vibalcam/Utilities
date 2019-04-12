package com.utilities.vibal.utilities.adapters;

import com.utilities.vibal.utilities.interfaces.CashBoxAdapterSwipable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.ItemTouchHelper.DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;
import static androidx.recyclerview.widget.ItemTouchHelper.UP;

public class CashBoxSwipeController extends ItemTouchHelper.Callback {
    private CashBoxAdapterSwipable adapter;

    public CashBoxSwipeController(CashBoxAdapterSwipable adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(UP|DOWN, LEFT|RIGHT);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return adapter.isDragEnabled();
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return adapter.isSwipeEnabled();
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        adapter.onItemMove(viewHolder.getAdapterPosition(),target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if(direction==LEFT)
            adapter.onItemDelete(viewHolder.getAdapterPosition());
        else if(direction==RIGHT)
            adapter.onItemModify(viewHolder.getAdapterPosition());
    }

    //    public CashBoxSwipeController() {
//        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
//    }
//
//    @Override
//    public boolean onItemMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//        return false;
//    }
//
//    @Override
//    public void onItemDelete(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//        if(viewHolder instanceof CashBoxManagerRecyclerAdapter.ViewHolder)
//            if(direction==ItemTouchHelper.RIGHT)
//                ((CashBoxManagerRecyclerAdapter.ViewHolder) viewHolder).deleteCashBox(viewHolder.getAdapterPosition());
////            else
////                ((CashBoxManagerRecyclerAdapter.ViewHolder) viewHolder).modifyCashBox(viewHolder.getAdapterPosition());
//    }
}
