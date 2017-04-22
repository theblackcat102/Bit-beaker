package fi.iki.kuitsi.bitbeaker.fragments;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import fi.iki.kuitsi.bitbeaker.Bitbeaker;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.event.RefreshEvent;
import fi.iki.kuitsi.bitbeaker.network.RestService;
import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;

import java.io.FileDescriptor;
import java.io.PrintWriter;

/**
 * Base class that can be used to implement a fragment that displays a list of items by binding to
 * a query results of a {@link BitbucketRequest}.
 * <p>
 * Inherited class <em>must</em> define its {@link SpiceListFragment.ContentLoader} and call {@link
 * #setContentAdapter(ListAdapter, SpiceListFragment.ContentLoader)} in {@link #onCreate(Bundle)}.
 * </p>
 * <p>
 * <strong>Features</strong>
 * </p>
 * <ul>
 * <li>Support refresh ({@link de.greenrobot.event.EventBus},
 * {@link fi.iki.kuitsi.bitbeaker.event.RefreshEvent})</li>
 * <li>Support endless scrolling (automatically loads more items as the user scrolls through
 * the items) ({@link SpiceListFragment.ContentAdapterDecorator},
 * {@link SpiceListFragment.ListScrollListener})</li>
 * </ul>
 *
 * @see #setContentAdapter
 */
abstract class SpiceListFragment extends ListFragment {

	private static final String TAG = SpiceListFragment.class.getSimpleName();

	/**
	 * The {@link com.octo.android.robospice.SpiceManager} allow to access the
	 * {@link RestService}.
	 */
	private final SpiceManager spiceManager = new SpiceManager(RestService.class);
	private boolean endlessScrolling;
	private ListScrollListener scrollListener;
	private AbsListView.OnScrollListener scrollListenerDelegate;

	/** The ContentLoader which performs loading. */
	private ContentLoader loader;

	/** Content should be refreshed. */
	private boolean contentDirty;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		Bitbeaker.getEventBus().register(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ensureContent();
	}

	/**
	 * Start loading.
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (loader != null) {
			if (!loader.isFinished()) {
				loader.load();
			} else if (loader.isLoading()) {
				// Last load hasn't been completed, restart it
				loader.restart();
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		spiceManager.start(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		if (loader != null) {
			if (contentDirty && (!loader.isLoading() || loader.isLoadingMore())) {
				loader.reload();
				contentDirty = false;
			} else if (loader.isLoadingMore() && !loader.isRestarted()) {
				loader.restart();
			}
		}
	}

	@Override
	public void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}

	@Override
	public void onDetach() {
		Bitbeaker.getEventBus().unregister(this);
		super.onDetach();
	}

	/**
	 * Set the listener that will receive notifications every time the list scrolls.
	 * @param l the scroll listener
	 */
	public void addOnScrollListener(AbsListView.OnScrollListener l) {
		if (endlessScrolling) {
			scrollListenerDelegate = l;
			if (scrollListener != null) {
				scrollListener.setScrollListenerDelegate(l);
			}
		} else {
			getAbsListView().setOnScrollListener(l);
		}
	}

	/**
	 * Return the SpiceManager for this fragment.
	 */
	SpiceManager getSpiceManager() {
		return spiceManager;
	}

	protected void setEndlessScrollingSupport(boolean enable) {
		endlessScrolling = enable;
	}

	/**
	 * Provide the adapter for the abstract list view.
	 */
	protected void setContentAdapter(ListAdapter adapter, ContentLoader loader) {
		setAbsListAdapter(adapter);
		this.loader = loader;
	}

	protected void setContentLoader(ContentLoader loader) {
		this.loader = loader;
		ensureContent();
	}

	/**
	 * Handle refresh event.
	 *
	 * @param event Refresh event
	 */
	@SuppressWarnings("unused")
	public void onEventMainThread(RefreshEvent event) {
		if (isVisible() && loader != null) {
			loader.reload();
		} else {
			contentDirty = true;
		}
	}

