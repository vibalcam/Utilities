package com.utilities.vibal.utilities.ui.cashBoxManager;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.ui.cashBoxItem.CashBoxItemActivity;
import com.utilities.vibal.utilities.ui.swipeController.CashBoxAdapterSwipable;
import com.utilities.vibal.utilities.ui.swipeController.OnStartDragListener;
import com.utilities.vibal.utilities.util.Util;

import java.text.NumberFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;

public class CashBoxManagerRecyclerAdapter extends ListAdapter<CashBox, CashBoxManagerRecyclerAdapter.ViewHolder> implements CashBoxAdapterSwipable {
    static final int REQUEST_CODE_ITEM = 1;

    private static final boolean SWIPE_ENABLED = true;
    private static final String TAG = "PruebaManagerActivity";

    private final CashBoxManagerActivity cashBoxManagerActivity;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    private OnStartDragListener onStartDragListener;
    private ShareActionProvider shareActionProvider;
    private ViewHolder selectedViewHolder = null;

    // Contextual toolbar
    ActionMode actionMode;
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_contextual_toolbar_cash_box_manager, menu);

            // Notify adapter to show images for dragging
            notifyItemRangeChanged(0, CashBoxManagerRecyclerAdapter.this.getItemCount());

            // Set up ShareActionProvider
            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_manager_share));
            shareActionProvider.setOnShareTargetSelectedListener((ShareActionProvider source, Intent intent) -> {
                mode.finish();
                return false;
            });
            if (selectedViewHolder != null)
                updateShareIntent(selectedViewHolder.getAdapterPosition());
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (selectedViewHolder == null) {
                Toast.makeText(cashBoxManagerActivity, "No item selected", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.action_manager_duplicate) {
                showCloneDialog(selectedViewHolder.getAdapterPosition());
                mode.finish();
                return true;
            } else
                return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            shareActionProvider = null;
            setSelectedViewHolder(null);
            // Notify adapter to hide images for dragging
            notifyItemRangeChanged(0, CashBoxManagerRecyclerAdapter.this.getItemCount());
        }
    };

    //DiffUtil Callback
    private static final DiffUtil.ItemCallback<CashBox> DIFF_CALLBACK = new DiffUtil.ItemCallback<CashBox>() {
        @Override
        public boolean areItemsTheSame(@NonNull CashBox oldItem, @NonNull CashBox newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull CashBox oldItem, @NonNull CashBox newItem) {
            return oldItem.getCash() == newItem.getCash();
        }
    };

    CashBoxManagerRecyclerAdapter(CashBoxManagerActivity cashBoxManagerActivity) {
        super(DIFF_CALLBACK);
        this.cashBoxManagerActivity = cashBoxManagerActivity;
    }

    void setOnStartDragListener(OnStartDragListener onStartDragListener) {
        this.onStartDragListener = onStartDragListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_cash_box_manager, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int index) {
        CashBox cashBox = getItem(index);
        viewHolder.rvName.setText(cashBox.getName());

        // Enable or disable dragging
        if (isDragEnabled()) {
            viewHolder.reorderImage.setVisibility(View.VISIBLE);
            viewHolder.rvAmount.setVisibility(View.GONE);
        } else {
            viewHolder.reorderImage.setVisibility(View.GONE);
            viewHolder.rvAmount.setVisibility(View.VISIBLE);
            viewHolder.rvAmount.setText(currencyFormat.format(cashBox.getCash()));
            if(cashBox.getCash()<0)
                viewHolder.rvAmount.setTextColor(cashBoxManagerActivity.getColor(R.color.colorNegativeNumber));
            else
                viewHolder.rvAmount.setTextColor(cashBoxManagerActivity.getColor(R.color.colorPositiveNumber));
        }

        // Update selected ViewHolder
        if (selectedViewHolder != null && index == selectedViewHolder.getAdapterPosition()) {
            setSelectedViewHolder(viewHolder);
        }
    }

    @Override
    public boolean isDragEnabled() {
        return actionMode != null;
    }

    @Override
    public boolean isSwipeEnabled() {
        return SWIPE_ENABLED;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
    }

    //TODO
    @Override
    public void onItemDrop(int fromPosition, int toPosition) {
//        cashBoxManagerActivity.cashBoxViewModel.moveCashBox();

//        cashBoxes.move(fromPosition, toPosition);
    }

    @Override
    public void onItemDelete(int position) {
        if (actionMode != null)
            actionMode.finish();
//        CashBox deletedCashBox = cashBoxes.remove(position);
//        notifyItemRemoved(position);
//
//        Snackbar.make(cashBoxManagerActivity.coordinatorLayout, cashBoxManagerActivity.getString(R.string.snackbarEntriesDeleted, 1), Snackbar.LENGTH_LONG)
//                .setAction(R.string.undo, (View v1) -> {
//                    cashBoxes.add(position, deletedCashBox);
//                    notifyItemInserted(position);
//                    cashBoxManagerActivity.saveCashBoxManager();
//                })
//                .show();
//        cashBoxManagerActivity.saveCashBoxManager();
        CashBox deletedCashBox = getItem(position);
        cashBoxManagerActivity.cashBoxViewModel.deleteCashBox(deletedCashBox);

        Snackbar.make(cashBoxManagerActivity.coordinatorLayout, cashBoxManagerActivity.getString(R.string.snackbarEntriesDeleted, 1), Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, (View v1) -> {
                    cashBoxManagerActivity.cashBoxViewModel.addCashBox(deletedCashBox);
                })
                .show();
    }

    @Override
    public void onItemModify(int position) {
        if (actionMode != null)
            actionMode.finish();

        AlertDialog dialogChangeName = inputNameDialog("Change Name", R.string.cashBox_changeNameButton);
        dialogChangeName.setOnShowListener(dialog -> {
            Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextChangeName);
            TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutChangeName);
            String oldName = getItem(position).getName();

            inputName.setText(oldName);
            layoutName.setCounterMaxLength(CashBox.MAX_LENGTH_NAME);
            inputName.setSelectAllOnFocus(true);

            // Show keyboard and select the whole text
            inputName.selectAll();
            Util.showKeyboard(cashBoxManagerActivity, inputName);

            positive.setOnClickListener((View v1) -> {
                String newName = inputName.getText().toString();
                try {
//                    if (cashBoxes.changeName(position, newName)) { TODO
                    if(cashBoxManagerActivity.cashBoxViewModel.changeCashBoxName(getItem(position),newName)) {
//                        notifyItemChanged(position);
                        dialog.dismiss();
                    } else {
                        layoutName.setError(cashBoxManagerActivity.getString(R.string.nameInUse));
                        inputName.selectAll();
                        Util.showKeyboard(cashBoxManagerActivity, inputName);
                    }
                } catch (IllegalArgumentException e) {
                    layoutName.setError(e.getMessage());
                    inputName.selectAll();
                    Util.showKeyboard(cashBoxManagerActivity, inputName);
                }
            });
        });
        dialogChangeName.show();

        notifyDataSetChanged(); // since the item is deleted from swipping we have to show it back again
    }

    private AlertDialog inputNameDialog(String title, int resPositiveButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cashBoxManagerActivity);
        AlertDialog dialog = builder.setTitle(title)
                .setView(R.layout.cash_box_input_name)
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(resPositiveButton, null)
                .create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    private void showCloneDialog(int index) {
        AlertDialog dialogClone = inputNameDialog("Clone CashBox", R.string.cashBox_cloneButton);
        dialogClone.setOnShowListener(dialog -> {
            Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextChangeName);
            TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutChangeName);

            Util.showKeyboard(cashBoxManagerActivity, inputName);
            inputName.setMaxLines(CashBox.MAX_LENGTH_NAME);
            inputName.setText(getItem(index).getName());
            layoutName.setCounterMaxLength(CashBox.MAX_LENGTH_NAME);

            positive.setOnClickListener((View v1) -> {
                try {
//                    if (cashBoxes.duplicate(index, inputName.getText().toString())) { TODO
                    if (cashBoxManagerActivity.cashBoxViewModel.duplicateCashBox(getItem(index),inputName.getText().toString())) {
//                        notifyItemInserted(index + 1);
                        dialog.dismiss();
                        Toast.makeText(cashBoxManagerActivity, "Entry cloned", Toast.LENGTH_SHORT).show();
                    } else {
                        layoutName.setError(cashBoxManagerActivity.getString(R.string.nameInUse));
                        inputName.selectAll();
                        Util.showKeyboard(cashBoxManagerActivity, inputName);
                    }
                } catch (IllegalArgumentException e) {
                    layoutName.setError(e.getMessage());
                    inputName.selectAll();
                    Util.showKeyboard(cashBoxManagerActivity, inputName);
                }
            });
        });
        dialogClone.show();
    }

    private void setSelectedViewHolder(ViewHolder viewHolder) {
        if (selectedViewHolder != null)
            selectedViewHolder.itemView.setBackgroundResource(R.color.colorRVBackgroundCashBox);
        if (viewHolder != null) {
            viewHolder.itemView.setBackgroundResource(R.color.colorRVSelectedCashBox);
            updateShareIntent(viewHolder.getAdapterPosition());
        }
        selectedViewHolder = viewHolder;
    }

    private void updateShareIntent(int index) {
        if (shareActionProvider != null)
            shareActionProvider.setShareIntent(Util.getShareIntent(getItem(index)));
    }

    boolean showActionMode() {
        if (actionMode != null)
            return false;
        else {
            actionMode = cashBoxManagerActivity.startSupportActionMode(actionModeCallback);
            return true;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @BindView(R.id.rvName)
        TextView rvName;
        @BindView(R.id.rvAmount)
        TextView rvAmount;
        @BindView(R.id.rvItemLayout)
        LinearLayout rvItemLayout;
        @BindView(R.id.reorderImage)
        ImageView reorderImage;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @OnTouch(R.id.reorderImage)
        boolean onTouch(MotionEvent event) {
            setSelectedViewHolder(this);
            if (onStartDragListener != null && event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                onStartDragListener.onStartDrag(this);
                return true;
            } else
                return false;
        }

        @Override
        public void onClick(View v) {
            //Highlight selected element
            setSelectedViewHolder(this);

            if (actionMode == null) {
//            CashBox cashBox = cashBoxes.get(selectedIndex);
                Intent intent = new Intent(cashBoxManagerActivity, CashBoxItemActivity.class);
                intent.putExtra(CashBoxItemActivity.EXTRA_INDEX, selectedViewHolder.getAdapterPosition());
//                intent.putExtra(CashBoxItemActivity.EXTRA_CASHBOX_MANAGER, (Parcelable) cashBoxes);
//            cashBoxManagerActivity.startActivity(intent);
                cashBoxManagerActivity.startActivityForResult(intent, REQUEST_CODE_ITEM);

                //Erase highlighting element
                setSelectedViewHolder(null);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            setSelectedViewHolder(this);
            return showActionMode();
        }
    }
}
