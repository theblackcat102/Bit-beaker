package fi.iki.kuitsi.bitbeaker.fragments;

import android.content.Intent;
import android.os.Bundle;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import fi.iki.kuitsi.bitbeaker.AppComponentService;
import fi.iki.kuitsi.bitbeaker.adapters.PullRequestCommentAdapter;
import fi.iki.kuitsi.bitbeaker.data.ImageLoader;
import fi.iki.kuitsi.bitbeaker.domainobjects.PullRequestComment;
import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.PullRequestCommentRequest;

/**
 * Display pull request comments.
 */
public class PullRequestCommentListFragment extends SpiceCommentListFragment {

	/**
	 * Creates a PullRequestCommentListFragment and sets its arguments.
	 *
	 * @param intent The host activity's intent
	 */
	public static PullRequestCommentListFragment newInstance(Intent intent) {
		PullRequestCommentListFragment fragment = new PullRequestCommentListFragment();
		fragment.setArguments(intent.getExtras());
		return fragment;
	}

	private String owner;
	private String slug;
	private int pullRequestId;
	private PullRequestCommentAdapter adapter;
	private PullRequestCommentLoader loader;

	public PullRequestCommentListFragment() {
		// Mandatory empty constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load arguments
		owner = getArguments().getString("owner");
		slug = getArguments().getString("slug");
		pullRequestId = getArguments().getInt("pullRequestId");

		ImageLoader imageLoader = AppComponentService.obtain(getContext().getApplicationContext()).imageLoader();
		adapter = new PullRequestCommentAdapter(getActivity(), imageLoader);
		loader = new PullRequestCommentLoader(owner, slug, pullRequestId);

		setContentAdapter(adapter, loader);
	}

	@Override
	public void onSubmitComment(final CharSequence comment) {
		getSpiceManager().execute(new BitbucketRequest<Void>(Void.class) {
			@Override
			public Void loadDataFromNetwork() throws Exception {
				getService().postPullRequestComment(owner, slug, pullRequestId, comment).loadDataFromNetwork();
				return null;
			}
		}, new CommentRequestListener());
	}

	private class PullRequestCommentLoader extends ContentLoader<PullRequestComment.List> {

		private final PullRequestCommentRequest request;

		private PullRequestCommentLoader(String owner, String slug, int pullRequestId) {
			request = new PullRequestCommentRequest(owner, slug, pullRequestId);
		}

		@Override
		PullRequestCommentRequest getRequest() {
			return request;
		}

		@Override
		RequestListener<PullRequestComment.List> getRequestListener() {
			return new PullRequestCommentRequestListener();
		}
	}

	final class PullRequestCommentRequestListener implements
			RequestListener<PullRequestComment.List> {

		@Override
		public void onRequestFailure(SpiceException e) {
			loader.notifyFinished(e);
		}

		@Override
		public void onRequestSuccess(PullRequestComment.List comments) {
			adapter.clear();
			// Filter deleted and spam comments
			for (PullRequestComment comment : comments) {
				if (!comment.isDeleted() && !comment.isSpam()) {
					adapter.add(comment);
				}
			}
			loader.notifyFinished();
		}
	}
}
