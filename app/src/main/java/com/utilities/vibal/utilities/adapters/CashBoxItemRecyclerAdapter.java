package com.utilities.vibal.utilities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.models.CashBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CashBoxItemRecyclerAdapter extends RecyclerView.Adapter<CashBoxItemRecyclerAdapter.ViewHolder> {
    private CashBox cashBox;
    private Context context;
    private java.text.DateFormat dateFormat;

    public CashBoxItemRecyclerAdapter(CashBox cashBox, Context context) {
        this.cashBox = cashBox;
        this.context = context;
        dateFormat = java.text.DateFormat.getDateInstance();
    }

    private Context getContext(){
        return context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_cash_box_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int index) {
        viewHolder.rvItemAmount.setText(getContext().getString(R.string.amountMoney,cashBox.getAmount(index)));
        viewHolder.rvItemInfo.setText(cashBox.getInfo(index));
        viewHolder.rvItemDate.setText(dateFormat.format(cashBox.getDate(index).getTime()));
    }

    @Override
    public int getItemCount() {
        return cashBox.sizeEntries();
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
    }
}
