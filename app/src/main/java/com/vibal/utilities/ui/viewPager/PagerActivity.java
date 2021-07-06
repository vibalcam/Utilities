package com.vibal.utilities.ui.viewPager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.vibal.utilities.util.LogUtil;

/**
 * DON'T FORGET TO SET UP THE VIEW PAGER
 */
public abstract class PagerActivity extends AppCompatActivity {
    @NonNull
    abstract public ViewPager2 getViewPager2();

    @Nullable
    abstract public TabLayout getTabLayout();

    public int getCurrentPagerPosition() {
        return getViewPager2().getCurrentItem();
    }

    public void setTabLayoutVisibility(int visibility) {
        if (getTabLayout() != null)
            getTabLayout().setVisibility(visibility);
    }

    @Override
    public void onBackPressed() {
        LogUtil.debug("Prueba", "Current item " + getViewPager2().getCurrentItem());
        PagerFragment fragment = getPagerFragment(getViewPager2().getCurrentItem());
        if (fragment != null) {
            if (fragment.onBackPressed())
                return;
        }
        super.onBackPressed();
    }

    @Nullable
    protected PagerFragment getPagerFragment(int position) {
        return (PagerFragment) getSupportFragmentManager().findFragmentByTag("f" + position);
//        return (PagerFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" +
//                getViewPager2().getId() + ":" + position);
    }

    public void selectTab(int position) {
        getViewPager2().setCurrentItem(position);
    }
}
