package com.vibal.utilities.ui.swipeController;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.vibal.utilities.R;
import com.vibal.utilities.util.LogUtil;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;
import static androidx.recyclerview.widget.ItemTouchHelper.DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;
import static androidx.recyclerview.widget.ItemTouchHelper.UP;
import static com.vibal.utilities.ui.settings.SettingsActivity.KEY_SWIPE_LEFT_DELETE;

public class CashBoxSwipeController extends ItemTouchHelper.Callback {
    private static final String TAG = "PruebaSwipeController";
    private static final float SWIPE_THRESHOLD = 0.4f;

    private boolean swipeLeftDelete;
    private CashBoxAdapterSwipable adapter;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private int fromIndex = -1;
    private int toIndex = -1;
    @DrawableRes
    private int secondaryActionIcon;

    public CashBoxSwipeController(CashBoxAdapterSwipable adapter, boolean swipeLeftDelete,
                                  @DrawableRes int secondaryActionIcon) {
        this.adapter = adapter;
        this.swipeLeftDelete = swipeLeftDelete;
        this.secondaryActionIcon = secondaryActionIcon;
    }

    public CashBoxSwipeController(CashBoxAdapterSwipable adapter, SharedPreferences preferences,
                                  @DrawableRes int secondaryActionIcon) {
        this(adapter,preferences.getBoolean(KEY_SWIPE_LEFT_DELETE, true),secondaryActionIcon);
        setPreferenceChangeListener(preferences);
    }

    public CashBoxSwipeController(CashBoxAdapterSwipable adapter, boolean swipeLeftDelete) {
        this(adapter, swipeLeftDelete, R.drawable.ic_edit_white_24dp);
    }

    public CashBoxSwipeController(CashBoxAdapterSwipable adapter, SharedPreferences preferences) {
        this(adapter,preferences.getBoolean(KEY_SWIPE_LEFT_DELETE, true));
        setPreferenceChangeListener(preferences);
    }

    private void setPreferenceChangeListener(SharedPreferences preferences) {
        // Set up preference listener for swipe direction
        preferenceChangeListener = (sharedPreferences, s) -> {
            if (s.equals(KEY_SWIPE_LEFT_DELETE))
                setSwipeLeftDelete(sharedPreferences.getBoolean(KEY_SWIPE_LEFT_DELETE, true));
        };
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    public void setSwipeLeftDelete(boolean swipeLeftDelete) {
        this.swipeLeftDelete = swipeLeftDelete;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(UP | DOWN, LEFT | RIGHT);
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
        if (fromIndex == -1)   // fromIndex hasn't been set -> beginning of the move
            fromIndex = source.getAdapterPosition();
        toIndex = target.getAdapterPosition();

        LogUtil.debug(TAG, "Move from " + source.getAdapterPosition() + " to " + toIndex);
        adapter.onItemMove(source.getAdapterPosition(), toIndex);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == LEFT || direction == RIGHT) {
            //^ is an exclusive OR: (left and true) or (right and false)
            if (direction == RIGHT ^ swipeLeftDelete)
                adapter.onItemDelete(viewHolder.getAdapterPosition());
            else
                adapter.onItemSecondaryAction(viewHolder.getAdapterPosition());
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        if (actionState == ACTION_STATE_SWIPE) {
            Paint paint = new Paint();
            Drawable drawable;
            Rect rectTotal = new Rect(viewHolder.itemView.getLeft(), viewHolder.itemView.getTop(), viewHolder.itemView.getRight(), viewHolder.itemView.getBottom());

            // Choose Drawable and background color
            if ((dX < 0 && swipeLeftDelete) || (dX > 0 && !swipeLeftDelete)) {  // delete swipe
                drawable = recyclerView.getContext().getDrawable(R.drawable.delete);
                paint.setColor(Color.RED);
            } else {    // modify swipe
                drawable = recyclerView.getContext().getDrawable(secondaryActionIcon);
                paint.setColor(Color.BLUE);
            }
            if (Math.abs(dX) < rectTotal.width() * SWIPE_THRESHOLD)
                paint.setColor(Color.LTGRAY);

            // Draw
            if (drawable != null) {
                c.drawRect(rectTotal, paint);

                int size = drawable.getIntrinsicHeight() > rectTotal.height() ? rectTotal.height() : drawable.getIntrinsicHeight();
                int padding = (rectTotal.height() - size) / 2;
                if (dX > 0)    // icon on the left side when swiping right
                    drawable.setBounds(rectTotal.left + padding, rectTotal.top + padding, rectTotal.left + padding + size, rectTotal.bottom - padding);
                else    // icon on the right side when swiping left
                    drawable.setBounds(rectTotal.right - padding - size, rectTotal.top + padding, rectTotal.right - padding, rectTotal.bottom - padding);
                drawable.draw(c);
            }
        }
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return SWIPE_THRESHOLD;
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        LogUtil.debug(TAG, "View Cleared");

        if (fromIndex != -1 && toIndex != -1) { // if there has been a move motion
            adapter.onItemDrop(fromIndex, toIndex);
            LogUtil.debug(TAG, "Drop from " + fromIndex + " to " + toIndex);
        }
        fromIndex = -1;
        toIndex = -1;
    }
}