	private void ensureContent() {
		if (loader == null) return;
		if (endlessScrolling) {
			ListAdapter adapter = getAbsListAdapter();
			if (adapter == null) {
				Log.e(TAG, "Adapter must be set before view is created.");
				return;
			}

			// Set the adapter
			ListAdapter contentAdapter;
			if (adapter instanceof ContentAdapterDecorator) {
				ListAdapter wrappedAdapter = ((WrapperListAdapter) adapter).getWrappedAdapter();
				contentAdapter = new ContentAdapterDecorator(wrappedAdapter, loader);
			} else {
				contentAdapter = new ContentAdapterDecorator(adapter, loader);
			}
			super.setAbsListAdapter(contentAdapter);

			// Set OnScrollListener so we can load more items
			scrollListener = new ListScrollListener(loader);
			getAbsListView().setOnScrollListener(scrollListener);
			if (scrollListenerDelegate != null) {
				scrollListener.setScrollListenerDelegate(scrollListenerDelegate);
			}
		}
	}

	/**
	 * Scroll listener that triggers {@link SpiceListFragment.ContentLoader#loadMore()}.
	 */
	static class ListScrollListener implements AbsListView.OnScrollListener {

		private final ContentLoader loader;
		private AbsListView.OnScrollListener delegateOnScrollListener;

		ListScrollListener(ContentLoader loader) {
			this.loader = loader;
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
				int totalItemCount) {
			if (delegateOnScrollListener != null) {
				delegateOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
						totalItemCount);
			}
			if (!loader.isLoading() && loader.hasMore()
					&& (firstVisibleItem + visibleItemCount) >= (totalItemCount - 1)) {
				loader.loadMore();
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (delegateOnScrollListener != null) {
				delegateOnScrollListener.onScrollStateChanged(view, scrollState);
			}
		}

		void setScrollListenerDelegate(AbsListView.OnScrollListener l) {
			delegateOnScrollListener = l;
		}
	}

	/**
	 * List adapter that wraps the list adapter of the inherited class. It displays
	 * the {@link #loadingView} at the end of the list if the list has more items.
	 */
	static class ContentAdapterDecorator implements WrapperListAdapter {

		private final ContentLoader loader;
		private final ListAdapter listAdapter;
		private View loadingView;

		ContentAdapterDecorator(ListAdapter listAdapter, ContentLoader loader) {
			this.loader = loader;
			this.listAdapter = listAdapter;
		}

		@Override
		public ListAdapter getWrappedAdapter() {
			return listAdapter;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			return isItem(position) && listAdapter.isEnabled(position);
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
			listAdapter.registerDataSetObserver(observer);
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
			listAdapter.unregisterDataSetObserver(observer);
		}

		@Override
		public int getCount() {
			int count = listAdapter.getCount();
			if (loader.hasMore()) {
				count++;
			}
			return count;
		}

