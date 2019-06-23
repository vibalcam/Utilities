package com.utilities.vibal.utilities.ui.cashBoxItem;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.ui.swipeController.CashBoxAdapterSwipable;
import com.utilities.vibal.utilities.util.LogUtil;
import com.utilities.vibal.utilities.util.Util;

import java.text.DateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CashBoxItemRecyclerAdapter extends RecyclerView.Adapter<CashBoxItemRecyclerAdapter.ViewHolder> implements CashBoxAdapterSwipable {
    private static final boolean DRAG_ENABLED = false;
    private static final boolean SWIPE_ENABLED = true;
    private static final String TAG = "PruebaItemActivity";
    private final CashBoxItemActivity cashBoxItemActivity;
    private CashBox cashBox;
    private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

    CashBoxItemRecyclerAdapter(CashBox cashBox, CashBoxItemActivity activity) {
        this.cashBox = cashBox;
        this.cashBoxItemActivity = activity;
    }

    void updateCashBox(CashBox cashBox) {
        this.cashBox = cashBox;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cash_box_item_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int index) {
        CashBox.Entry entry = cashBox.getEntry(index);

        // Amount
        viewHolder.rvItemAmount.setText(cashBoxItemActivity.formatCurrency.format(entry.getAmount()));
        if(entry.getAmount()<0)
            viewHolder.rvItemAmount.setTextColor(cashBoxItemActivity.getColor(R.color.colorNegativeNumber));
        else
            viewHolder.rvItemAmount.setTextColor(cashBoxItemActivity.getColor(R.color.colorPositiveNumber));
        // Info
        if (entry.getInfo().isEmpty())
            viewHolder.rvItemInfo.setText(R.string.noInfoEntered);
        else
            viewHolder.rvItemInfo.setText(entry.getInfo());
        // Date
        viewHolder.rvItemDate.setText(dateFormat.format(entry.getDate().getTime()));
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
    public void onItemDelete(int position) {
        CashBox.Entry deletedEntry = cashBox.remove(position);
        notifyItemRemoved(position);
//        cashBoxItemActivity.updateCash();
        Snackbar.make(cashBoxItemActivity.rvCashBoxItem, cashBoxItemActivity.getString(R.string.snackbarEntriesDeleted, 1), Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, (View v) -> {
                    cashBox.add(position, deletedEntry);
                    notifyItemInserted(position);
//                    cashBoxItemActivity.updateCash();
//                    cashBoxItemActivity.saveCashBoxManager();
                    cashBoxItemActivity.notifyCashBoxChanged();
                })
                .show();
//        cashBoxItemActivity.saveCashBoxManager();
        cashBoxItemActivity.notifyCashBoxChanged();
    }

    @Override
    public void onItemModify(int position) {
        LogUtil.debug(TAG, "onItemModify: ");

        AlertDialog.Builder builder = new AlertDialog.Builder(cashBoxItemActivity);
        AlertDialog dialog = builder.setTitle(R.string.modifyEntry)
                .setView(R.layout.cash_box_item_entry_input)
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.confirm, null)
                .create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnShowListener((DialogInterface dialog1) -> {
            Button positive = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            TextInputEditText inputInfo = ((AlertDialog) dialog1).findViewById(R.id.inputTextInfo);
            TextInputEditText inputAmount = ((AlertDialog) dialog1).findViewById(R.id.inputTextAmount);
            TextInputLayout layoutAmount = ((AlertDialog) dialog1).findViewById(R.id.inputLayoutAmount);

            CashBox.Entry entry = cashBox.getEntry(position);
            inputInfo.setText(entry.getInfo());
            inputAmount.setText(String.format(Locale.getDefault(), "%.2f", entry.getAmount()));

            inputAmount.selectAll();
            Util.showKeyboard(cashBoxItemActivity, inputAmount);
            positive.setOnClickListener((View v) -> {
                try {
                    LogUtil.debug(TAG, "showAddDialog: cause" + (inputInfo.getText() == null) + (inputInfo.getText().toString().isEmpty()));
                    String input = inputAmount.getText().toString();
                    if (input.trim().isEmpty()) {
                        layoutAmount.setError(cashBoxItemActivity.getString(R.string.required));
                        inputAmount.setText("");
                        Util.showKeyboard(cashBoxItemActivity, inputAmount);
                    } else {
                        double amount = Util.parseDouble(inputAmount.getText().toString());
                        CashBox.Entry modifiedEntry = cashBox.modify(position, amount, inputInfo.getText().toString(), cashBox.getEntry(position).getDate());
//                        cashBoxItemActivity.updateCash();
                        notifyItemChanged(position);
                        dialog1.dismiss();
                        Snackbar.make(cashBoxItemActivity.rvCashBoxItem, R.string.snackbarEntryModified, Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, (View v1) -> {
                                    cashBox.modify(position, modifiedEntry);
//                                cashBoxItemActivity.updateCash();
                                    notifyItemChanged(position);
//                                cashBoxItemActivity.saveCashBoxManager();
                                    cashBoxItemActivity.notifyCashBoxChanged();
                                })
                                .show();
//                        cashBoxItemActivity.saveCashBoxManager();
                        cashBoxItemActivity.notifyCashBoxChanged();
                    }
                } catch (NumberFormatException e) {
                    layoutAmount.setError(cashBoxItemActivity.getString(R.string.errorMessageAmount));
                    inputAmount.selectAll();
                    Util.showKeyboard(cashBoxItemActivity, inputAmount);
                }
            });
        });
        dialog.show();

        notifyDataSetChanged();   // since the item is deleted from swipping we have to show it back again
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rvItemDate)
        TextView rvItemDate;
        @BindView(R.id.rvItemAmount)
        TextView rvItemAmount;
        @BindView(R.id.rvItemInfo)
        TextView rvItemInfo;

        ViewHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
