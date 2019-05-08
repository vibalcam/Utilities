package com.utilities.vibal.utilities.adapters;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.interfaces.CashBoxAdapterSwipable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;
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
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//        if(actionState==ACTION_STATE_SWIPE) {
//            // dX>0 -> right swipe
//            Paint paint = new Paint();
//            Drawable drawable;
//            if(dX>0) {  // right swipe -> modify
//                drawable = recyclerView.getContext().getDrawable(R.drawable.ic_edit_white_24dp);
//                paint.setColor(Color.BLUE);
//            } else {    // left swipe -> delete
//                drawable = recyclerView.getContext().getDrawable(R.drawable.delete);
//                paint.setColor(Color.RED);
//            }
//
//            if(drawable!=null) {
//                Rect rectTotal = new Rect(viewHolder.itemView.getLeft(), viewHolder.itemView.getTop(), viewHolder.itemView.getRight(), viewHolder.itemView.getBottom());
//                c.drawRect(rectTotal, paint);
//
//                int size = drawable.getIntrinsicHeight()>rectTotal.height() ? rectTotal.height() : drawable.getIntrinsicHeight();
//                int padding = (rectTotal.height()-size)/2;
//                if(dX>0)    // icon on the left side
//                    drawable.setBounds(rectTotal.left+padding,rectTotal.top+padding,rectTotal.left+padding+size,rectTotal.bottom-padding);
//                else    // icon on the right side
//                    drawable.setBounds(rectTotal.right-padding-size,rectTotal.top+padding,rectTotal.right-padding,rectTotal.bottom-padding);
//                drawable.draw(c);
//
//                Log.d("Prueba", "onChildDraw:Intrinsic width=" + drawable.getIntrinsicWidth() + " height=" + drawable.getIntrinsicHeight() + " given size:" + size);
//                Log.d("Prueba", "onChildDraw:Min width=" + drawable.getMinimumWidth() + " height=" + drawable.getMinimumHeight());
//            }
//        }
        if(isCurrentlyActive && actionState==ACTION_STATE_SWIPE) {
            Paint paint = new Paint();
            Drawable drawable;
            Rect rectTotal = new Rect(viewHolder.itemView.getLeft(), viewHolder.itemView.getTop(), viewHolder.itemView.getRight(), viewHolder.itemView.getBottom());

            // Choose Drawable and background color
            if(dX>0) {  // right swipe -> modify
                drawable = recyclerView.getContext().getDrawable(R.drawable.ic_edit_white_24dp);
                paint.setColor(Color.BLUE);
            } else {    // left swipe -> delete
                drawable = recyclerView.getContext().getDrawable(R.drawable.delete);
                paint.setColor(Color.RED);
            }
            if(Math.abs(dX)<rectTotal.width()/3)
                paint.setColor(Color.LTGRAY);

            // Draw
            if(drawable!=null) {
                c.drawRect(rectTotal, paint);

                int size = drawable.getIntrinsicHeight()>rectTotal.height() ? rectTotal.height() : drawable.getIntrinsicHeight();
                int padding = (rectTotal.height()-size)/2;
                if(dX>0)    // icon on the left side
                    drawable.setBounds(rectTotal.left+padding,rectTotal.top+padding,rectTotal.left+padding+size,rectTotal.bottom-padding);
                else    // icon on the right side
                    drawable.setBounds(rectTotal.right-padding-size,rectTotal.top+padding,rectTotal.right-padding,rectTotal.bottom-padding);
                drawable.draw(c);

//                Log.d("Prueba", "onChildDraw:Intrinsic width=" + drawable.getIntrinsicWidth() + " height=" + drawable.getIntrinsicHeight() + " given size:" + size);
//                Log.d("Prueba", "onChildDraw:Min width=" + drawable.getMinimumWidth() + " height=" + drawable.getMinimumHeight() +" swipe threshold:" + getSwipeThreshold(viewHolder));
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        if(fromIndex!=-1 && toIndex!=-1) // if there has been a move motion
            adapter.onItemDrop(fromIndex,toIndex);
        fromIndex = -1;
        toIndex = -1;
    }
}
