package com.utilities.vibal.utilities.ui.randomChooser;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.ui.settings.SettingsActivity;
import com.utilities.vibal.utilities.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;

public class RandomChooserActivity extends AppCompatActivity {
    @BindView(R.id.rvRandomChooser) RecyclerView rvRandomChooser;
    @BindView(R.id.inputText) EditText inputText;

    private List<String> contestants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.random_chooser_activity);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initialize data
        contestants = new ArrayList<>();

        //Set-up RecyclerView
        rvRandomChooser.setHasFixedSize(true);
        rvRandomChooser.setLayoutManager(new LinearLayoutManager(rvRandomChooser.getContext()));
        rvRandomChooser.setAdapter(new RandomChooserRecyclerAdapter(contestants));
    }

    @Override
    protected void onStart() {
        super.onStart();
        inputText.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_toolbar_random_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_RC_deleteAll:
                int size = contestants.size();
                contestants.clear();
                rvRandomChooser.getAdapter().notifyItemRangeRemoved(0, size);
                Toast.makeText(this, "Deleted all entries", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_RC_help:
                Util.createHelpDialog(this, R.string.randomChooser_helpTitle, R.string.randomChooser_help).show();
                return true;
            case R.id.action_RC_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.buttonShuffle)
    public void onViewClicked(View view) {
        Collections.shuffle(contestants);
        rvRandomChooser.getAdapter().notifyDataSetChanged();
    }

    @OnEditorAction(R.id.inputText)
    public boolean onEditorAction(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            addParticipant();
            return true;
        }
        return false;
    }

    /**
     * Adds the text in inputText to the participants ArrayList
     */
    @OnClick(R.id.buttonAdd)
    void addParticipant() {
        String input = inputText.getText().toString();
        if (!input.isEmpty()) {
            contestants.add(input);
            inputText.setText("");
            rvRandomChooser.getAdapter().notifyItemInserted(contestants.size() - 1);
        } else
            Toast.makeText(this, "You have to enter a name", Toast.LENGTH_SHORT).show();
    }

    /**
     * Randomly chooses an element from the participants ArrayList and shows it in a AlertDialog
     */
    @OnClick(R.id.buttonRoll)
    void getWinner() {
        if (!contestants.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("The dice have spoken!");
            builder.setMessage(contestants.get((int) (Math.random() * contestants.size())));
            builder.show();
        } else
            Toast.makeText(this, "No contestants added", Toast.LENGTH_SHORT).show();
    }
}