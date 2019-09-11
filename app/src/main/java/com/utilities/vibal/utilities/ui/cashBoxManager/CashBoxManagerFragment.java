package com.utilities.vibal.utilities.ui.cashBoxManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.db.CashBoxInfo;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.models.CashBoxViewModel;
import com.utilities.vibal.utilities.ui.settings.SettingsActivity;
import com.utilities.vibal.utilities.ui.swipeController.CashBoxAdapterSwipable;
import com.utilities.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.utilities.vibal.utilities.ui.swipeController.OnStartDragListener;
import com.utilities.vibal.utilities.util.DiffCallback;
import com.utilities.vibal.utilities.util.LogUtil;
import com.utilities.vibal.utilities.util.Util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.ACTION_ADD_CASHBOX;
import static com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.ACTION_DETAILS;
import static com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.EXTRA_ACTION;
import static com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.EXTRA_CASHBOX_ID;
import static com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.NO_ACTION;

public class CashBoxManagerFragment extends Fragment {
    private static final String TAG = "PruebaManagerFragment";

    @BindView(R.id.lyCBM) CoordinatorLayout coordinatorLayout;

    private CashBoxViewModel viewModel;
    private CashBoxManagerRecyclerAdapter adapter;

    static CashBoxManagerFragment newInstance() {
        return new CashBoxManagerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fragment has options menu
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cash_box_manager_fragment, container, false);
        ButterKnife.bind(this,view);

