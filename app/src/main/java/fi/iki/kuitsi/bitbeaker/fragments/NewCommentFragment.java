package fi.iki.kuitsi.bitbeaker.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.R;

/**
 * DialogFragment for commenting.
 */
public class NewCommentFragment extends DialogFragment implements TextView.OnEditorActionListener {

	private EditText editText;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		@SuppressLint("InflateParams")
		final View view = inflater.inflate(R.layout.dialog_text_entry, null);
		editText = (EditText) view.findViewById(R.id.text_entry);
		editText.setOnEditorActionListener(this);
		editText.requestFocus();
		// Build the dialog and set up the button click handlers
		Dialog dialog = new AlertDialog.Builder(getActivity())
				.setTitle(R.string.add_comment)
				.setView(view)
				.setPositiveButton(R.string.submit_comment, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						submit();
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				})
				.create();
		// Show soft keyboard automatically
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return dialog;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (EditorInfo.IME_ACTION_SEND == actionId) {
			submit();
			return true;
		}
		return false;
	}

	private void submit() {
		// Return input text to activity
		if (getTargetFragment() == null) return;

		Intent intent = new Intent();
		intent.putExtra("comment", editText.getText());
		getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
	}
}
