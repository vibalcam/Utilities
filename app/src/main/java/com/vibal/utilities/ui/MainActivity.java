package com.vibal.utilities.ui;import android.content.Context;import android.content.Intent;import android.content.SharedPreferences;import android.os.Bundle;import android.view.Menu;import android.view.MenuItem;import androidx.annotation.NonNull;import androidx.appcompat.app.AlertDialog;import androidx.appcompat.app.AppCompatActivity;import androidx.preference.PreferenceManager;import com.vibal.utilities.R;import com.vibal.utilities.databinding.MainActivityBinding;import com.vibal.utilities.models.EntryBase;import com.vibal.utilities.persistence.repositories.CashBoxOnlineRepository;import com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;import com.vibal.utilities.ui.randomChooser.RandomChooserActivity;import com.vibal.utilities.ui.settings.SettingsActivity;import com.vibal.utilities.util.Util;import static com.vibal.utilities.ui.settings.SettingsActivity.KEY_DEFAULT_START;public class MainActivity extends AppCompatActivity {    private static final String TAG = "PruebaMainActivity";    //    private static final int[] SONGS = {};    private static boolean startUp = true;    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        MainActivityBinding binding = MainActivityBinding.inflate(getLayoutInflater());        setContentView(binding.getRoot());        // Set up listeners        binding.logoImage.setOnLongClickListener(v -> showAboutDialog());        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);        if (startUp) {            startDefaultStartScreen();            obtainCashBoxOnlineId();        }    }    private void obtainCashBoxOnlineId() {        SharedPreferences sharedPreferences = getSharedPreferences(                CashBoxManagerActivity.CASHBOX_MANAGER_PREFERENCE, Context.MODE_PRIVATE);        CashBoxOnlineRepository.setOnlineId(sharedPreferences                .getLong(CashBoxManagerActivity.CLIENT_ID_KEY, 0));        EntryBase.setSelfName(this);    }    private void startDefaultStartScreen() {        startUp = false;        String defaultStart = PreferenceManager.getDefaultSharedPreferences(this)                .getString(KEY_DEFAULT_START, "undefined");        if (defaultStart.equals(getString(R.string.defaultStart_value_randomChooser)))            startActivity(new Intent(this, RandomChooserActivity.class));        else if (defaultStart.equals(getString(R.string.defaultStart_value_cashBoxManager)))            startActivity(new Intent(this, CashBoxManagerActivity.class));    }    @Override    public boolean onCreateOptionsMenu(Menu menu) {        getMenuInflater().inflate(R.menu.menu_toolbar_main, menu);        return true;    }    @Override    public boolean onOptionsItemSelected(@NonNull MenuItem item) {        switch (item.getItemId()) {            case R.id.action_main_settings:                startActivity(new Intent(this, SettingsActivity.class));                return true;            case R.id.action_main_help:                Util.createHelpDialog(this, R.string.appHelpTitle,                        R.string.appHelpMessage)                        .show();                return true;            case R.id.action_main_versions:                Util.createHelpDialog(this, R.string.action_versions,                        R.string.changesVersions)                        .show();                return true;            case R.id.action_main_about:                return showAboutDialog();            default:                return super.onOptionsItemSelected(item);        }    }    private boolean showAboutDialog() {        AlertDialog dialog = new AlertDialog.Builder(this)                .setTitle("About this app")                .setMessage(getString(R.string.aboutApp))                .setPositiveButton(R.string.button_gotIt, null)                .create();        //Get random song//        if (PreferenceManager.getDefaultSharedPreferences(this)//                .getBoolean(KEY_PLAY_ABOUT_SONG, true)//                && SONGS.length > 0) { //play random song//            int random = (int) Math.floor(Math.random() * SONGS.length);//            LogUtil.debug(TAG, Integer.toString(random));//            MediaPlayer mediaPlayer = MediaPlayer.create(this, SONGS[random]);//            dialog.setOnShowListener(dialogInterface -> mediaPlayer.start());//            dialog.setOnDismissListener(dialogInterface -> mediaPlayer.stop());//        }        dialog.show();        return true;    }}