        // Set up RecyclerView
        RecyclerView rvCashBoxManager = view.findViewById(R.id.rvCashBoxManager);
        rvCashBoxManager.setHasFixedSize(true);
        rvCashBoxManager.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CashBoxManagerRecyclerAdapter();
        rvCashBoxManager.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new CashBoxSwipeController(adapter,
                PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()))
                        .getBoolean("swipeLeftDelete", true)));
        itemTouchHelper.attachToRecyclerView(rvCashBoxManager);
        adapter.setOnStartDragListener(itemTouchHelper::startDrag);

        LogUtil.debug(TAG, "onCreate: ");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) Objects.requireNonNull(getActivity());
        // Initialize data
        viewModel = new ViewModelProvider(activity).get(CashBoxViewModel.class);
        viewModel.getCashBoxesInfo().observe(getViewLifecycleOwner(), infoWithCashes ->
            adapter.submitList(infoWithCashes));

        //Set Toolbar as ActionBar
        activity.setSupportActionBar(getView().findViewById(R.id.toolbarCBManager));
        ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setTitle(R.string.titleCBM);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Look at intent
        doIntentAction();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_toolbar_cash_box_manager,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_manager_deleteAll:
                deleteAll();
                return true;
            case R.id.action_manager_help:
                Util.getHelpDialog(getContext(), R.string.cashBoxManager_helpTitle,
                        R.string.cashBoxManager_help).show();
                return true;
            case R.id.action_manager_edit:
                adapter.showActionMode();
                return true;
            case R.id.action_manager_settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doIntentAction() {
        Intent intent = getActivity().getIntent();
        int action = intent==null ? NO_ACTION : intent.getIntExtra(EXTRA_ACTION,NO_ACTION);

        if (action == ACTION_ADD_CASHBOX)
            showAddDialog();
        else if(action== ACTION_DETAILS)
            swapToItemFragment(intent.getIntExtra(EXTRA_CASHBOX_ID, CashBoxInfo.NO_CASHBOX));
    }

    private void swapToItemFragment(long cashBoxId) {
        LogUtil.debug(TAG,"Selected "+cashBoxId);
        if(cashBoxId==CashBoxInfo.NO_CASHBOX)
            return;

        viewModel.setCurrentCashBoxId(cashBoxId);
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_right,
                        R.anim.enter_from_right,R.anim.exit_to_right)
                .addToBackStack(null)
                .replace(R.id.container,CashBoxItemFragment.newInstance())
                .commit();
    }

    private void deleteAll() {
        if(adapter.getItemCount()==0) {
            Toast.makeText(getContext(), "No entries to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.confirmDeleteAllDialog)
                .setMessage("Are you sure you want to delete all entries? This action CANNOT be undone")
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.confirmDeleteDialogConfirm, (DialogInterface dialog, int which) ->
                        viewModel.addDisposable(viewModel.deleteAllCashBoxes()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> Toast.makeText(getContext(),
                                        "Deleted all entries", Toast.LENGTH_SHORT).show())))
                .show();
    }

    @OnClick(R.id.fabCBManager)
    void showAddDialog() {
        if (adapter.actionMode != null)
            adapter.actionMode.finish();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog dialog = builder.setTitle(R.string.newEntry)
                .setView(R.layout.cash_box_new_input)  //use that view from folder layout
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.createCashBoxDialog, null)
                .create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnShowListener(dialog1 -> {
            Button positive = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            TextInputEditText inputTextName = ((AlertDialog) dialog1).findViewById(R.id.inputTextName);
            TextInputLayout inputLayoutName = ((AlertDialog) dialog1).findViewById(R.id.inputLayoutName);
            TextInputEditText inputTextInitCash = ((AlertDialog) dialog1).findViewById(R.id.inputTextInitCash);
            TextInputLayout inputLayoutInitCash = ((AlertDialog) dialog1).findViewById(R.id.inputLayoutInitCash);

            Util.showKeyboard(getContext(), inputTextName);
            positive.setOnClickListener(v -> {
                inputLayoutInitCash.setError(null);
                inputLayoutName.setError(null);
                try {
                    CashBox cashBox = new CashBox(inputTextName.getText().toString());
                    String strInitCash = inputTextInitCash.getText().toString().trim();
                    if (!strInitCash.isEmpty()) {
                        double initCash = Util.parseExpression(strInitCash);
                        if (initCash != 0)
                            cashBox.getEntries().add(new CashBox.Entry(initCash,
                                    "Initial Amount", Calendar.getInstance()));
                    }

                    viewModel.addDisposable(viewModel.addCashBox(cashBox)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(dialog1::dismiss, throwable -> {
                                LogUtil.error(TAG,"Error in add", throwable);
                                inputLayoutName.setError(getContext().getString(R.string.nameInUse));
                                inputTextName.selectAll();
                                Util.showKeyboard(getContext(), inputTextName);
                            }));
                } catch (NumberFormatException e) {
                    LogUtil.error(TAG,"Error in add", e);
                    inputLayoutInitCash.setError(getContext().getString(R.string.errorMessageAmount));
                    inputTextInitCash.selectAll();
                    Util.showKeyboard(getContext(), inputTextInitCash);
                } catch (IllegalArgumentException e) {
                    LogUtil.error(TAG,"Error in add", e);
                    inputLayoutName.setError(e.getMessage());
                    inputTextName.selectAll();
                    Util.showKeyboard(getContext(), inputTextName);
                }
            });
        });
        dialog.show();
    }

    public class CashBoxManagerRecyclerAdapter extends RecyclerView.Adapter<CashBoxManagerRecyclerAdapter.ViewHolder> implements CashBoxAdapterSwipable {
        private static final boolean SWIPE_ENABLED = true;
        private static final String TAG = "PruebaManagerActivity";

        private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        private OnStartDragListener onStartDragListener;
//        private ShareActionProvider shareActionProvider;
        private CashBoxManagerRecyclerAdapter.ViewHolder selectedViewHolder = null;
        private List<CashBox.InfoWithCash> currentList = new ArrayList<>();

        // Contextual toolbar
        private ActionMode actionMode;
        private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_contextual_toolbar_cash_box_manager, menu);

                // Notify adapter to show images for dragging
                notifyItemRangeChanged(0, CashBoxManagerRecyclerAdapter.this.getItemCount());

