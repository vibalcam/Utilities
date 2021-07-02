package com.vibal.utilities.ui.randomChooser;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vibal.utilities.R;
import com.vibal.utilities.databinding.RandomChooserActivityBinding;
import com.vibal.utilities.databinding.RandomChooserItemBinding;
import com.vibal.utilities.ui.settings.SettingsActivity;
import com.vibal.utilities.ui.swipeController.CashBoxAdapterSwipable;
import com.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.vibal.utilities.util.MyDialogBuilder;
import com.vibal.utilities.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class RandomChooserActivity extends AppCompatActivity {
    private static final int NO_LIST = 0;

    private RandomChooserRecyclerAdapter adapter;
    private RandomChooserActivityBinding binding;
    private ArrayAdapter<String> spinnerAdapter;
    private SharedPreferences preferences;
    private String currentListName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RandomChooserActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Set up listeners
        binding.spinnerList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onListSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.buttonAddList.setOnClickListener(v -> showAddListDialog());
        binding.buttonDeleteList.setOnClickListener(v -> deleteCurrentList());
        binding.buttonShuffle.setOnClickListener(v -> shuffleContestants());
        binding.inputText.setOnEditorActionListener((v, actionId, event) -> onEditorAction(actionId));
        binding.buttonAdd.setOnClickListener(v -> addParticipant());
        binding.buttonRoll.setOnClickListener(v -> getWinner());

        // Set up toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set-up RecyclerView
        binding.rvRandomChooser.setHasFixedSize(true);
        binding.rvRandomChooser.setLayoutManager(new LinearLayoutManager(binding.rvRandomChooser.getContext()));
        adapter = new RandomChooserRecyclerAdapter();
        binding.rvRandomChooser.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new CashBoxSwipeController(adapter,
                PreferenceManager.getDefaultSharedPreferences(this)));
        itemTouchHelper.attachToRecyclerView(binding.rvRandomChooser);

        // Set up spinner
        preferences = getPreferences(Context.MODE_PRIVATE);
        ArrayList<String> list = new ArrayList<>();
        list.add("New List");
        list.addAll(preferences.getAll().keySet());
        spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_contestants_list_item, list);
        binding.spinnerList.setAdapter(spinnerAdapter);
        binding.spinnerList.setSelection(NO_LIST); // select new list initially
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.inputText.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_toolbar_random_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_RC_deleteAll:
                int size = adapter.contestants.size();
                if (size > 0) {
                    adapter.contestants.clear();
                    saveContestantsToPreferences();
                    adapter.notifyItemRangeRemoved(0, size);
                }
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

    public void onListSelected(int position) {
        currentListName = position == NO_LIST ? null : spinnerAdapter.getItem(position);
        // Clear contestants
        adapter.contestants.clear();
        if (currentListName != null)
            adapter.contestants.addAll(preferences.getStringSet(currentListName, new HashSet<>()));
        adapter.notifyDataSetChanged();
    }

    private void saveContestantsToPreferences() {
        if (currentListName != null)
            preferences.edit().putStringSet(currentListName, new HashSet<>(adapter.contestants)).apply();
    }

    private void showAddListDialog() {
        new MyDialogBuilder(this)
                .setTitle("New contestants list")
                .setView(R.layout.random_chooser_input_name)
                .setActions(dialog -> {
                    Button positive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextNameList);
                    TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.layoutTextNameList);

                    Util.showKeyboard(this, inputName);
                    positive.setOnClickListener((View v) -> {
                        String input = inputName.getText().toString().trim();
                        if (input.isEmpty()) {
                            layoutName.setError("Name cannot be blank");
                            Util.showKeyboard(this, inputName);
                        } else if (preferences.contains(input)) {
                            layoutName.setError("Name already exists");
                            Util.showKeyboard(this, inputName);
                        } else {
                            addList(input);
                            dialog.dismiss();
                        }
                    });
                }).show();
    }

    private void addList(String name) {
        currentListName = name;
        saveContestantsToPreferences();
        spinnerAdapter.insert(currentListName, 1);
        binding.spinnerList.setSelection(1);
    }

    private void deleteCurrentList() {
        if (currentListName == null)
            return;
        spinnerAdapter.remove(currentListName);
        preferences.edit().remove(currentListName).apply();
        binding.spinnerList.setSelection(NO_LIST);
    }

    private void shuffleContestants() {
        Collections.shuffle(adapter.contestants);
        adapter.notifyDataSetChanged();
    }

    private boolean onEditorAction(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            addParticipant();
            return true;
        }
        return false;
    }

    /**
     * Adds the text in inputText to the participants ArrayList
     */
    private void addParticipant() {
        String input = binding.inputText.getText().toString();
        if (input.isEmpty()) {
            Toast.makeText(this, "You have to enter a name", Toast.LENGTH_SHORT).show();
//        } else if (input.contains(SEPARATOR)) {
//            Toast.makeText(this, "The name cannot contain " + SEPARATOR, Toast.LENGTH_SHORT).show();
        } else {
            adapter.contestants.add(input);
            binding.inputText.setText("");
            adapter.notifyItemInserted(adapter.contestants.size() - 1);
            saveContestantsToPreferences();
        }
    }

    /**
     * Randomly chooses an element from the participants ArrayList and shows it in a AlertDialog
     */
    private void getWinner() {
        if (!adapter.contestants.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("The dice have spoken!");
            builder.setMessage(adapter.contestants.get((int) (Math.random() * adapter.contestants.size())));
            builder.show();
        } else
            Toast.makeText(this, "No contestants added", Toast.LENGTH_SHORT).show();
    }

    public class RandomChooserRecyclerAdapter extends RecyclerView.Adapter<RandomChooserRecyclerAdapter.ViewHolder>
            implements CashBoxAdapterSwipable {
        private final List<String> contestants = new ArrayList<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.random_chooser_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.rvRandomChooserContestant.setText(contestants.get(position));
        }

        @Override
        public int getItemCount() {
            return contestants.size();
        }

        @Override
        public boolean isDragEnabled() {
            return false;
        }

        @Override
        public boolean isSwipeEnabled() {
            return true;
        }

        @Override
        public void onItemDelete(int position) {
            contestants.remove(position);
            notifyItemRemoved(position);
            saveContestantsToPreferences();
        }

        @Override
        public void onItemSecondaryAction(int position) {
            new MyDialogBuilder(RandomChooserActivity.this)
                    .setTitle("Change contestant")
                    .setView(R.layout.random_chooser_input_name)
                    .setActions(dialog -> {
                        Button positive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextNameList);
                        TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.layoutTextNameList);

                        inputName.setText(contestants.get(position));
                        inputName.selectAll();
                        Util.showKeyboard(RandomChooserActivity.this, inputName);
                        positive.setOnClickListener((View v) -> {
                            String input = inputName.getText().toString().trim();
                            if (input.isEmpty()) {
                                layoutName.setError("Name cannot be blank");
                                Util.showKeyboard(RandomChooserActivity.this, inputName);
                            } else {
                                contestants.set(position, input);
                                saveContestantsToPreferences();
                                notifyItemChanged(position);
                                dialog.dismiss();
                            }
                        });
                    }).show();
            notifyItemChanged(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView rvRandomChooserContestant;

            public ViewHolder(@NonNull View view) {
                super(view);
                rvRandomChooserContestant = RandomChooserItemBinding.bind(view).rvRandomChooserContestant;
            }
        }
    }

}