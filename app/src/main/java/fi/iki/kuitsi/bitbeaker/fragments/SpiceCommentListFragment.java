package fi.iki.kuitsi.bitbeaker.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.EventListenerAdapter;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.activities.BaseActivity;
import fi.iki.kuitsi.bitbeaker.event.RefreshEvent;
import fi.iki.kuitsi.bitbeaker.util.StringUtils;
import fi.iki.kuitsi.bitbeaker.view.FloatingActionButton;

/**
 * {@link SpiceListFragment} that display comments. It has an option menu to add new comments.
 * Inherited class <em>must</em> define {@link #onSubmitComment(CharSequence)} method.
 *
 * @see NewCommentFragment
 */
public abstract class SpiceCommentListFragment extends SpiceListFragment {

	private static final String TAG = "SpiceCommentList";
	private static final String DIALOG_TAG = "dialog";
	private static final int REQUEST_NEW_COMMENT = 0;

	@BindView(R.id.add_comment_button) FloatingActionButton fab;
	private Unbinder unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_comment_list, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		unbinder = ButterKnife.bind(this, view);
		addOnScrollListener(fab.createScrollListener());
	}

	@Override
	public void onDestroyView() {
		unbinder.unbind();
		super.onDestroyView();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_NEW_COMMENT) {
			if (resultCode == Activity.RESULT_OK) {
				CharSequence comment = data.getCharSequenceExtra("comment");
				Log.d(TAG, "comment text= " + comment);
				if (StringUtils.isNotBlank(comment)) {
					onSubmitComment(comment);
					showProgress(true);
				}
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@OnClick(R.id.add_comment_button)
	void addComment(View view) {
		NewCommentFragment dialog = new NewCommentFragment();
		dialog.setTargetFragment(this, REQUEST_NEW_COMMENT);
		dialog.show(getChildFragmentManager(), DIALOG_TAG);
	}

	private void showProgress(boolean visible) {
		((BaseActivity) getActivity()).showProgressBar(visible);
	}

	private void showCommentSubmittedMessage() {
		SnackbarManager.show(
				Snackbar.with(getActivity().getApplicationContext())
						.text(R.string.comment_submitted)
						.eventListener(new EventListenerAdapter() {
							@Override
							public void onShow(Snackbar snackbar) {
								if (isAdded() && fab != null) {
									fab.setMovementY(-snackbar.getHeight());
								}
							}
							@Override
							public void onDismiss(Snackbar snackbar) {
								if (isAdded() && fab != null) {
									fab.setMovementY(0);
								}
							}
						})
				, getActivity());
	}

	/**
	 * Called when 'Submit' (positive button) pressed on {@link NewCommentFragment} dialog.
	 * @param comment The comment entered in the dialog.
	 */
	public abstract void onSubmitComment(CharSequence comment);

	protected class CommentRequestListener implements RequestListener<Void> {

		@Override
		public void onRequestFailure(SpiceException e) {
			showProgress(false);
			Log.d(TAG, "onRequestFailure", e);
		}

		@Override
		public void onRequestSuccess(Void aVoid) {
			showProgress(false);
			showCommentSubmittedMessage();
			onEventMainThread(new RefreshEvent());
		}
	}
}
