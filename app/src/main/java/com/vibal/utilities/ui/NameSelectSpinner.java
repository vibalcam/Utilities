package com.vibal.utilities.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.vibal.utilities.R;
import com.vibal.utilities.models.EntryBase;
import com.vibal.utilities.util.MyDialogBuilder;
import com.vibal.utilities.util.Util;

import java.util.ArrayList;
import java.util.List;

public class NameSelectSpinner extends MaterialSpinner {
    private static final int INDEX_ADD = 0;    // add option in first position of spinner
    private static final int INDEX_DEFAULT = 1;
    private static final String STRING_ADD = "New Name";

    public NameSelectSpinner(Context context) {
        super(context);
        config(null);
    }

    public NameSelectSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        config(null);
    }

    public NameSelectSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        config(null);
    }

    public void config(List<String> names) {
//        if (names == null || names.isEmpty())
//            throw new IllegalArgumentException("List for names spinner should not be null nor empty");
        if (names == null)
            names = new ArrayList<>();
        // Add new option
        names.add(INDEX_ADD, STRING_ADD);
        // Self option as second option
        names.remove(EntryBase.getSelfName());
        names.add(1, EntryBase.getSelfName());
        // Set items and select self option
        setItems(names);
        setSelectedIndex(INDEX_DEFAULT);
        // Set onItemSelectedListener
        setOnItemSelectedListener(new NewNameItemSelectedListener());
    }

    public String getSelectedString() {
        int idx = getSelectedIndex();
        if (idx == -1)
            return null;
        List<String> names = getItems();
        if (names.size() <= 1 || !(names.get(0) instanceof String))
            return null;

        return names.get(idx);
    }

    public static MyDialogBuilder createAddParticipantDialog(Context context, NameDialogListener listener) {
        return new MyDialogBuilder(context)
                .setTitle("New Participant")
                .setView(R.layout.cash_box_input_name)
                .setActions(dialog -> {
                    Button positive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextChangeName);
                    TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutChangeName);

                    Util.showKeyboard(context, inputName);
                    positive.setOnClickListener((View v) -> {
                        String input = inputName.getText().toString().trim();
                        if (input.isEmpty()) {
                            layoutName.setError("Name cannot be blank");
                            Util.showKeyboard(context, inputName);
                            return;
                        }
                        listener.onCorrect(dialog, inputName, layoutName);
                    });
                });
    }

    public interface NameDialogListener {
        void onCorrect(DialogInterface dialog, TextInputEditText inputName, TextInputLayout layoutName);
    }

    private static class NewNameItemSelectedListener implements MaterialSpinner.OnItemSelectedListener<String> {
        @Override
        public void onItemSelected(MaterialSpinner spinner, int position, long id, String item) {
            if (position != INDEX_ADD)
                return;

            Context context = spinner.getContext();
            createAddParticipantDialog(context, (dialog, inputName, layoutName) -> {
                String input = inputName.getText().toString().trim();
                List<String> names = spinner.getItems();
                int pos = names.indexOf(input);
                if (pos != -1) {
                    layoutName.setError("Name already exists");
                    spinner.setSelectedIndex(pos);
                    dialog.dismiss();
                    return;
                }
                names.add(INDEX_DEFAULT + 1, input);
                spinner.setSelectedIndex(INDEX_DEFAULT + 1);
                dialog.dismiss();
            }).setNegativeButton((dialogInterface, i) -> spinner.setSelectedIndex(INDEX_DEFAULT))
                    .show();
        }
    }

//    public static class NameCheckActions implements DialogInterface.OnShowListener {
//        private NameDialogListener listener;
//        public NameCheckActions(NameDialogListener listener) {
//            this.listener = listener;
//        }
//
//        @Override
//        public void onShow(DialogInterface dialog) {
//            Button positive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//            TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextChangeName);
//            TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutChangeName);
//            Context context = ((AlertDialog) dialog).getContext();
//
//            Util.showKeyboard(context, inputName);
//            positive.setOnClickListener((View v) -> {
//                String input = inputName.getText().toString().trim();
//                if (input.isEmpty()) {
//                    layoutName.setError("Name cannot be blank");
//                    Util.showKeyboard(context, inputName);
//                    return;
//                }
//                listener.onCorrect(dialog, inputName, layoutName);
//            });
//        }
//    }
//
//    private static class NewNameItemSelectedListener implements MaterialSpinner.OnItemSelectedListener<String> {
//        @Override
//        public void onItemSelected(MaterialSpinner spinner, int position, long id, String item) {
//            if (position != INDEX_ADD)
//                return;
//
//            Context context = spinner.getContext();
//            new MyDialogBuilder(context)
//                    .setTitle("New Participant")
//                    .setView(R.layout.cash_box_input_name)
//                    .setNegativeButton((dialogInterface, i) -> spinner.setSelectedIndex(INDEX_DEFAULT))
//                    .setActions(new NameCheckActions((dialog, inputName, layoutName) -> {
//                        String input = inputName.getText().toString().trim();
//                        List<String> names = spinner.getItems();
//                        int pos = names.indexOf(input);
//                        if (pos != -1) {
//                            layoutName.setError("Name already exists");
//                            spinner.setSelectedIndex(pos);
//                            dialog.dismiss();
//                            return;
//                        }
//                        names.add(INDEX_DEFAULT + 1, input);
//                        spinner.setSelectedIndex(INDEX_DEFAULT + 1);
//                        dialog.dismiss();
//                    })).show();
//        }
//    }
}
