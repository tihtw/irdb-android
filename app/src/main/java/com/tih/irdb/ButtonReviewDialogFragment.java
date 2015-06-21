package com.tih.irdb;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * Created by pichu on 西元15/6/21.
 */
public class ButtonReviewDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        ImageView iv = new ImageView(getActivity());
        iv.setImageBitmap(MainScreenActivity.currentControllerPhoto);

        builder.setMessage("測試")
                .setView(iv)
                .setPositiveButton("發射", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
