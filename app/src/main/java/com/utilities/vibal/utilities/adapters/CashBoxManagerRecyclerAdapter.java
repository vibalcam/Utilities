package com.utilities.vibal.utilities.adapters;

import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.activities.CashBoxItemActivity;
import com.utilities.vibal.utilities.activities.CashBoxManagerActivity;
import com.utilities.vibal.utilities.interfaces.CashBoxAdapterSwipable;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.models.CashBoxManager;
import com.utilities.vibal.utilities.util.Util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CashBoxManagerRecyclerAdapter extends RecyclerView.Adapter<CashBoxManagerRecyclerAdapter.ViewHolder> implements CashBoxAdapterSwipable {
    public static final String STRING_EXTRA = "com.utilities.vibal.CashBoxIndex";
    public static final String CASHBOX_MANAGER_EXTRA = "com.utilities.vibal.CashBoxManager";
    public static final int REQUEST_CODE_ITEM = 1;

    private static final boolean SWIPE_ENABLED = true;
    private static final boolean DRAG_ENABLED = true;
    private static final String TAG = "PruebaManagerActivity";

    private CashBoxManager cashBoxManager;
    private CashBoxManagerActivity cashBoxManagerActivity;
    private int selectedIndex;

    public CashBoxManagerRecyclerAdapter(CashBoxManager cashBoxManager, CashBoxManagerActivity cashBoxManagerActivity) {
        this.cashBoxManager = cashBoxManager;
        this.cashBoxManagerActivity = cashBoxManagerActivity;
        selectedIndex = -1;
    }

    private CashBoxManagerActivity getCashBoxManagerActivity() {
        return cashBoxManagerActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_cash_box_manager, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int index) {
        CashBox cashBox = cashBoxManager.get(index);
        viewHolder.rvName.setText(cashBox.getName());
        viewHolder.rvAmount.setText(getCashBoxManagerActivity().getString(R.string.amountMoney, cashBox.getCash()));
        if (index == selectedIndex)
            viewHolder.rvItemLayout.setBackgroundColor(getCashBoxManagerActivity().getColor(R.color.colorRVSelectedCashBox));
        else
            viewHolder.rvItemLayout.setBackgroundColor(getCashBoxManagerActivity().getColor(R.color.colorRVBackgroundCashBox));
    }

    @Override
    public int getItemCount() {
        return cashBoxManager.size();
    }

    public void updateCashBoxManager(CashBoxManager cashBoxManager) {
        this.cashBoxManager = cashBoxManager;
        notifyDataSetChanged();
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
    public void onItemMove(int fromPosition, int toPosition) {
        Log.d(TAG, "onItemMove: ");
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDrop(int fromPosition, int toPosition) {
        Log.d(TAG, "onItemDrop: ");
        cashBoxManager.move(fromPosition, toPosition);
        getCashBoxManagerActivity().saveCashBoxManager();
    }

    @Override
    public void onItemDelete(int position) {
        CashBoxManagerActivity activity = getCashBoxManagerActivity();
        CashBox deletedCashBox = cashBoxManager.remove(position);
        notifyItemRemoved(position);

        Snackbar.make(activity.getRecyclerView(),activity.getString(R.string.snackbarEntriesDeleted,1),Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, (View v1) -> {
                    cashBoxManager.add(position,deletedCashBox);
                    notifyItemInserted(position);
                    activity.saveCashBoxManager();
                })
                .show();
        activity.saveCashBoxManager();
    }

    @Override
    public void onItemModify(int position) {
        showChangeNameDialog(position);
        notifyDataSetChanged();
    }

    private AlertDialog inputNameDialog(String title, int resPositiveButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getCashBoxManagerActivity());
        AlertDialog dialog = builder.setTitle(title)
                .setView(R.layout.cash_box_input_name)
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(resPositiveButton, null)
                .create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    private void showChangeNameDialog(int index) {
        CashBoxManagerActivity activity = getCashBoxManagerActivity();
        AlertDialog dialogChangeName = inputNameDialog("Change Name", R.string.cashBox_changeNameButton);

        dialogChangeName.setOnShowListener(dialog -> {
            Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextChangeName);
            TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutChangeName);
            String oldName = cashBoxManager.get(index).getName();

            Util.showKeyboard(activity, inputName);
            inputName.setMaxLines(CashBox.MAX_LENGTH_NAME);
            inputName.setText(oldName);
            layoutName.setCounterMaxLength(CashBox.MAX_LENGTH_NAME);

            positive.setOnClickListener((View v1) -> {
                String newName = inputName.getText().toString();
                if (newName.equalsIgnoreCase(oldName)) //if input name is the same, keep it how it is
                    dialog.dismiss();
                else {
                    try {
                        if (cashBoxManager.changeName(index, newName)) {
                            notifyItemChanged(index);
                            dialog.dismiss();
//                                cashBoxManager.saveDataTemp(getContext());
                            activity.saveCashBoxManager();
                        } else
                            layoutName.setError(activity.getString(R.string.nameInUse));
                    } catch (IllegalArgumentException e) {
                        layoutName.setError(e.getMessage());
                    }
                }
            });
        });
        dialogChangeName.show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @BindView(R.id.rvName)
        TextView rvName;
        @BindView(R.id.rvAmount)
        TextView rvAmount;
        @BindView(R.id.rvItemLayout)
        LinearLayout rvItemLayout;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        private void selectItemHighlight(){
            int temp = selectedIndex;
            selectedIndex = getAdapterPosition();
            notifyItemChanged(temp);
            notifyItemChanged(selectedIndex);
        }

        @Override
        public void onClick(View v) {
            //Highlight selected element
            selectItemHighlight();

//            CashBox cashBox = cashBoxManager.get(selectedIndex);
            Intent intent = new Intent(getCashBoxManagerActivity(), CashBoxItemActivity.class);
            intent.putExtra(STRING_EXTRA,selectedIndex);
            intent.putExtra(CASHBOX_MANAGER_EXTRA,cashBoxManager);
//            getCashBoxManagerActivity().startActivity(intent);
            getCashBoxManagerActivity().startActivityForResult(intent,REQUEST_CODE_ITEM);

            //Erase highlighting element
            selectedIndex =-1;
        }

        @Override
        public boolean onLongClick(View v) {
//            //Highlight selected element
//            selectItemHighlight();
//
//            //Creating instance of PopupMenu
//            PopupMenu popupMenu = new PopupMenu(getCashBoxManagerActivity(), v);
//            //Inflating PopupMenu using xml file
//            popupMenu.getMenuInflater().inflate(R.menu.menu_popup_cash_box_manager_options, popupMenu.getMenu());
//            //Registering Popup with onMenuItemClickListener()
//            popupMenu.setOnMenuItemClickListener((MenuItem item) -> {
//                switch (item.getItemId()) {
//                    case R.id.popupDelete:
////                        deleteCashBox(selectedIndex);
//                        onItemDelete(selectedIndex);
//                        return true;
//
//                    case R.id.popupChangeName:
//                        showChangeNameDialog(selectedIndex);
//                        return true;
//
//                    case R.id.popupClone:
//                        showCloneDialog(selectedIndex);
//                        return true;
//
//                    case R.id.popupShare:
//                        return true;
//
//                    default:
//                        return false;
//                }
//            });
//            popupMenu.show();
            return true;
        }

        private void showCloneDialog(int index) {
            AlertDialog dialogClone = inputNameDialog("Clone CashBox", R.string.cashBox_cloneButton);
            dialogClone.setOnShowListener(dialog -> {
                Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextChangeName);
                TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutChangeName);

                Util.showKeyboard(getCashBoxManagerActivity(), inputName);
                inputName.setMaxLines(CashBox.MAX_LENGTH_NAME);
                inputName.setText(cashBoxManager.get(index).getName());
                layoutName.setCounterMaxLength(CashBox.MAX_LENGTH_NAME);

                positive.setOnClickListener((View v1) -> {
                    try {
                        if (cashBoxManager.duplicate(index, inputName.getText().toString())) {
                            notifyItemInserted(index + 1);
                            dialog.dismiss();
                            Toast.makeText(getCashBoxManagerActivity(), "Entry cloned", Toast.LENGTH_SHORT).show();
//                            cashBoxManager.saveDataTemp(getContext());
                            getCashBoxManagerActivity().saveCashBoxManager();
                        } else
                            layoutName.setError(getCashBoxManagerActivity().getString(R.string.nameInUse));
                    } catch (IllegalArgumentException e) {
                        layoutName.setError(e.getMessage());
                    }
                });
            });
            dialogClone.show();
        }
    }
}