//                // Set up ShareActionProvider
//                shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_manager_share));
//                shareActionProvider.setOnShareTargetSelectedListener((ShareActionProvider source, Intent intent) -> {
//                    mode.finish();
//                    return false;
//                });
//                if (selectedViewHolder != null)
//                    updateShareIntent(selectedViewHolder.getAdapterPosition());
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (selectedViewHolder == null) {
                    Toast.makeText(CashBoxManagerFragment.this.getContext(), "No item selected", Toast.LENGTH_SHORT).show();
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
//                shareActionProvider = null;
                setSelectedViewHolder(null);
                // Notify adapter to hide images for dragging
                notifyItemRangeChanged(0, CashBoxManagerRecyclerAdapter.this.getItemCount());
            }
        };

        void setOnStartDragListener(OnStartDragListener onStartDragListener) {
            this.onStartDragListener = onStartDragListener;
        }

        void submitList(@NonNull List<CashBox.InfoWithCash> newList) {
            LogUtil.debug(TAG,"New list submitted: " + newList.toString());
            viewModel.addDisposable(Single.just(DiffUtil.calculateDiff(
                    new DiffCallback<CashBox.InfoWithCash>(currentList,newList), false))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(diffResult -> {
                        LogUtil.debug(TAG,"DiffResult calculated");
                        currentList.clear();
                        currentList.addAll(newList);
                        diffResult.dispatchUpdatesTo(CashBoxManagerRecyclerAdapter.this);
                    }));
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cash_box_manager_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int index) {
            CashBox.InfoWithCash cashBoxInfo = currentList.get(index);
            viewHolder.rvName.setText(cashBoxInfo.getCashBoxInfo().getName());

            // Enable or disable dragging
            if (isDragEnabled()) {
                viewHolder.reorderImage.setVisibility(View.VISIBLE);
                viewHolder.rvAmount.setVisibility(View.GONE);
            } else {
                viewHolder.reorderImage.setVisibility(View.GONE);
                viewHolder.rvAmount.setVisibility(View.VISIBLE);
                viewHolder.rvAmount.setText(currencyFormat.format(cashBoxInfo.getCash()));
                if(cashBoxInfo.getCash()<0)
                    viewHolder.rvAmount.setTextColor(getActivity().getColor(R.color.colorNegativeNumber));
                else
                    viewHolder.rvAmount.setTextColor(getActivity().getColor(R.color.colorPositiveNumber));
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

        @Override
        public void onItemDrop(int fromPosition, int toPosition) {
            // In order for the animations to not occur, oldList and newList have to be the same
            CashBox.InfoWithCash infoWithCash = currentList.remove(fromPosition);
            currentList.add(toPosition,infoWithCash);
            viewModel.addDisposable(viewModel.moveCashBox(infoWithCash,toPosition)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe());
        }

        @Override
        public int getItemCount() {
            return currentList.size();
        }

        @Override
        public void onItemDelete(int position) {
            if (actionMode != null)
                actionMode.finish();
            CashBox.InfoWithCash deletedCashBoxInfo = currentList.remove(position);
            notifyItemRemoved(position);
            Snackbar.make(coordinatorLayout,
                    getString(R.string.snackbarEntriesDeleted, 1), Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, v -> {
                        currentList.add(position,deletedCashBoxInfo);
                        notifyItemInserted(position);
                    })
                    .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);

                            LogUtil.debug(TAG, "Delete CashBox");
                            if (event != DISMISS_EVENT_ACTION)
                                viewModel.addDisposable(viewModel.deleteCashBoxInfo(deletedCashBoxInfo)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe());
                        }
                    }).show();
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
                CashBox.InfoWithCash infoWithCash = currentList.get(position);

                inputName.setText(infoWithCash.getCashBoxInfo().getName());
                layoutName.setCounterMaxLength(CashBoxInfo.MAX_LENGTH_NAME);
                // Show keyboard and select the whole text
                inputName.selectAll();
                Util.showKeyboard(getContext(), inputName);

                positive.setOnClickListener((View v1) -> {
                    String newName = inputName.getText().toString();
                    try {
                        viewModel.addDisposable(
                                viewModel.changeCashBoxName(infoWithCash, newName)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(dialog::dismiss,throwable -> {
                                            layoutName.setError(getString(R.string.nameInUse));
                                            inputName.selectAll();
                                            Util.showKeyboard(getContext(), inputName);
                                        }));
                    } catch (IllegalArgumentException e) {
                        layoutName.setError(e.getMessage());
                        inputName.selectAll();
                        Util.showKeyboard(getContext(), inputName);
                    }
                });
            });
            dialogChangeName.show();
            notifyItemChanged(position); // since the item is deleted from swipping we have to show it back again
        }

        private AlertDialog inputNameDialog(String title, int resPositiveButton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            AlertDialog dialog = builder.setTitle(title)
                    .setView(R.layout.cash_box_input_name)
                    .setNegativeButton(R.string.cancelDialog, null)
                    .setPositiveButton(resPositiveButton, null)
                    .create();
            dialog.setCanceledOnTouchOutside(false);

            return dialog;
        }

        private void showCloneDialog(int position) {
            CashBoxInfo cashBoxInfo = currentList.get(position).getCashBoxInfo();

            AlertDialog dialogClone = inputNameDialog("Clone CashBox", R.string.cashBox_cloneButton);
            dialogClone.setOnShowListener(dialog -> {
                Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextChangeName);
                TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutChangeName);

                Util.showKeyboard(getContext(), inputName);
                inputName.setMaxLines(CashBoxInfo.MAX_LENGTH_NAME);
                inputName.setText(cashBoxInfo.getName());
                layoutName.setCounterMaxLength(CashBoxInfo.MAX_LENGTH_NAME);

                positive.setOnClickListener((View v1) ->
                    viewModel.addDisposable(viewModel.duplicateCashBox(cashBoxInfo.getId(),
                            inputName.getText().toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                dialog.dismiss();
                                Toast.makeText(getContext(), "Entry Cloned",
                                        Toast.LENGTH_SHORT).show();
                            },throwable -> {
                                LogUtil.error(TAG,"Clone: ",throwable);
                                if(throwable instanceof SQLiteConstraintException)
                                    layoutName.setError("Name in use");
                                else
                                    layoutName.setError(throwable.getMessage());
                                inputName.selectAll();
                                Util.showKeyboard(getContext(), inputName);
                            })));
            });
            dialogClone.show();
        }

        private void setSelectedViewHolder(CashBoxManagerRecyclerAdapter.ViewHolder viewHolder) {
            if (selectedViewHolder != null)
                selectedViewHolder.itemView.setBackgroundResource(R.color.colorRVBackgroundCashBox);
            if (viewHolder != null) {
                viewHolder.itemView.setBackgroundResource(R.color.colorRVSelectedCashBox);
//                updateShareIntent(viewHolder.getAdapterPosition());
            }
            selectedViewHolder = viewHolder;
        }

//        private void updateShareIntent(int index) {
//            if (shareActionProvider != null)
//                shareActionProvider.setShareIntent(Util.getShareIntent(getItem(index)));
//        }

        boolean showActionMode() {
            if (actionMode != null)
                return false;
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
            return true;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            @BindView(R.id.rvName) TextView rvName;
            @BindView(R.id.rvAmount) TextView rvAmount;
            @BindView(R.id.rvItemLayout) LinearLayout rvItemLayout;
            @BindView(R.id.reorderImage) ImageView reorderImage;

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
                    swapToItemFragment(currentList.get(getAdapterPosition()).getCashBoxInfo().getId());

                    //Erase highlighting element
                    setSelectedViewHolder(null);
                }
            }

            @Override
            public boolean onLongClick(View v) {
                setSelectedViewHolder(this);
                showActionMode();
                return true;
            }
        }
    }
}
