package com.vibal.utilities.ui.cashBoxManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.vibal.utilities.R;
import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.ui.settings.SettingsActivity;
import com.vibal.utilities.ui.swipeController.CashBoxAdapterSwipable;
import com.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.vibal.utilities.viewModels.CashBoxDeletedViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CashBoxDeletedActivity extends AppCompatActivity { //todo to fragment
    private final DiffUtil.ItemCallback<CashBox.InfoWithCash> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CashBox.InfoWithCash>() {
                @Override
                public boolean areItemsTheSame(@NonNull CashBox.InfoWithCash oldItem,
                                               @NonNull CashBox.InfoWithCash newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull CashBox.InfoWithCash oldItem,
                                                  @NonNull CashBox.InfoWithCash newItem) {
                    return oldItem.getCash() == newItem.getCash() &&
                            oldItem.getCashBoxInfo().getName().
                                    equals(newItem.getCashBoxInfo().getName());
                }
            };

    private CashBoxDeletedRecyclerAdapter adapter;
    private CashBoxDeletedViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cash_box_deleted_activity);
        ButterKnife.bind(this);

        // Set ViewModel
        viewModel = new ViewModelProvider(this).get(CashBoxDeletedViewModel.class);
        viewModel.getCashBoxesInfo().observe(this, infoWithCashes -> adapter.submitList(infoWithCashes));

        // Set Toolbar as ActionBar
        setSupportActionBar(findViewById(R.id.toolbarCBDeleted));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_deletedCashBox);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set up RecyclerView
        RecyclerView rvDeleted = findViewById(R.id.rvCashBoxDeleted);
        rvDeleted.setHasFixedSize(true);
        rvDeleted.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CashBoxDeletedRecyclerAdapter();
        rvDeleted.setAdapter(adapter);
        new ItemTouchHelper(new CashBoxSwipeController(adapter,
                PreferenceManager.getDefaultSharedPreferences(this),
                R.drawable.ic_restore_white_24dp))
                .attachToRecyclerView(rvDeleted);
//        new ItemTouchHelper(new CashBoxSwipeController(adapter,
//                PreferenceManager.getDefaultSharedPreferences(this)
//                        .getBoolean("swipeLeftDelete", true),
//                R.drawable.ic_restore_white_24dp))
//                .attachToRecyclerView(rvDeleted);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_toolbar_cash_box_deleted, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_deleted_deleteAll:
                deleteAll();
                return true;
            case R.id.action_deleted_restoreAll:
                restoreAll();
                return true;
            case R.id.action_deleted_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAll() {
        int count = adapter.getItemCount();
        if (count == 0) {
            Toast.makeText(this, "No entries to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirmDeleteAllDialog)
                .setMessage("Are you sure you want to delete all entries? This action CANNOT be undone")
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.confirmDeleteDialogConfirm, (DialogInterface dialog, int which) ->
                        viewModel.addDisposable(viewModel.clearRecycleBin()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> Toast.makeText(this,
                                        getString(R.string.snackbarEntriesDeleted, count),
                                        Toast.LENGTH_SHORT)
                                        .show())))
                .show();
    }

    private void restoreAll() {
        int count = adapter.getItemCount();
        if (count == 0) {
            Toast.makeText(this, "No entries to restore", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirmRestoreAllDialog)
                .setMessage("Are you sure you want to restore all CashBoxes?")
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.confirm, (DialogInterface dialog, int which) ->
                        viewModel.addDisposable(viewModel.restoreAll()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> Toast.makeText(this,
                                        getString(R.string.snackbarEntriesRestored, count),
                                        Toast.LENGTH_SHORT)
                                        .show())))
                .show();
    }

    public class CashBoxDeletedRecyclerAdapter
            extends ListAdapter<CashBox.InfoWithCash, CashBoxDeletedRecyclerAdapter.ViewHolder>
            implements CashBoxAdapterSwipable {
        private static final boolean SWIPE_ENABLED = true;
        private static final boolean DRAG_ENABLED = false;

        public CashBoxDeletedRecyclerAdapter() {
            super(DIFF_CALLBACK);
        }

        @NonNull
        @Override
        public CashBoxDeletedRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cash_box_deleted_item,
                    parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CashBoxDeletedRecyclerAdapter.ViewHolder viewHolder, int position) {
            CashBox.InfoWithCash infoWithCash = getItem(position);
            viewHolder.rvName.setText(infoWithCash.getCashBoxInfo().getName());
            int colorRes = infoWithCash.getCash() < 0 ? R.color.colorNegativeNumber : R.color.colorPositiveNumber;
            viewHolder.rvAmount.setTextColor(getColor(colorRes));
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
            viewModel.addDisposable(viewModel.delete(getItem(position))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Toast.makeText(CashBoxDeletedActivity.this,
                            getString(R.string.snackbarEntriesDeleted, 1), Toast.LENGTH_SHORT)
                            .show()));
        }

        @Override
        public void onItemSecondaryAction(int position) {
            viewModel.addDisposable(viewModel.restore(getItem(position))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Toast.makeText(CashBoxDeletedActivity.this,
                            getString(R.string.snackbarEntriesRestored, 1), Toast.LENGTH_SHORT)
                            .show()));
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.rvName)
            TextView rvName;
            @BindView(R.id.rvAmount)
            TextView rvAmount;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
