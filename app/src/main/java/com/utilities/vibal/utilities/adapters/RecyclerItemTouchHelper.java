package com.utilities.vibal.utilities.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    public RecyclerItemTouchHelper() {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if(viewHolder instanceof CashBoxManagerRecyclerAdapter.ViewHolder)
            if(direction==ItemTouchHelper.RIGHT)
                ((CashBoxManagerRecyclerAdapter.ViewHolder) viewHolder).deleteCashBox(viewHolder.getAdapterPosition());
//            else
//                ((CashBoxManagerRecyclerAdapter.ViewHolder) viewHolder).modifyCashBox(viewHolder.getAdapterPosition());
    }
}
