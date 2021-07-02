package com.vibal.utilities.ui.cashBoxManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.vibal.utilities.R;
import com.vibal.utilities.databinding.CashBoxBalanceFragmentBinding;
import com.vibal.utilities.databinding.CashBoxBalanceItemBinding;
import com.vibal.utilities.models.CashBoxBalances;
import com.vibal.utilities.models.CashBoxInfo;
import com.vibal.utilities.util.Converters;
import com.vibal.utilities.viewModels.CashBoxBalancesViewModel;
import com.vibal.utilities.workaround.LinearLayoutManagerWrapper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CashBoxBalancesActivity extends AppCompatActivity {
    private final DiffUtil.ItemCallback<CashBoxBalances.Transaction> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CashBoxBalances.Transaction>() {
                @Override
                public boolean areItemsTheSame(@NonNull CashBoxBalances.Transaction oldItem,
                                               @NonNull CashBoxBalances.Transaction newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull CashBoxBalances.Transaction oldItem,
                                                  @NonNull CashBoxBalances.Transaction newItem) {
                    return true;
                }
            };

    private static final String TAG = "PruebaOnlineBalanceFrag";
    private static final String CHART_LABEL = "Balances";
    public static final String CASHBOX_ID_EXTRA = "com.vibal.utilities.ui.cashBoxManager.balances.cashBoxId";
    public static final String CASHBOX_TYPE_EXTRA = "com.vibal.utilities.ui.cashBoxManager.balances.cashBoxType";

    private final NumberFormat formatCurrency = NumberFormat.getCurrencyInstance();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private CashBoxBalancesRecyclerAdapter adapter;
    private CashBoxBalancesViewModel viewModel;
    private CashBoxBalanceFragmentBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CashBoxBalanceFragmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configureChartAppearance();

        //Set up RecyclerView
        binding.rvCBBalance.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManagerWrapper(this); // Workaround for recycler error
        binding.rvCBBalance.setLayoutManager(layoutManager);
        adapter = new CashBoxBalancesRecyclerAdapter();
        binding.rvCBBalance.setAdapter(adapter);
//        binding.rvCBBalance.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        // Get intent
        Intent intent = getIntent();
        @CashBoxType.Type int cashBoxType = intent.getIntExtra(CASHBOX_TYPE_EXTRA, -1);
        long cashBoxId = intent.getLongExtra(CASHBOX_ID_EXTRA, CashBoxInfo.NO_ID);
        if (cashBoxType == -1 || cashBoxId == CashBoxInfo.NO_ID)
            throw new IllegalArgumentException("Intent does not contain correct extras");

        // Get viewModel
        viewModel = new ViewModelProvider(this,
                CashBoxBalancesViewModel.Factory.getInstance(getApplication(),
                        CashBoxType.getCashBoxRepositoryClass(cashBoxType), cashBoxId))
                .get(CashBoxBalancesViewModel.class);
        viewModel.getBalancesEntries().observe(this, balances -> {
            submitChartData(balances);
            adapter.submitList(CashBoxBalances.getBalanceTransactions(balances));
        });

        // Set currency
        compositeDisposable.add(viewModel.getCurrency().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(currency -> {
                    if (!Objects.equals(formatCurrency.getCurrency(), currency)) {
                        formatCurrency.setCurrency(currency);
                        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
                    }
                }));

        // Set Toolbar
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private void configureChartAppearance() {
        // Set colors
        BalancesBarDataSet.initializeColors(ContextCompat.getColor(this, R.color.colorChartPositive),
                ContextCompat.getColor(this, R.color.colorChartNegative));

        binding.barChartBalance.setDrawBarShadow(false);
        Description description = new Description();
        description.setText("");
        binding.barChartBalance.setDescription(description);
        binding.barChartBalance.getLegend().setEnabled(false);
        binding.barChartBalance.setPinchZoom(false);
        binding.barChartBalance.setDoubleTapToZoomEnabled(false);
        binding.barChartBalance.setDrawValueAboveBar(false);
        binding.barChartBalance.setScaleEnabled(false);
        binding.barChartBalance.setNoDataText("No entries added");

        // Display the axis on the left (axis with names)
        XAxis xAxis = binding.barChartBalance.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setEnabled(true);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);

        YAxis left = binding.barChartBalance.getAxisLeft();
        left.setDrawZeroLine(true);
        left.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatCurrency.format(value);
            }
        });

        YAxis right = binding.barChartBalance.getAxisRight();
        right.setDrawAxisLine(true);
        right.setDrawGridLines(false);
        right.setEnabled(false);
    }

    private void submitChartData(@NonNull List<CashBoxBalances.Entry> balances) {
        // Create data
        ArrayList<BarEntry> values = new ArrayList<>();
        float temp;
        ArrayList<String> names = new ArrayList<>();
        for (int k = 0; k < balances.size(); k++) {
            temp = (float) balances.get(k).getAmount();
            values.add(new BarEntry(k, temp));
            names.add(balances.get(k).printFromName());
        }

        // Configure axis
        final float scale = Converters.getScaleDpToPx(this);
        binding.barChartBalance.setMinimumHeight((int) scale * 50 * names.size());
        binding.barChartBalance.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return names.get((int) value);
            }
        });

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(new BalancesBarDataSet(values, CHART_LABEL));

        BarData data = new BarData(dataSets);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatCurrency.format(value);
            }
        });
        data.setBarWidth(0.9f);
        data.setValueTextSize(12f);

        binding.barChartBalance.setData(data);
        binding.barChartBalance.invalidate();
        binding.barChartBalance.animateY(1000);
    }

    private static class BalancesBarDataSet extends BarDataSet {
        private static int COLOR_POSITIVE;
        private static int COLOR_NEGATIVE;
        private static boolean initialized = false;

        public static void initializeColors(int colorPositive, int colorNegative) {
            COLOR_POSITIVE = colorPositive;
            COLOR_NEGATIVE = colorNegative;
            initialized = true;
        }

        public BalancesBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
            if (initialized)
                setColors(COLOR_POSITIVE, COLOR_NEGATIVE);
        }

        @Override
        public int getColor(int index) {
//            if(!initialized)
//                return super.getColor(index);

            if (getEntryForIndex(index).getY() >= 0)
                return mColors.get(0);
            else
                return mColors.get(1);
        }
    }

    class CashBoxBalancesRecyclerAdapter extends ListAdapter<CashBoxBalances.Transaction, CashBoxBalancesRecyclerAdapter.ViewHolder> {
        public CashBoxBalancesRecyclerAdapter() {
            super(DIFF_CALLBACK);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            CashBoxBalanceItemBinding binding = CashBoxBalanceItemBinding.inflate(
                    LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
            return new ViewHolder(binding.getRoot(), binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CashBoxBalances.Transaction transaction = getItem(position);

            holder.binding.rvFrom.setText(transaction.printFromName());
            holder.binding.rvTo.setText(transaction.printToName());
            holder.binding.rvAmount.setText(formatCurrency.format(transaction.getAmount()));
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final CashBoxBalanceItemBinding binding;

            public ViewHolder(@NonNull View view, CashBoxBalanceItemBinding binding) {
                super(view);
                this.binding = binding;
            }
        }
    }
}
