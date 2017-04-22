package fi.iki.kuitsi.bitbeaker.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import fi.iki.kuitsi.bitbeaker.R;

/**
 * {@link DialogFragment} that shows instructions how to enable
 * newsfeed.
 */
public class NewsfeedHelpDialogFragment extends DialogFragment {

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.newsfeed_dialog_help_title)
				.setMessage(R.string.newsfeed_dialog_help)
				.setPositiveButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						getActivity().finish();
					}
				});
		return builder.create();
	}
}