		@Override
		public Object getItem(int position) {
			if (isItem(position)) {
				return listAdapter.getItem(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			if (isItem(position)) {
				return listAdapter.getItemId(position);
			}
			return AdapterView.INVALID_ROW_ID;
		}

		@Override
		public boolean hasStableIds() {
			return listAdapter.hasStableIds();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (isItem(position)) {
				return listAdapter.getView(position, convertView, parent);
			} else if (loader.hasMore()) {
				return getLoadingView(parent);
			}
			throw new IndexOutOfBoundsException();
		}

		@Override
		public int getItemViewType(int position) {
			if (isItem(position)) {
				return listAdapter.getItemViewType(position);
			} else if (position == listAdapter.getCount()) {
				return AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
			}
			return AdapterView.ITEM_VIEW_TYPE_IGNORE;
		}

		@Override
		public int getViewTypeCount() {
			return listAdapter.getViewTypeCount() + 1;
		}

		@Override
		public boolean isEmpty() {
			return listAdapter.getCount() == 0;
		}

		private boolean isItem(int position) {
			return position < listAdapter.getCount();
		}

		private View getLoadingView(ViewGroup parent) {
			if (loadingView == null) {
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				loadingView = inflater.inflate(R.layout.listfooter_loading, parent, false);
			}
			return loadingView;
		}
	}

	/**
	 * An abstract class that performs loading of data using SpiceManager.
	 *
	 * @param <RESULT> Result type
	 */
	abstract class ContentLoader<RESULT> {

		private boolean loading;
		private boolean loadingMore;
		private boolean finished;
		private boolean restarted;

		/**
		 * Get {@link BitbucketRequest} to execute.
		 *
		 * @see #onLoad()
		 * @see #onReload()
		 * @see #onLoadMore()
		 */
		abstract BitbucketRequest<RESULT> getRequest();

		/**
		 * Instantiate and return a new RequestListener.
		 */
		abstract RequestListener<RESULT> getRequestListener();

		ContentLoader() {
			this.loading = false;
			this.loadingMore = false;
			this.finished = false;
			this.restarted = false;
		}

		/**
		 * Execute the request with default cache expire duration. Progress indicator is being
		 * displayed.
		 */
		public void load() {
			onLoad();
			BitbucketRequest<RESULT> request = getRequest();
			getSpiceManager().execute(request, request.getCacheKey(),
					request.getCacheExpireDuration(), getRequestListener());
			loading = true;
			loadingMore = false;
			restarted = false;
			showProgress();
		}

		/**
		 * Called by {@link #load()} before it gets the request. Override this method to prepare
		 * the request for first loading.
		 */
		void onLoad() {
		}

		/**
		 * Execute the request (a network call will always be performed).
		 */
		public void reload() {
			onReload();
			BitbucketRequest<RESULT> request = getRequest();
			getSpiceManager().execute(request, request.getCacheKey(),
					DurationInMillis.ALWAYS_EXPIRED, getRequestListener());
			loading = true;
			loadingMore = false;
			restarted = false;
		}

		/**
		 * Called by {@link #reload()} before it gets the request. Override this method to prepare
		 * the request for reloading.
		 */
		void onReload() {
		}

		/**
		 * Execute the request without cache.
		 */
		void loadMore() {
			onLoadMore();
			getSpiceManager().execute(getRequest(), getRequestListener());
			loading = true;
			loadingMore = true;
			restarted = false;
		}

		/**
		 * Called by {@link #loadMore()} before it gets the request. Override this method to prepare
		 * the request for reloading.
		 */
		void onLoadMore() {
		}

		/**
		 * Execute last request again.
		 */
		void restart() {
			BitbucketRequest<RESULT> request = getRequest();
			if (request != null) {
				getSpiceManager().execute(request, getRequestListener());
				restarted = true;
			}
		}

		/**
		 * Return whether this load has been started.
		 */
		public boolean isLoading() {
			return loading;
		}

		/**
		 * Return whether this load more has been started.
		 */
		public boolean isLoadingMore() {
			return loadingMore;
		}

		/**
		 * Return whether this load has been restarted.
		 */
		public boolean isRestarted() {
			return restarted;
		}

		/**
		 * Return whether the loader has more elements. Override this method for endless scrolling
		 * support.
		 */
		boolean hasMore() {
			return false;
		}

		/**
		 * Return whether this load has been finished.
		 */
		public boolean isFinished() {
			return finished;
		}

		private void setFinished(boolean finished) {
			this.loading = false;
			this.loadingMore = false;
			this.finished = finished;
			this.restarted = false;
			showAbsList();
		}

		/**
		 * Notify loader that the loading finished successfully.
		 */
		public void notifyFinished() {
			setFinished(true);
		}

		/**
		 * Notify loader that the loading finished with an exception.
		 *
		 * @param e SpiceException
		 */
		public void notifyFinished(SpiceException e) {
			Log.e(TAG, "onRequestFailure", e);
			setFinished(false);
		}

		/**
		 * Print the loader's state into the given stream.
		 *
		 * @param prefix Text to print at the front of each line.
		 * @param fd The raw file descriptor that the dump is being sent to.
		 * @param writer The PrintWriter to which you should dump your state.  This will be
		 * closed for you after you return.
		 * @param args additional arguments to the dump request.
		 */
		public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
			writer.print(prefix);
			writer.print("loading=");
			writer.print(loading);
			writer.print(" loadingMore=");
			writer.print(loadingMore);
			writer.print(" finished=");
			writer.print(finished);
			writer.print(" restarted=");
			writer.println(restarted);
		}
	}
}
