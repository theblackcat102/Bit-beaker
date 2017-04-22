package fi.iki.kuitsi.bitbeaker.preferences;

import android.content.Context;
import android.util.AttributeSet;

public final class DialogPreference extends android.support.v7.preference.DialogPreference {

	private OnCloseListener listener;

	public DialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	OnCloseListener getOnCloseListener() {
		return listener;
	}

	void setOnCloseListener(OnCloseListener listener) {
		this.listener = listener;
	}

	interface OnCloseListener {
		void onDialogClosed();
	}
}
