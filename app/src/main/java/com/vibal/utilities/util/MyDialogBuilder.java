package com.vibal.utilities.util;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.vibal.utilities.R;

public class MyDialogBuilder extends AlertDialog.Builder {
    private static final int POSITIVE_RES =  R.string.confirm;
    private static final int NEGATIVE_RES =  R.string.cancelDialog;
//    private boolean dismissAfterPositive = true;
    private boolean cancelOnTouchOutside = false;
//    private MyDialogAction actions;
    private DialogInterface.OnShowListener actions;

    public MyDialogBuilder(@NonNull Context context) {
        super(context);
        setNegativeButton(null);
        setPositiveButton(null);
    }

//    public MyDialogBuilder(@NonNull Context context, boolean dismissAfterPositive) {
//        this(context);
//        this.dismissAfterPositive = dismissAfterPositive;
//    }


    public MyDialogBuilder setPositiveButton(DialogInterface.OnClickListener listener) {
        super.setPositiveButton(POSITIVE_RES, listener);
        return this;
    }

    public MyDialogBuilder setNegativeButton(DialogInterface.OnClickListener listener) {
        super.setNegativeButton(NEGATIVE_RES, listener);
        return this;
    }

    @NonNull
    @Override
    public AlertDialog create() {
        AlertDialog dialog = super.create();
        setupDialog(dialog);
        return dialog;
    }

    private void setupDialog(AlertDialog dialog) {
        if(actions != null)
            dialog.setOnShowListener(actions);
//        dialog.setOnShowListener(dialog1 -> {
//            Button positive = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
//            Button negative = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_NEGATIVE);
//            Button neutral = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_NEUTRAL);
//
//            positive.setOnClickListener(v -> actions.doOnPositive(dialog1));
//
//            neutral.setOnClickListener(v -> actions.doOnNeutral(dialog1));
//
//            negative.setOnClickListener(v -> actions.doOnNegative(dialog1));
//        });
        dialog.setCanceledOnTouchOutside(cancelOnTouchOutside);
    }

    public MyDialogBuilder setCancelOnTouchOutside(boolean cancelOnTouchOutside) {
        this.cancelOnTouchOutside = cancelOnTouchOutside;
        return this;
    }

    public MyDialogBuilder setActions(DialogInterface.OnShowListener actions) {
        this.actions = actions;
        return this;
    }

//    public MyDialogBuilder setDismissAfterPositive(boolean dismissAfterPositive) {
//        this.dismissAfterPositive = dismissAfterPositive;
//        return this;
//    }

//    public MyDialogBuilder setActions(MyDialogAction actions) {
//        this.actions = actions;
//        return this;
//    }


//    public interface MyDialogAction {
//        void doOnPositive(DialogInterface dialog);
//        void doOnNeutral(DialogInterface dialog);
//        void doOnNegative(DialogInterface dialog);
//    }

//    public static class MyDialogActionBase implements MyDialogAction {
//        @Override
//        public void doOnPositive(DialogInterface dialog) {
//            dialog.dismiss();
//        }
//        @Override
//        public void doOnNeutral(DialogInterface dialog) {
//            dialog.dismiss();
//        }
//        @Override
//        public void doOnNegative(DialogInterface dialog) {
//            dialog.dismiss();
//        }
//    }
}
