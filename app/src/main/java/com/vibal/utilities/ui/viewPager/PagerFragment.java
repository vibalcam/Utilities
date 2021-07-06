package com.vibal.utilities.ui.viewPager;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class PagerFragment extends Fragment {
    // imp Add date and currency format as general class

    private static final String PAGER_POSITION_ARG = "pager_position";
    private int pagerPosition = -1;

    protected void setPositionAsArgument(int pagerPosition) {
        Bundle bundle = getArguments() != null ? getArguments() : new Bundle();
        bundle.putInt(PAGER_POSITION_ARG, pagerPosition);
        setArguments(bundle);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            pagerPosition = getArguments().getInt(PAGER_POSITION_ARG, -1);
    }

    protected int getPagerPosition() {
        return pagerPosition;
    }

    @Nullable
    protected PagerActivity getPagerActivity() {
        return getActivity() instanceof PagerActivity ? ((PagerActivity) getActivity()) : null;
    }

    protected boolean isOptionsMenuActive() {
        // If this fragment is not currently showing
        // if no pager position, return true
        return getPagerActivity() == null || getPagerActivity().getCurrentPagerPosition() == pagerPosition;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!isOptionsMenuActive())
            return false;
        return super.onOptionsItemSelected(item);
    }

    protected void setTabLayoutVisibility(int visibility) {
        if (getPagerActivity() != null)
            getPagerActivity().setTabLayoutVisibility(visibility);
    }

    public boolean onBackPressed() {
        return false;
    }
}
