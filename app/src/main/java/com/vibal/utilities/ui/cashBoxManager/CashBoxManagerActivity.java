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

import com.google.android.material.tabs.TabLayout;
import com.vibal.utilities.R;
import com.vibal.utilities.databinding.CashBoxManagerActivityBinding;
import com.vibal.utilities.ui.viewPager.PagerActivity;
import com.vibal.utilities.ui.viewPager.PagerFragment;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.widget.CashBoxWidgetProvider;

import java.util.Objects;

public class CashBoxManagerActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, PagerActivity {
    // Extras for intents
    public static final String EXTRA_CASHBOX_ID = "com.vibal.utilities.cashBoxId";
    public static final String EXTRA_CASHBOX_TYPE = "com.vibal.utilities.cashBoxId";

    //    @IntDef({ONLINE, LOCAL})
//    @Retention(RetentionPolicy.SOURCE)
//    public @interface CashBoxType {
//    }
//
//    public static final int LOCAL = 0;
//    public static final int ONLINE = 1;
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

    // For online
    public static final String CLIENT_ID_KEY = "com.vibal.utilities.cashBoxManager.CLIENT_ID";
    public static final String USERNAME_KEY = "com.vibal.utilities.cashBoxManager.USERNAME";

    private CashBoxManagerActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CashBoxManagerActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        binding.CBViewPager.setAdapter(new MenusPagerAdapter(getSupportFragmentManager()));
        if (binding.CBTabs != null)
            binding.CBTabs.addOnTabSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Look at intent
        doIntentAction(getIntent());
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.debug("PruebaCBActivity", "On pause");

        // Update app widget
        Intent intent = new Intent(this, CashBoxWidgetProvider.class);
        intent.setAction(CashBoxWidgetProvider.ACTION_REFRESH);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), CashBoxWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        doIntentAction(intent);
        setIntent(intent);
    }

    private void doIntentAction(Intent intent) {
        if (intent == null || intent.getIntExtra(EXTRA_ACTION, NO_ACTION) == NO_ACTION)
            return;

        if (intent.getIntExtra(EXTRA_CASHBOX_TYPE, CashBoxType.LOCAL) == CashBoxType.ONLINE) {
            selectTab(CashBoxType.ONLINE);
        } else {
            selectTab(CashBoxType.LOCAL);
        }
    }

    private Fragment getPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + binding.CBViewPager.getId() +
                ":" + position);
    }

    @Override
    public void onBackPressed() {
        LogUtil.debug("Prueba", "Current item " + binding.CBViewPager.getCurrentItem());
        Fragment fragment = getPagerFragment(binding.CBViewPager.getCurrentItem());
        if (fragment instanceof PagerFragment) {
            if (((PagerFragment) fragment).onBackPressed())
                return;
        }
        super.onBackPressed();
    }

//    imp https://developer.android.com/guide/navigation/navigation-swipe-view-2

    // Implementing PagerActivity
    @Override
    public int getCurrentPagerPosition() {
        return binding.CBViewPager.getCurrentItem();
    }

    @Override
    public void setTabLayoutVisibility(int visibility) {
        if (binding.CBTabs != null)
            binding.CBTabs.setVisibility(visibility);
    }

    // Implementing TabLayout.OnTabSelectedListener
    @Override
    public void onTabSelected(@NonNull TabLayout.Tab tab) {
        selectTab(tab.getPosition());
    }

    private void selectTab(int position) {
        LogUtil.debug("PruebaViewPager", "Position: " + position);
        supportInvalidateOptionsMenu();
        binding.CBViewPager.setCurrentItem(position, true);
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
                case CashBoxType.LOCAL:
                    return CashBoxViewFragment.newInstance(position, false);
                case CashBoxType.ONLINE:
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
