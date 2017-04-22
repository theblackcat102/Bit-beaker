package fi.iki.kuitsi.bitbeaker.view;

import android.content.Context;
import android.util.AttributeSet;

public class SwipeRefreshLayout extends android.support.v4.widget.SwipeRefreshLayout {

	private CanChildScrollUpCallback callback;

	public SwipeRefreshLayout(Context context) {
		super(context);
	}

	public SwipeRefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean canChildScrollUp() {
		if (callback != null) {
			return callback.canSwipeRefreshChildScrollUp();
		}
		return super.canChildScrollUp();
	}

	public void setCanChildScrollUpCallback(CanChildScrollUpCallback callback) {
		this.callback = callback;
	}

	public interface CanChildScrollUpCallback {
		boolean canSwipeRefreshChildScrollUp();
	}

	public interface OnRefreshListener extends
			android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener {
	}
}
