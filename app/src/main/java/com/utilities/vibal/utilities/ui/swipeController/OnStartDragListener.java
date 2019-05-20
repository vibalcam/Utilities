package com.utilities.vibal.utilities.ui.swipeController;

import androidx.recyclerview.widget.RecyclerView;

public interface OnStartDragListener {
    /**
     * Called when a view is requesting a start of a drag.
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
}
