package com.vibal.utilities.workaround;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vibal.utilities.util.LogUtil;

public class LinearLayoutManagerWrapper extends LinearLayoutManager {
    private static final String TAG = "PruebaWorkaround";

    public LinearLayoutManagerWrapper(Context context) {
        super(context);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            LogUtil.error(TAG, "Workaround error", e);
        }
    }
}
