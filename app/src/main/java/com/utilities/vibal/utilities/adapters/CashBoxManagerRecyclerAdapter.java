package com.utilities.vibal.utilities.adapters;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.activities.CashBoxItemActivity;
import com.utilities.vibal.utilities.activities.CashBoxManagerActivity;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.models.CashBoxManager;
import com.utilities.vibal.utilities.util.Util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CashBoxManagerRecyclerAdapter extends RecyclerView.Adapter<CashBoxManagerRecyclerAdapter.ViewHolder> {
    public static final String STRING_EXTRA = "com.utilities.vibal.CashBoxIndex";
    public static final String CASHBOX_MANAGER_EXTRA = "com.utilities.vibal.CashBoxManager";

    private CashBoxManager cashBoxManager;
    private CashBoxManagerActivity cashBoxManagerActivity;
//    private RecyclerView recyclerView;
    private int selectedIndex;

    public CashBoxManagerRecyclerAdapter(CashBoxManager cashBoxManager, CashBoxManagerActivity cashBoxManagerActivity) {
        this.cashBoxManager = cashBoxManager;
        this.cashBoxManagerActivity = cashBoxManagerActivity;
        selectedIndex = -1;
    }

//    @Override
//    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
//        this.recyclerView = recyclerView;
//        super.onAttachedToRecyclerView(recyclerView);
//    }

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

    void changePosition(int oldPos, int newPos) {
        cashBoxManager.changePosition(oldPos,newPos);
        notifyDataSetChanged();
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
            getCashBoxManagerActivity().startActivity(intent);

            //Erase highlighting element
            selectedIndex =-1;
        }

        @Override
        public boolean onLongClick(View v) {
            //Highlight selected element
            selectItemHighlight();

            //Creating instance of PopupMenu
            PopupMenu popupMenu = new PopupMenu(getCashBoxManagerActivity(), v);
            //Inflating PopupMenu using xml file
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup_cash_box_manager_options, popupMenu.getMenu());
            //Registering Popup with onMenuItemClickListener()
            popupMenu.setOnMenuItemClickListener((MenuItem item) -> {
                switch (item.getItemId()) {
                    case R.id.popupDelete:
                        deleteCashBox(selectedIndex);
                        return true;

                    case R.id.popupChangeName:
                        showChangeNameDialog();
                        return true;

                    case R.id.popupClone:
                        showCloneDialog();
                        return true;

                    case R.id.popupShare:
                        return true;

                    default:
                        return false;
                }
            });
            popupMenu.show();
            return true;
        }

        void deleteCashBox(int deletedIndex) {
            CashBox deletedCashBox = cashBoxManager.remove(deletedIndex);
            notifyItemRemoved(deletedIndex);
            Snackbar.make(getCashBoxManagerActivity().getRecyclerView(),getCashBoxManagerActivity().getString(R.string.snackbarEntriesDeleted,1),Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, (View v1) -> {
                        cashBoxManager.add(deletedIndex,deletedCashBox);
                        notifyItemInserted(deletedIndex);
//                        cashBoxManager.saveDataTemp(getContext());
                        getCashBoxManagerActivity().saveCashBoxManager();
                    })
                    .show();
//            cashBoxManager.saveDataTemp(getContext());
            getCashBoxManagerActivity().saveCashBoxManager();
        }

