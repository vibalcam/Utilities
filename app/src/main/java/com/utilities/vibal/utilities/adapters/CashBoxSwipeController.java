package com.utilities.vibal.utilities.adapters;

import android.util.Log;

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
    private int fromIndex = -1;
    private int toIndex = -1;

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
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
        // Remember FIRST fromPosition
        if(fromIndex==-1)   // fromIndex hasn't been set -> beginning of the move
            fromIndex = source.getAdapterPosition();
        toIndex = target.getAdapterPosition();

        adapter.onItemMove(source.getAdapterPosition(),target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if(direction==LEFT)
            adapter.onItemDelete(viewHolder.getAdapterPosition());
        else if(direction==RIGHT)
            adapter.onItemModify(viewHolder.getAdapterPosition());
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        Log.d("Prueba", "clearView: " + fromIndex + ", " + toIndex);
        if(fromIndex!=-1 && toIndex!=-1) {  // if there has been a move motion
            adapter.onItemDrop(fromIndex,toIndex);
            Log.d("Prueba", "clearView: mueve");
        }
        fromIndex = -1;
        toIndex = -1;
        Log.d("Prueba", "clearView: " + fromIndex + ", " + toIndex);
    }
}
