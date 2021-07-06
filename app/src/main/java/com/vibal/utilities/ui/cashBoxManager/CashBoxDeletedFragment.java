package com.vibal.utilities.ui.cashBoxManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.vibal.utilities.R;
import com.vibal.utilities.databinding.CashBoxDeletedItemBinding;
import com.vibal.utilities.models.InfoWithCash;
import com.vibal.utilities.ui.settings.SettingsActivity;
import com.vibal.utilities.ui.swipeController.CashBoxAdapterSwipable;
import com.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.vibal.utilities.ui.viewPager.PagerFragment;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.MyDialogBuilder;
import com.vibal.utilities.viewModels.CashBoxDeletedViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CashBoxDeletedFragment extends PagerFragment {
    private final DiffUtil.ItemCallback<InfoWithCash> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<InfoWithCash>() {
                @Override
                public boolean areItemsTheSame(@NonNull InfoWithCash oldItem,
                                               @NonNull InfoWithCash newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull InfoWithCash oldItem,
                                                  @NonNull InfoWithCash newItem) {
                    return oldItem.getCash() == newItem.getCash() &&
                            oldItem.getCashBoxInfo().getName().
                                    equals(newItem.getCashBoxInfo().getName());
                }
            };

    private CashBoxDeletedRecyclerAdapter adapter;
    private CashBoxDeletedViewModel viewModel;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    static CashBoxDeletedFragment newInstance(int pagerPosition) {
        CashBoxDeletedFragment fragment = new CashBoxDeletedFragment();
        fragment.setPositionAsArgument(pagerPosition);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fragment has options menu
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cash_box_deleted_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up RecyclerView
        RecyclerView rvDeleted = view.findViewById(R.id.rvCashBoxDeleted);
        rvDeleted.setHasFixedSize(true);
        rvDeleted.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CashBoxDeletedRecyclerAdapter();
        rvDeleted.setAdapter(adapter);
        new ItemTouchHelper(new CashBoxSwipeController(adapter,
                PreferenceManager.getDefaultSharedPreferences(requireContext()),
                R.drawable.ic_restore_white_24dp))
                .attachToRecyclerView(rvDeleted);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set ViewModel
        viewModel = new ViewModelProvider(this).get(CashBoxDeletedViewModel.class);
        viewModel.getCashBoxesInfo().observe(getViewLifecycleOwner(), infoWithCashes ->
                adapter.submitList(infoWithCashes));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (!isOptionsMenuActive())
            return;
        super.onCreateOptionsMenu(menu, inflater);

        LogUtil.debug("PruebaOptionsMenu", "Deleted");
        menu.clear();
        inflater.inflate(R.menu.menu_toolbar_cash_box_deleted, menu);

        // Set Toolbar title
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(R.string.title_deletedCashBox);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!isOptionsMenuActive())
            return false;

        switch (item.getItemId()) {
//            case android.R.id.home:
//                onBackPressed();
//                return true;
            case R.id.action_deleted_deleteAll:
                deleteAll();
                return true;
            case R.id.action_deleted_restoreAll:
                restoreAll();
                return true;
            case R.id.action_deleted_settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAll() {
        int count = adapter.getItemCount();
        if (count == 0) {
            Toast.makeText(getContext(), "No entries to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        new MyDialogBuilder(requireContext())
                .setTitle(R.string.confirmDeleteAllDialog)
                .setMessage("Are you sure you want to delete all entries? This action CANNOT be undone")
                .setPositiveButton(R.string.confirmDeleteDialogConfirm, (DialogInterface dialog, int which) ->
                        compositeDisposable.add(viewModel.clearRecycleBin()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> Toast.makeText(getContext(),
                                        getString(R.string.snackbarEntriesDeleted, count),
                                        Toast.LENGTH_SHORT)
                                        .show())))
                .show();
    }

    private void restoreAll() {
        int count = adapter.getItemCount();
        if (count == 0) {
            Toast.makeText(getContext(), "No entries to restore", Toast.LENGTH_SHORT).show();
            return;
        }
        new MyDialogBuilder(requireContext())
                .setTitle(R.string.confirmRestoreAllDialog)
                .setMessage("Are you sure you want to restore all CashBoxes?")
                .setPositiveButton(R.string.confirm, (DialogInterface dialog, int which) ->
                        compositeDisposable.add(viewModel.restoreAll()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> Toast.makeText(getContext(),
                                        getString(R.string.snackbarEntriesRestored, count),
                                        Toast.LENGTH_SHORT)
                                        .show())))
                .show();
    }

    public class CashBoxDeletedRecyclerAdapter
            extends ListAdapter<InfoWithCash, CashBoxDeletedRecyclerAdapter.ViewHolder>
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
            InfoWithCash infoWithCash = getItem(position);
            viewHolder.binding.rvName.setText(infoWithCash.getCashBoxInfo().getName());
            int colorRes = infoWithCash.getCash() < 0 ? R.color.colorNegativeNumber : R.color.colorNeutralNumber;
            viewHolder.binding.rvAmount.setTextColor(getContext().getColor(colorRes));
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
            compositeDisposable.add(viewModel.delete(getItem(position))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Toast.makeText(getContext(),
                            getString(R.string.snackbarEntriesDeleted, 1), Toast.LENGTH_SHORT)
                            .show()));
        }

        @Override
        public void enableAllSwipe(boolean enable) {
            getPagerActivity().getViewPager2().setUserInputEnabled(enable);
        }

        @Override
        public void onItemSecondaryAction(int position) {
            compositeDisposable.add(viewModel.restore(getItem(position))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Toast.makeText(getContext(),
                            getString(R.string.snackbarEntriesRestored, 1), Toast.LENGTH_SHORT)
                            .show()));
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final CashBoxDeletedItemBinding binding;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                binding = CashBoxDeletedItemBinding.bind(itemView);
            }
        }
    }
}
