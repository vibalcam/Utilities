package com.vibal.utilities.ui.cashBoxManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.vibal.utilities.R;
import com.vibal.utilities.ui.PagerFragment;
import com.vibal.utilities.util.LogUtil;

import butterknife.ButterKnife;

public class CashBoxViewFragment extends PagerFragment {
    private static final String TAG = "PruebaView";

    @NonNull
    static CashBoxViewFragment newInstance(int pagerPosition) {
        CashBoxViewFragment fragment = new CashBoxViewFragment();
        fragment.setPositionAsArgument(pagerPosition);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cash_box_view_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Logic for landscape mode
        FragmentManager fragmentManager = getChildFragmentManager();
        LogUtil.debug(TAG, fragmentManager.getFragments().toString());
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, CashBoxManagerFragment.newInstance(getPagerPosition()))
                    .commitNow();
        } else {
            View viewLand = requireView().findViewById(R.id.containerItem);
            Fragment fragment = fragmentManager.findFragmentById(R.id.container);
            if (viewLand != null && viewLand.getVisibility() == View.VISIBLE &&
                    fragment instanceof CashBoxItemFragment) {
                // If the Item Fragment was active, pop it and replace it
                fragmentManager.popBackStackImmediate();
                fragmentManager.beginTransaction()
                        .replace(R.id.containerItem, CashBoxItemFragment.newInstance(getPagerPosition()))
                        .replace(R.id.container, CashBoxManagerFragment.newInstance(getPagerPosition()))
                        .commitNow();
            } else if ((fragment = fragmentManager.findFragmentById(R.id.containerItem)) != null) {
                // Remove the ItemFragment that is not being used anymore
                fragmentManager.beginTransaction()
                        .remove(fragment)
                        .commitNow();
            }
        }
        LogUtil.debug(TAG, fragmentManager.getFragments().toString());
    }

    @Override
    public boolean onBackPressed() {
        if (!isAdded())
            return false;

        LogUtil.debug(TAG, "funciona " + getChildFragmentManager().getBackStackEntryCount());
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            return true;
        } else
            return false;
    }
}
