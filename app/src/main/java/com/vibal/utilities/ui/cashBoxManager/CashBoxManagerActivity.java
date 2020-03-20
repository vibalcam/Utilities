package com.vibal.utilities.ui.cashBoxManager;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.vibal.utilities.R;
import com.vibal.utilities.ui.viewPager.PagerActivity;
import com.vibal.utilities.ui.viewPager.PagerFragment;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.widget.CashBoxWidgetProvider;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CashBoxManagerActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, PagerActivity {
    //public class CashBoxManagerActivity extends AppCompatActivity {
    // Extras for intents
    public static final String EXTRA_CASHBOX_ID = "com.vibal.utilities.cashBoxId";
    public static final String EXTRA_ACTION = "com.vibal.utilities.ui.cashBoxManager.action";
    public static final int NO_ACTION = 0;
    public static final int ACTION_ADD_CASHBOX = 1;
    public static final int ACTION_DETAILS = 2;

    /**
     * Group Notification for CashBoxManager
     */
    public static final String NOTIFICATION_GROUP_KEY_CASHBOX = "com.vibal.utilities.NOTIFICATION_GROUP_CASHBOX";

    /**
     * Shared Preferences Key for CashBoxManager
     */
    public static final String CASHBOX_MANAGER_PREFERENCE = "com.vibal.utilities.cashBoxManager.CASHBOX_MANAGER_PREFERENCE";
    /**
     * Next free group id for use (returned id is not in use, +1 when save new one)
     */
    public static final String GROUP_ID_COUNT_KEY = "com.vibal.utilities.cashBoxManager.GROUP_ID_COUNT";
    public static final String GROUP_ADD_MODE_KEY = "com.vibal.utilities.cashBoxManager.GROUP_ADD_MODE";
    public static final String CLIENT_ID_KEY = "com.vibal.utilities.cashBoxManager.CLIENT_ID";

    @BindView(R.id.CB_viewPager)
    ViewPager pager;
    @Nullable
    @BindView(R.id.CB_tabs)
    TabLayout tabs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cash_box_manager_activity);
        ButterKnife.bind(this);

        //Cancel reminder notifications if any
        NotificationManagerCompat.from(this).cancelAll();

        //Set Toolbar as ActionBar
        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

//        if (savedInstanceState == null) {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.container, CashBoxManagerFragment.newInstance())
//                    .commitNow();
//        } else {
//            View viewLand = findViewById(R.id.containerItem);
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            if (viewLand != null && viewLand.getVisibility() == View.VISIBLE &&
//                    fragmentManager.findFragmentById(R.id.container) instanceof CashBoxItemFragment) {
//                fragmentManager.popBackStack();
//                fragmentManager.beginTransaction()
//                        .replace(R.id.containerItem, CashBoxItemFragment.newInstance())
//                        .replace(R.id.container, CashBoxManagerFragment.newInstance())
//                        .commitNow();
//            }
//        }

        // Set up TabLayout
        pager.setAdapter(new MenusPagerAdapter(getSupportFragmentManager()));
        if (tabs != null)
            tabs.addOnTabSelectedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Update app widget
        //todo update app widget
        Intent intent = new Intent(this, CashBoxWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplicationContext())
                .getAppWidgetIds(new ComponentName(getApplicationContext(), CashBoxWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private Fragment getPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + pager.getId() +
                ":" + position);
    }

    @Override
    public void onBackPressed() {
        LogUtil.debug("Prueba", "Current item " + pager.getCurrentItem());
        Fragment fragment = getPagerFragment(pager.getCurrentItem());
        if (fragment instanceof PagerFragment) {
            if (((PagerFragment) fragment).onBackPressed())
                return;
        }
        super.onBackPressed();
    }

    // Implementing PagerActivity
    @Override
    public int getCurrentPagerPosition() {
        return pager.getCurrentItem();
    }

    @Override
    public void setTabLayoutVisibility(int visibility) {
        if (tabs != null)
            tabs.setVisibility(visibility);
    }

    // Implementing TabLayout.OnTabSelectedListener
    @Override
    public void onTabSelected(@NonNull TabLayout.Tab tab) {
        LogUtil.debug("PruebaViewPager", "Position: " + tab.getPosition());
        supportInvalidateOptionsMenu();
        pager.setCurrentItem(tab.getPosition(), true);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) { // nothing to do
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) { // nothing to do
    }

    private static class MenusPagerAdapter extends FragmentPagerAdapter {

        private MenusPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            LogUtil.debug("PruebaViewPager", "Position get Item: " + position);
            switch (position) {
                case 0:
                    return CashBoxViewFragment.newInstance(position, false);
                case 1:
                    return CashBoxViewFragment.newInstance(position, true);
                case 2:
                    return CashBoxDeletedFragment.newInstance(position);
                case 3:
                    return CashBoxPeriodicFragment.newInstance(position);
                default: // should never happen
                    throw new IllegalArgumentException("No fragment associated to position");
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
