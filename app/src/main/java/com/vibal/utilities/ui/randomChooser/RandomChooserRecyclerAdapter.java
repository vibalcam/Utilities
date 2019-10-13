package com.vibal.utilities.ui.randomChooser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vibal.utilities.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RandomChooserRecyclerAdapter extends RecyclerView.Adapter<RandomChooserRecyclerAdapter.ViewHolder> {
    private List<String> contestants;

    public RandomChooserRecyclerAdapter(List<String> contestants) {
        this.contestants = contestants;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.random_chooser_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.rvRandomChooserContestant.setText(contestants.get(position));
    }

    @Override
    public int getItemCount() {
        return contestants.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.rvRandomChooserContestant)
        TextView rvRandomChooserContestant;
        @Nullable
        @BindView(R.id.rvRandomChooserLayout)
        LinearLayout rvRandomChooserLayout;

        public ViewHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
