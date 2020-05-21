package com.vibal.utilities.util;

import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.vibal.utilities.R;

public class MyDialogBuilder extends AlertDialog.Builder {
    private static final int POSITIVE_RES = R.string.confirm;
    private static final int NEGATIVE_RES = R.string.cancelDialog;
    private static final boolean CANCELABLE = true;
    private boolean cancelOnTouchOutside = false;
    private DialogInterface.OnShowListener actions;

    public MyDialogBuilder(@NonNull Context context) {
        super(context);
        setNegativeButton(null);
        setPositiveButton(null);
    }

    public MyDialogBuilder setPositiveButton(DialogInterface.OnClickListener listener) {
        return setPositiveButton(POSITIVE_RES, listener);
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
        if (actions != null)
            dialog.setOnShowListener(actions);
        dialog.setCancelable(CANCELABLE);
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

    @Override
    public MyDialogBuilder setPositiveButton(int textId, DialogInterface.OnClickListener listener) {
        return (MyDialogBuilder) super.setPositiveButton(textId, listener);
    }


    @Override
    public MyDialogBuilder setView(int layoutResId) {
        return (MyDialogBuilder) super.setView(layoutResId);
    }

    @Override
    public MyDialogBuilder setTitle(int titleId) {
        return (MyDialogBuilder) super.setTitle(titleId);
    }

    @Override
    public MyDialogBuilder setTitle(@Nullable CharSequence title) {
        return (MyDialogBuilder) super.setTitle(title);
    }

    @Override
    public MyDialogBuilder setMessage(int messageId) {
        return (MyDialogBuilder) super.setMessage(messageId);
    }

    @Override
    public MyDialogBuilder setMessage(@Nullable CharSequence message) {
        return (MyDialogBuilder) super.setMessage(message);
    }

    @Override
    public MyDialogBuilder setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        return (MyDialogBuilder) super.setOnDismissListener(onDismissListener);
    }
}
