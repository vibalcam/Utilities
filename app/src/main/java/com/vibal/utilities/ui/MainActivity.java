package com.vibal.utilities.ui;import android.content.Intent;import android.media.MediaPlayer;import android.os.Bundle;import android.view.Menu;import android.view.MenuItem;import androidx.annotation.NonNull;import androidx.appcompat.app.AlertDialog;import androidx.appcompat.app.AppCompatActivity;import androidx.preference.PreferenceManager;import com.vibal.utilities.R;import com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;import com.vibal.utilities.ui.randomChooser.RandomChooserActivity;import com.vibal.utilities.ui.settings.SettingsActivity;import com.vibal.utilities.util.LogUtil;import com.vibal.utilities.util.Util;import butterknife.ButterKnife;import butterknife.OnLongClick;public class MainActivity extends AppCompatActivity {    private static final String TAG = "PruebaMainActivity";    private static boolean startUp = true;    private static int[] songs = {};    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.main_activity);        ButterKnife.bind(this);        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);        if (startUp)            startDefaultStartScreen();//        else if(savedInstanceState == null) {//            getSupportFragmentManager()//                    .beginTransaction()//                    .add(R.id.utility_options_fragment,UtilitiesOptionsFragment.newInstance())//                    .commitNow();//        }    }    private void startDefaultStartScreen() {        startUp = false;        String defaultStart = PreferenceManager.getDefaultSharedPreferences(this).getString("defaultStart", "undefined");        switch (defaultStart) {            case "randomChooser":                startActivity(new Intent(this, RandomChooserActivity.class));                break;            case "cashBoxManager":                startActivity(new Intent(this, CashBoxManagerActivity.class));                break;        }    }    @Override    public boolean onCreateOptionsMenu(Menu menu) {        getMenuInflater().inflate(R.menu.menu_toolbar_main, menu);        return true;    }    @Override    public boolean onOptionsItemSelected(@NonNull MenuItem item) {        switch (item.getItemId()) {            case R.id.action_main_settings:                startActivity(new Intent(this, SettingsActivity.class));                return true;            case R.id.action_main_help:                Util.createHelpDialog(this, R.string.appHelpTitle, R.string.appHelpMessage).show();                return true;            case R.id.action_main_about:                return showAboutDialog();            default:                return super.onOptionsItemSelected(item);        }    }    @OnLongClick(R.id.logoImage)    boolean showAboutDialog() {//        AlertDialog.Builder builder = new AlertDialog.Builder(this);//        builder.setTitle("About this app")//                .setMessage(getString(R.string.aboutApp))//                .setPositiveButton(R.string.button_gotIt, null)//                .show();        //todo        AlertDialog dialog = new AlertDialog.Builder(this)                .setTitle("About this app")                .setMessage(getString(R.string.aboutApp))                .setPositiveButton(R.string.button_gotIt, null)                .create();        //Get random song        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("playAboutSong", true)                && songs.length>0) { //play random song            int random = (int) Math.floor(Math.random() * songs.length);            LogUtil.debug(TAG, Integer.toString(random));            MediaPlayer mediaPlayer = MediaPlayer.create(this, songs[random]);            dialog.setOnShowListener(dialogInterface -> mediaPlayer.start());            dialog.setOnDismissListener(dialogInterface -> mediaPlayer.stop());        }        dialog.show();        return true;    }}