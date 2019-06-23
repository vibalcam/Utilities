package com.utilities.vibal.utilities.ui;import android.content.Intent;import android.os.Bundle;import android.view.Menu;import android.view.MenuItem;import androidx.appcompat.app.AlertDialog;import androidx.appcompat.app.AppCompatActivity;import androidx.preference.PreferenceManager;import com.utilities.vibal.utilities.R;import com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;import com.utilities.vibal.utilities.ui.randomChooser.RandomChooserActivity;import com.utilities.vibal.utilities.ui.settings.SettingsActivity;import com.utilities.vibal.utilities.util.Util;import butterknife.ButterKnife;import butterknife.OnClick;import butterknife.OnLongClick;public class MainActivity extends AppCompatActivity {    private static boolean startUp = true;    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.main_activity);        ButterKnife.bind(this);        PreferenceManager.setDefaultValues(this,R.xml.preferences,false);        if(startUp)            startDefaultStartScreen();    }    private void startDefaultStartScreen() {        startUp = false;        String defaultStart = PreferenceManager.getDefaultSharedPreferences(this).getString("defaultStart","undefined");        if (defaultStart != null) {            switch (defaultStart) {                case "randomChooser":                    startRandomChooser();                    break;                case "cashBoxManager":                    startCashBoxManager();                    break;            }        }    }    @OnClick(R.id.randomChooser)    void startRandomChooser() {        startActivity(new Intent(this, RandomChooserActivity.class));    }    @OnClick(R.id.cashBoxManager)    void startCashBoxManager() {        startActivity(new Intent(this, CashBoxManagerActivity.class));    }    @Override    public boolean onCreateOptionsMenu(Menu menu) {        getMenuInflater().inflate(R.menu.menu_toolbar_main, menu);        return true;    }    @Override    public boolean onOptionsItemSelected(MenuItem item) {        switch (item.getItemId()) {            case R.id.action_main_settings:                startActivity(new Intent(this, SettingsActivity.class));                return true;            case R.id.action_main_help:                Util.getHelpDialog(this, R.string.appHelpTitle, R.string.appHelpMessage).show();                return true;            case R.id.action_main_about:                return showAboutDialog();            default:                return super.onOptionsItemSelected(item);        }    }    @OnLongClick(R.id.logoImage)    boolean showAboutDialog() {        AlertDialog.Builder builder = new AlertDialog.Builder(this);        builder.setTitle("About this app")                .setMessage(getString(R.string.aboutApp))                .setPositiveButton(R.string.button_gotIt, null)                .show();        return true;    }}