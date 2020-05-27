package com.vibal.utilities.ui.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.vibal.utilities.R;
import com.vibal.utilities.persistence.repositories.CashBoxOnlineRepository;
import com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.MyDialogBuilder;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SettingsActivity extends AppCompatActivity {
    public static final String KEY_DEFAULT_START = "defaultStart";
    public static final String KEY_PLAY_ABOUT_SONG = "playAboutSong";
    public static final String KEY_SWIPE_LEFT_DELETE = "swipeLeftDelete";
    public static final String KEY_NOTIFY_PERIODIC = "notifyPeriodic";
    public static final String KEY_ONLINE = "allowOnline";

    public static void acceptOnlineMode(Context context, DialogInterface.OnClickListener onClickListener) {
        new MyDialogBuilder(context)
                .setTitle(R.string.pref_online)
                .setMessage(R.string.allowOnline_acceptMessage)
                .setCancelOnTouchOutside(true)
                .setPositiveButton(onClickListener)
                .show();
    }

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
        private CompositeDisposable compositeDisposable = new CompositeDisposable();

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            findPreference(KEY_ONLINE).setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            SwitchPreference switchPreference = (SwitchPreference) preference;
            switchPreference.setChecked(!switchPreference.isChecked());
            if (switchPreference.isChecked())
                cancelOnlineMode(switchPreference);
            else
                acceptOnlineMode(requireContext(), (dialog, which) -> switchPreference.setChecked(true));
            return true;
        }

        private void cancelOnlineMode(SwitchPreference preference) {
            new MyDialogBuilder(requireContext())
                    .setTitle(R.string.pref_online_cancel)
                    .setMessage(R.string.allowOnline_cancelMessage)
                    .setPositiveButton(R.string.deleteLocal, (dialog, which) -> {
                        if (!preference.isChecked())
                            return;

                        try {
                            CashBoxOnlineRepository repository = CashBoxOnlineRepository.getInstance(requireActivity().getApplication());
                            compositeDisposable.add(repository.deleteUser()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(string -> {
                                                requireActivity().getSharedPreferences(
                                                        CashBoxManagerActivity.CASHBOX_MANAGER_PREFERENCE, Context.MODE_PRIVATE)
                                                        .edit()
                                                        .remove(CashBoxManagerActivity.USERNAME_KEY)
                                                        .remove(CashBoxManagerActivity.CLIENT_ID_KEY)
                                                        .apply();
                                                CashBoxOnlineRepository.setOnlineId(0);
                                                preference.setChecked(false);
                                                Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show();
                                            },
                                            throwable -> Toast.makeText(requireContext(),
                                                    "An unexpected error occured...", Toast.LENGTH_SHORT).show()));
                        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | KeyManagementException e) {
                            LogUtil.error("PruebaSettings", "Delete user:", e);
                            Toast.makeText(requireContext(), "An unexpected error occured...", Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton(R.string.cancelDialog, null)
//                    .setNeutralButton(R.string.cancelDialog, null)
//                    .setNegativeButton(R.string.moveLocal, (dialog, which) -> preference.setChecked(false))
                    .show();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            compositeDisposable.dispose();
        }
    }
}