//        void modifyCashBox(int modifiedIndex) {
//            CashBox cashBox = cashBoxManager.get(modifiedIndex);
//            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//            AlertDialog dialog = builder.setTitle(R.string.newEntry)
//                    .setView(R.layout.new_cash_box_input)  //use that view from folder layout
//                    .setNegativeButton(R.string.cancelDialog, null)
//                    .setPositiveButton(R.string.createCashBoxDialog, null)
//                    .create();
//            dialog.setCanceledOnTouchOutside(false);
//
//            dialog.setOnShowListener((DialogInterface dialog1) -> {
//                Button positive = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
//                TextInputEditText inputTextName = (TextInputEditText) ((AlertDialog) dialog1).findViewById(R.id.inputTextName);
//                TextInputLayout inputLayoutName = (TextInputLayout) ((AlertDialog) dialog1).findViewById(R.id.inputLayoutName);
//                TextInputEditText inputTextInitCash = (TextInputEditText) ((AlertDialog) dialog1).findViewById(R.id.inputTextInitCash);
//                TextInputLayout inputLayoutInitCash = (TextInputLayout) ((AlertDialog) dialog1).findViewById(R.id.inputLayoutInitCash);
//
//                Util.showKeyboard(getContext(),inputTextName);
//                inputTextName.setMaxLines(CashBox.MAX_LENGTH_NAME);
//                inputTextName.setText(cashBox.getName());
//                inputTextInitCash.setText(Double.toString(cashBox.getCash()));
//                positive.setOnClickListener(v -> {
//                    try{
//                        CashBox cashBox = new CashBox(inputTextName.getText().toString());
//                        String strInitCash = inputTextInitCash.getText().toString();
//                        if(!strInitCash.isEmpty() && Double.parseDouble(strInitCash)!=0)
//                            cashBox.add(Double.parseDouble(strInitCash),"Initial Amount", Calendar.getInstance());
//                        if(cashBoxManager.add(cashBox)) {
//                            notifyItemInserted(cashBoxManager.size()-1);
//                            dialog1.dismiss();
//                            cashBoxManager.saveDataTemp(getContext());
//                        } else
//                            inputLayoutName.setError(getContext().getString(R.string.nameInUse));
//                    } catch (NumberFormatException e){
//                        inputLayoutInitCash.setError("Not a valid number");
//                        inputTextInitCash.setText("");
//                    } catch (IllegalArgumentException e){
//                        inputLayoutName.setError(e.getMessage());
//                        inputTextName.setText(inputTextName.getText().toString().trim());
//                    }
//                });
//            });
//            dialog.show();
//        }

        private void showChangeNameDialog() {
            AlertDialog dialogChangeName = inputNameDialog("Change Name", R.string.cashBox_changeNameButton);
            dialogChangeName.setOnShowListener(dialog -> {
                Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextChangeName);
                TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutChangeName);
                String oldName = cashBoxManager.get(selectedIndex).getName();

                Util.showKeyboard(getCashBoxManagerActivity(), inputName);
                inputName.setMaxLines(CashBox.MAX_LENGTH_NAME);
                inputName.setText(oldName);
                layoutName.setCounterMaxLength(CashBox.MAX_LENGTH_NAME);

                positive.setOnClickListener((View v1) -> {
                    String newName = inputName.getText().toString();
                    if (newName.equalsIgnoreCase(oldName)) //if input name is the same, keep it how it is
                        dialog.dismiss();
                    else {
                        try {
                            if (cashBoxManager.changeName(selectedIndex, newName)) {
                                notifyItemChanged(selectedIndex);
                                dialog.dismiss();
//                                cashBoxManager.saveDataTemp(getContext());
                                getCashBoxManagerActivity().saveCashBoxManager();
                            } else
                                layoutName.setError(getCashBoxManagerActivity().getString(R.string.nameInUse));
                        } catch (IllegalArgumentException e) {
                            layoutName.setError(e.getMessage());
                        }
                    }
                });
            });
            dialogChangeName.show();
        }

        private void showCloneDialog() {
            AlertDialog dialogClone = inputNameDialog("Clone CashBox", R.string.cashBox_cloneButton);
            dialogClone.setOnShowListener(dialog -> {
                Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextChangeName);
                TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutChangeName);

                Util.showKeyboard(getCashBoxManagerActivity(), inputName);
                inputName.setMaxLines(CashBox.MAX_LENGTH_NAME);
                inputName.setText(cashBoxManager.get(selectedIndex).getName());
                layoutName.setCounterMaxLength(CashBox.MAX_LENGTH_NAME);

                positive.setOnClickListener((View v1) -> {
                    try {
                        if (cashBoxManager.duplicate(selectedIndex, inputName.getText().toString())) {
                            notifyItemInserted(selectedIndex + 1);
                            dialog.dismiss();
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

            Toast.makeText(getCashBoxManagerActivity(), "Entry cloned", Toast.LENGTH_SHORT).show();
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
    }
}
