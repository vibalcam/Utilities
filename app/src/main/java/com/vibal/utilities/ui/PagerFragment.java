package com.vibal.utilities.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

public abstract class PagerFragment extends Fragment {
    private static final String PAGER_POSITION_ARG = "pager_position";
    private int pagerPosition = -1;

    protected void setPositionAsArgument(int pagerPosition) {
        Bundle bundle = getArguments()!=null ? getArguments() : new Bundle();
        bundle.putInt(PAGER_POSITION_ARG,pagerPosition);
        setArguments(bundle);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null)
            pagerPosition = getArguments().getInt(PAGER_POSITION_ARG, -1);
    }

    protected int getPagerPosition() {
        return pagerPosition;
    }

    protected boolean isOptionsMenuActive() {
        // If this fragment is not currently showing
        if(getActivity() instanceof PagerActivity) {
             return ((PagerActivity) getActivity()).getCurrentPagerPosition() == pagerPosition;
        } else // if no pager position, return true
            return true;
    }

    protected void setTabLayoutVisibility(int visibility) {
        if((getActivity() instanceof PagerActivity))
            ((PagerActivity) getActivity()).setTabLayoutVisibility(visibility);
    }

    public boolean onBackPressed() {
        return false;
    }
}
