package com.vibal.utilities.ui.swipeController;

public interface CashBoxAdapterSwipable {
    // Drag functionality
    boolean isDragEnabled();

    default void onItemMove(int fromPosition, int toPosition) {
    }

    default void onItemDrop(int fromPosition, int toPosition) {
    }

    // Swipe functionality
    boolean isSwipeEnabled();

    default void onItemDelete(int position) {
    }

    default void onItemSecondaryAction(int position) {
    }
}
