package io.github.andreybgm.gigreminder.screen.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import io.github.andreybgm.gigreminder.R;

public class DiscardDialog extends DialogFragment {

    public static final String ARGUMENT_MESSAGE = "MESSAGE";
    private DiscardDialogListener listener;

    public static DiscardDialog newInstance(int msg) {
        Bundle args = new Bundle();
        args.putInt(ARGUMENT_MESSAGE, msg);

        DiscardDialog dialog = new DiscardDialog();
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (DiscardDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(String.format(
                    "Activity must implement %s", DiscardDialogListener.class.getName()));
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        int msg = args.getInt(ARGUMENT_MESSAGE);

        return new AlertDialog.Builder(getActivity())
                .setMessage(msg)
                .setPositiveButton(
                        R.string.action_discard,
                        (dialog, which) -> listener.onDiscardDialogConfirmClick())
                .setNegativeButton(
                        R.string.action_cancel,
                        (dialog, which) -> {
                        })
                .create();
    }

    public interface DiscardDialogListener {
        void onDiscardDialogConfirmClick();
    }
}
