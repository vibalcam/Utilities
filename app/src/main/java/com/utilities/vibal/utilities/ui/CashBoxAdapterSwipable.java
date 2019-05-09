package com.utilities.vibal.utilities.ui;

public interface CashBoxAdapterSwipable {
    boolean isDragEnabled();
    boolean isSwipeEnabled();
    void onItemMove(int fromPosition, int toPosition);
    void onItemDelete(int position);
    void onItemModify(int position);
    void onItemDrop(int fromPosition, int toPosition);
}
