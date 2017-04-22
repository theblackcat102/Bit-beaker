package fi.iki.kuitsi.bitbeaker.fragments;

import android.os.Bundle;
import android.util.Log;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import fi.iki.kuitsi.bitbeaker.Bitbeaker;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.adapters.NewsfeedAdapter;
import fi.iki.kuitsi.bitbeaker.event.RefreshEvent;
import fi.iki.kuitsi.bitbeaker.network.RequestNewsfeed;
import fi.iki.kuitsi.bitbeaker.network.RssService;

import zeroone.rss.Channel;

public final class NewsfeedFragment extends ListFragment {
	private static final String TAG = "newsfeed";
	private RequestNewsfeed request;

	private final SpiceManager spiceManager = new SpiceManager(RssService.class);

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Hide progress view
		setAbsListAdapter(new NewsfeedAdapter(getActivity(), new Channel()));
		showAbsList();
	}

	@Override
	public void onStart() {
		super.onStart();
		spiceManager.start(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		Bitbeaker.getEventBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Bitbeaker.getEventBus().unregister(this);
	}

	@Override
	public void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}

	public void reload(String owner, String token) {
		performRequest(owner, token);
	}

	@SuppressWarnings("unused")
	public void onEventMainThread(RefreshEvent event) {
		Log.d(TAG, "onRefreshEvent");
		performRefresh();
	}

	private void performRequest(String owner, String token) {
		Log.d(TAG, "performRequest");
		showProgress();
		request = new RequestNewsfeed(owner, token);
		spiceManager.execute(request, new RssChannelRequestListener());
	}

	private void performRefresh() {
		if (request != null) {
			spiceManager.execute(request, new RssChannelRequestListener());
		}
	}

	final class RssChannelRequestListener implements RequestListener<Channel> {
		@Override
		public void onRequestFailure(SpiceException e) {
			setEmptyText(getString(R.string.rss_loading_failed));
			showAbsList();
			//TODO: ask if user wants to try downloading a new RSS token
		}

		@Override
		public void onRequestSuccess(Channel news) {
			setAbsListAdapter(new NewsfeedAdapter(getActivity(), news));
			showAbsList();
		}
	}
}
