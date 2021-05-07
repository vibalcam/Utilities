package com.vibal.utilities.ui.cashBoxManager;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vibal.utilities.R;
import com.vibal.utilities.databinding.DialogViewChangesBinding;
import com.vibal.utilities.databinding.DialogViewChangesItemBinding;
import com.vibal.utilities.models.CashBoxInfo;
import com.vibal.utilities.models.EntryOnline;
import com.vibal.utilities.models.EntryOnlineInfo;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.viewModels.CashBoxOnlineViewModel;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CashBoxChangesDialogFragment extends DialogFragment {
    private static final String TAG = "PruebaChangesDialog";
    private static final String ARGS_CASHBOX_ID = "cashBoxId";

    private CashBoxChangesRecyclerAdapter adapter;
    private CashBoxOnlineViewModel viewModel;
    private DialogViewChangesBinding binding;
    private Disposable disposable;

    static CashBoxChangesDialogFragment newInstance(long cashBoxId) {
        CashBoxChangesDialogFragment fragment = new CashBoxChangesDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARGS_CASHBOX_ID, cashBoxId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogViewChangesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Set up the RecyclerView
        binding.rvChanges.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.rvChanges.setLayoutManager(layoutManager);
        binding.rvChanges.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
        adapter = new CashBoxChangesRecyclerAdapter();
        binding.rvChanges.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        long cashBoxId = getArguments().getLong(ARGS_CASHBOX_ID);
        viewModel = new ViewModelProvider(requireParentFragment()).get(CashBoxOnlineViewModel.class);
        if (cashBoxId == CashBoxInfo.NO_ID) {
            LogUtil.debug(TAG, "Tried to get changes for NO_ID");
            dismiss();
        } else {
            disposable = viewModel.getNonViewedEntries(cashBoxId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(entryOnlineBundleMap -> adapter.submitNew(entryOnlineBundleMap),
                            throwable -> {
                                LogUtil.error(TAG, "Obtaining non viewed: ", throwable);
                                Toast.makeText(requireParentFragment().getContext(), "Unexpected error", Toast.LENGTH_SHORT).show();
                                dismiss();
                            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null)
            disposable.dispose();
    }

    class CashBoxChangesRecyclerAdapter extends RecyclerView.Adapter<CashBoxChangesRecyclerAdapter.ViewHolder> {
        private final NumberFormat formatCurrency = NumberFormat.getCurrencyInstance();
        private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

        private List<EntryOnline.EntryChanges> changesList = new ArrayList<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_view_changes_item,
                    parent, false);
            return new ViewHolder(view);
        }

        private void submitNew(List<EntryOnline.EntryChanges> entryChanges) {
            if (entryChanges == null)
                return;
            this.changesList = entryChanges;
            notifyDataSetChanged();

            //Stop showing progress bar
            binding.progressChanges.hide();
            binding.rvChanges.setVisibility(View.VISIBLE);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            EntryOnline.EntryChanges changes = changesList.get(position);
            EntryOnlineInfo newEntry = changes.getNewEntry().getEntryInfo();
            EntryOnlineInfo oldEntry = changes.getOldEntry().getEntryInfo();

            if (newEntry != null && oldEntry != null) { // Updated
                // Amount
                viewHolder.binding.changesNewAmount.setText(formatCurrency.format(newEntry.getAmount()));
                viewHolder.binding.changesNewAmount.setVisibility(View.VISIBLE);
                Double amount = changes.getDiffAmount();
                if (amount != null) {
                    viewHolder.binding.changesOldAmount.setText(formatCurrency.format(amount));
                    viewHolder.binding.changesOldAmount.setVisibility(View.VISIBLE);
                    viewHolder.binding.changesOldAmount.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                } else
                    viewHolder.binding.changesOldAmount.setVisibility(View.GONE);

                // Info
                viewHolder.binding.changesNewInfo.setText(newEntry.printInfo());
                viewHolder.binding.changesNewInfo.setVisibility(View.VISIBLE);
                String info = changes.getDiffInfo();
                if (info != null) {
                    viewHolder.binding.changesOldInfo.setText(info);
                    viewHolder.binding.changesOldInfo.setVisibility(View.VISIBLE);
                    viewHolder.binding.changesOldInfo.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                } else
                    viewHolder.binding.changesOldInfo.setVisibility(View.GONE);

                // Date
                viewHolder.binding.changesNewDate.setText(dateFormat.format(newEntry.getDate().getTime()));
                viewHolder.binding.changesNewDate.setVisibility(View.VISIBLE);
                Calendar calendar = changes.getDiffDate();
                if (calendar != null) {
                    viewHolder.binding.changesOldDate.setText(dateFormat.format(calendar.getTime()));
                    viewHolder.binding.changesOldDate.setVisibility(View.VISIBLE);
                    viewHolder.binding.changesOldDate.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                } else
                    viewHolder.binding.changesOldDate.setVisibility(View.GONE);
            } else if (oldEntry != null) { // Delete
                // Amount
                viewHolder.binding.changesNewAmount.setVisibility(View.GONE);
                viewHolder.binding.changesOldAmount.setText(formatCurrency.format(oldEntry.getAmount()));
                viewHolder.binding.changesOldAmount.setVisibility(View.VISIBLE);
                viewHolder.binding.changesOldAmount.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

                // Info
                viewHolder.binding.changesNewInfo.setVisibility(View.GONE);
                viewHolder.binding.changesOldInfo.setText(oldEntry.getInfo());
                viewHolder.binding.changesOldInfo.setVisibility(View.VISIBLE);
                viewHolder.binding.changesOldInfo.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

                // Date
                viewHolder.binding.changesNewDate.setVisibility(View.GONE);
                viewHolder.binding.changesOldDate.setText(dateFormat.format(oldEntry.getDate().getTime()));
                viewHolder.binding.changesOldDate.setVisibility(View.VISIBLE);
                viewHolder.binding.changesOldDate.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            } else if (newEntry != null) { // Insert
                // Amount
                viewHolder.binding.changesNewAmount.setText(formatCurrency.format(newEntry.getAmount()));
                viewHolder.binding.changesNewAmount.setVisibility(View.VISIBLE);
                viewHolder.binding.changesOldAmount.setVisibility(View.GONE);

                // Info
                viewHolder.binding.changesNewInfo.setText(newEntry.printInfo());
                viewHolder.binding.changesNewInfo.setVisibility(View.VISIBLE);
                viewHolder.binding.changesOldInfo.setVisibility(View.GONE);

                // Date
                viewHolder.binding.changesNewDate.setText(dateFormat.format(newEntry.getDate().getTime()));
                viewHolder.binding.changesNewDate.setVisibility(View.VISIBLE);
                viewHolder.binding.changesOldDate.setVisibility(View.GONE);
            }

//            EntryOnline.EntryChanges changes = changesList.get(position);
//            EntryOnline entry = changes.getNewEntry();
//            Bundle bundle = changes.getChangesPayload();
//
//            // New entry
//            if(!changes.isDelete()) {
//                // Amount
//                viewHolder.amount.setText(formatCurrency.format(entry.getAmount()));
//                viewHolder.amount.setVisibility(View.VISIBLE);
//                // CashBoxInfo
//                viewHolder.info.setText(entry.printInfo());
//                viewHolder.info.setVisibility(View.VISIBLE);
//                // Date
//                viewHolder.date.setText(dateFormat.format(entry.getDate().getTime()));
//                viewHolder.date.setVisibility(View.VISIBLE);
//            } else {
//                viewHolder.amount.setVisibility(View.GONE);
//                viewHolder.info.setVisibility(View.GONE);
//                viewHolder.date.setVisibility(View.GONE);
//            }
//
//            // Old entry
//            if(changes.isDelete()) {
//                // Amount
//                viewHolder.oldAmount.setText(formatCurrency.format(entry.getAmount()));
//                viewHolder.oldAmount.setVisibility(View.VISIBLE);
//                viewHolder.oldAmount.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
//
//                // CashBoxInfo
//                viewHolder.oldInfo.setText(entry.getInfo());
//                viewHolder.oldInfo.setVisibility(View.VISIBLE);
//                viewHolder.oldInfo.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
//
//                // Date
//                viewHolder.oldDate.setText(dateFormat.format(
//                        Converters.calendarFromTimestamp(bundle.getLong(DIFF_DATE)).getTime()));
//                viewHolder.oldDate.setVisibility(View.VISIBLE);
//                viewHolder.oldDate.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
//
//            }
//
//
//
//            if(bundle != null) {
//                // Amount
//                if (bundle.containsKey(DIFF_AMOUNT)) {
//                    viewHolder.oldAmount.setText(formatCurrency.format(bundle.getDouble(DIFF_AMOUNT)));
//                    viewHolder.oldAmount.setVisibility(View.VISIBLE);
//                    viewHolder.oldAmount.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
//                } else
//                    viewHolder.oldAmount.setVisibility(View.GONE);
//
//                // CashBoxInfo
//                if (bundle.containsKey(DIFF_INFO)) {
//                    viewHolder.oldInfo.setText(bundle.getString(DIFF_INFO));
//                    viewHolder.oldInfo.setVisibility(View.VISIBLE);
//                    viewHolder.oldInfo.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
//                } else
//                    viewHolder.oldInfo.setVisibility(View.GONE);
//
//                // Date
//                if (bundle.containsKey(DIFF_DATE)) {
//                    viewHolder.oldDate.setText(dateFormat.format(
//                            Converters.calendarFromTimestamp(bundle.getLong(DIFF_DATE)).getTime()));
//                    viewHolder.oldDate.setVisibility(View.VISIBLE);
//                    viewHolder.oldDate.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
//                } else
//                    viewHolder.oldDate.setVisibility(View.GONE);
//            } else {
//                viewHolder.oldAmount.setVisibility(View.GONE);
//                viewHolder.oldInfo.setVisibility(View.GONE);
//                viewHolder.oldDate.setVisibility(View.GONE);
//            }
        }

        @Override
        public int getItemCount() {
            return changesList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final DialogViewChangesItemBinding binding;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                binding = DialogViewChangesItemBinding.bind(itemView);
            }
        }
    }
}
