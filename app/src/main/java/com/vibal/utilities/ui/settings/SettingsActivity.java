package com.vibal.utilities.ui.settings;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.vibal.utilities.R;
import com.vibal.utilities.util.MyDialogBuilder;

public class SettingsActivity extends AppCompatActivity {
    public static final String KEY_DEFAULT_START = "defaultStart";
    public static final String KEY_PLAY_ABOUT_SONG = "playAboutSong";
    public static final String KEY_SWIPE_LEFT_DELETE = "swipeLeftDelete";
    public static final String KEY_NOTIFY_PERIODIC = "notifyPeriodic";
    public static final String KEY_ONLINE = "allowOnline";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            findPreference(KEY_ONLINE).setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            SwitchPreference switchPreference = (SwitchPreference) preference;
            switchPreference.setChecked(!switchPreference.isChecked());
            if(switchPreference.isChecked())
                cancelOnlineMode(switchPreference);
            else
                acceptOnlineMode(switchPreference);
            return true;
        }

        //todo hacer preferences delete
        private void acceptOnlineMode(SwitchPreference preference) {
            new MyDialogBuilder(requireContext())
                    .setTitle(R.string.pref_online)
                    .setMessage(R.string.allowOnline_acceptMessage)
                    .setCancelOnTouchOutside(true)
                    .setPositiveButton((dialog, which) -> preference.setChecked(true))
                    .show();
        }

        private void cancelOnlineMode(SwitchPreference preference) {
            new MyDialogBuilder(requireContext())
                    .setTitle(R.string.pref_online_cancel)
                    .setMessage(R.string.allowOnline_cancelMessage)
                    .setPositiveButton(R.string.deleteLocal,(dialog, which) -> preference.setChecked(false))
                    .setNegativeButton(R.string.moveLocal,(dialog, which) -> preference.setChecked(false))
                    .setNeutralButton(R.string.cancelDialog,null)
                    .show();
        }
    }
}