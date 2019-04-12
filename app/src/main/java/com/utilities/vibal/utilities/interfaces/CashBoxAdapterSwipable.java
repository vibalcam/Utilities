package com.utilities.vibal.utilities.interfaces;

public interface CashBoxAdapterSwipable {
    boolean isDragEnabled();
    boolean isSwipeEnabled();
    void onItemMove(int fromPosition, int toPosition);
    void onItemDelete(int position);
    void onItemModify(int position);
}
