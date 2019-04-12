package com.utilities.vibal.utilities.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.activities.CashBoxItemActivity;
import com.utilities.vibal.utilities.interfaces.CashBoxAdapterSwipable;
import com.utilities.vibal.utilities.models.CashBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CashBoxItemRecyclerAdapter extends RecyclerView.Adapter<CashBoxItemRecyclerAdapter.ViewHolder> implements CashBoxAdapterSwipable {
    private static final boolean DRAG_ENABLED = false;
    private static final boolean SWIPE_ENABLED = true;

    private CashBox cashBox;
    private CashBoxItemActivity activity;
    private java.text.DateFormat dateFormat;

    public CashBoxItemRecyclerAdapter(CashBox cashBox, CashBoxItemActivity activity) {
        this.cashBox = cashBox;
        this.activity = activity;
        dateFormat = java.text.DateFormat.getDateInstance();
    }

    private CashBoxItemActivity getCashBoxItemActivity(){
        return activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_cash_box_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int index) {
        viewHolder.rvItemAmount.setText(getCashBoxItemActivity().getString(R.string.amountMoney,cashBox.getAmount(index)));
        viewHolder.rvItemInfo.setText(cashBox.getInfo(index));
        viewHolder.rvItemDate.setText(dateFormat.format(cashBox.getDate(index).getTime()));
    }

    @Override
    public int getItemCount() {
        return cashBox.sizeEntries();
    }

    @Override
    public boolean isDragEnabled() {
        return DRAG_ENABLED;
    }

    @Override
    public boolean isSwipeEnabled() {
        return SWIPE_ENABLED;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {}

    @Override
    public void onItemDelete(int position) {
        CashBox.Entry deletedEntry = cashBox.remove(position);
        notifyItemRemoved(position);
        Snackbar.make(getCashBoxItemActivity().getRecyclerView(),getCashBoxItemActivity().getString(R.string.snackbarEntriesDeleted,1),Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, (View v) -> {
                    cashBox.add(position,deletedEntry);
                    notifyItemInserted(position);
                    getCashBoxItemActivity().saveCashBoxManager();
                })
                .show();
        getCashBoxItemActivity().saveCashBoxManager();
    }

    @Override
    public void onItemModify(int position) {
//        int modifiedIndex = viewHolder.getAdapterPosition();
//        CashBox.Entry modifiedEntry = cashBox.modify();
//        notifyItemRemoved(modifiedIndex);
//        Snackbar.make(getCashBoxItemActivity().getRecyclerView(),getCashBoxItemActivity().getString(R.string.snackbarEntriesDeleted,1),Snackbar.LENGTH_LONG)
//                .setAction(R.string.undo, (View v) -> {
//                    cashBox.add(modifiedIndex,modifiedEntry);
//                    notifyItemInserted(modifiedIndex);
//                    getCashBoxItemActivity().saveCashBoxManager();
//                })
//                .show();
//        getCashBoxItemActivity().saveCashBoxManager();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rvItemDate)
        TextView rvItemDate;
        @BindView(R.id.rvItemAmount)
        TextView rvItemAmount;
        @BindView(R.id.rvItemInfo)
        TextView rvItemInfo;

        public ViewHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this,view);
        }

//        void deleteCashBoxEntry() {
//            int deletedIndex = getAdapterPosition();
//            CashBox.Entry deletedEntry = cashBox.remove(deletedIndex);
//            notifyItemRemoved(deletedIndex);
//            Snackbar.make(getCashBoxItemActivity().getRecyclerView(),getCashBoxItemActivity().getString(R.string.snackbarEntriesDeleted,1),Snackbar.LENGTH_LONG)
//                    .setAction(R.string.undo, (View v) -> {
//                        cashBox.add(deletedIndex,deletedEntry);
//                        notifyItemInserted(deletedIndex);
//                        getCashBoxItemActivity().saveCashBoxManager();
//                    })
//                    .show();
//            getCashBoxItemActivity().saveCashBoxManager();
//        }
    }
}
