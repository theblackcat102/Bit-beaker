package fi.iki.kuitsi.bitbeaker.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.adapters.ChangesetCommentsAdapter;
import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;
import fi.iki.kuitsi.bitbeaker.domainobjects.ChangesetComment;
import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.ChangesetCommentRequest;
import fi.iki.kuitsi.bitbeaker.view.ChangesetDetailsLayout;

/**
 * Displays changeset comments in ListView and changeset details as a header in portrait mode.
 */
public class ChangesetCommentListFragment extends SpiceCommentListFragment {

	/**
	 * Creates a ChangesetCommentListFragment and sets its arguments.
	 *
	 * @param intent The host activity's intent
	 */
	public static ChangesetCommentListFragment newInstance(Intent intent) {
		ChangesetCommentListFragment fragment = new ChangesetCommentListFragment();
		fragment.setArguments(intent.getExtras());
		return fragment;
	}

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of comment number.
	 */
	public interface Callbacks {
		/**
		 * Callback for when comments have been loaded.
		 */
		void onCommentsLoaded(int num);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks dummyCallbacks = new Callbacks() {
		@Override
		public void onCommentsLoaded(int num) {
		}
	};

	private Callbacks callbacks = dummyCallbacks;
	private String owner;
	private String slug;
	private String node;
	private Changeset changeset;
	private ChangesetCommentsLoader loader;
	private ChangesetCommentsAdapter adapter;
	private ChangesetDetailsLayout header;

	public ChangesetCommentListFragment() {
		// Mandatory empty constructor
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		// Activities containing this fragment must implement its callbacks.
		if (!(context instanceof Callbacks)) {
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		callbacks = (Callbacks) context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		owner = getArguments().getString("owner");
		slug = getArguments().getString("slug");
		changeset = getArguments().getParcelable("changeset");
		node = changeset.getRawNode();

		loader = new ChangesetCommentsLoader(owner, slug, node);
		adapter = new ChangesetCommentsAdapter(getActivity());

		setContentAdapter(adapter, loader);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
			savedInstanceState) {
		if (getResources().getBoolean(R.bool.changeset_header)) {
			header = new ChangesetDetailsLayout(getActivity());
		}
		return super.onCreateView(inflater, container, savedInstanceState);
		// TODO: set empty text
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if (header != null) {
			addHeaderView(header);
			header.setChangeset(changeset);
		}
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		callbacks.onCommentsLoaded(adapter.getCount());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		header = null;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		callbacks = dummyCallbacks;
	}

	@Override
	public void onSubmitComment(final CharSequence comment) {
		getSpiceManager().execute(new BitbucketRequest<Void>(Void.class) {
			@Override
			public Void loadDataFromNetwork() throws Exception {
				getService().postChangesetComment(owner, slug, node, comment).loadDataFromNetwork();
				return null;
			}
		}, new CommentRequestListener());
	}

	private class ChangesetCommentRequestListener
			implements RequestListener<ChangesetComment.List> {
		@Override
		public void onRequestFailure(SpiceException spiceException) {
			loader.notifyFinished(spiceException);
		}

		@Override
		public void onRequestSuccess(ChangesetComment.List changesetComments) {
			callbacks.onCommentsLoaded(changesetComments.size());
			adapter.clear();
			adapter.addAll(changesetComments);
			loader.notifyFinished();
		}
	}

	private class ChangesetCommentsLoader extends ContentLoader<ChangesetComment.List> {

		final ChangesetCommentRequest request;

		private ChangesetCommentsLoader(String owner, String slug, String node) {
			request = new ChangesetCommentRequest(owner, slug, node);
		}

		@Override
		ChangesetCommentRequest getRequest() {
			return request;
		}

		@Override
		RequestListener<ChangesetComment.List> getRequestListener() {
			return new ChangesetCommentRequestListener();
		}
	}
}
