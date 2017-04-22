package fi.iki.kuitsi.bitbeaker.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class that can be used to implement a fragment that displays a list of items by binding to
 * a data source. ListFragment has a default layout that consists of a single list view. You can
 * customize the fragment layout by returning a custom view hierarchy from
 * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
 * To do this, your view <em>must</em> contain an {@link AbsListView} object with the id
 * "@android:id/list". It can be a {@link android.widget.ListView ListView} or a
 * {@link android.widget.GridView GridView}.
 */
public class ListFragment extends Fragment {

	private static final String TAG = "ListFragment";

	private final AdapterView.OnItemClickListener onClickListener = new AdapterView
			.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			onListItemClick((AbsListView) parent, v, position, id);
		}
	};

	/** The parent of the {@link android.widget.ProgressBar} view. */
	private View progressContainer;

	/** The parent of the {@link #absList}. */
	private View listContainer;

	/** The fragment's ListView/GridView. */
	private AbsListView absList;

	/** Storage for header views. */
	private final List<View> headerViews = new ArrayList<>();

	/** The Adapter which will be used to populate {@link #absList} with Views. */
	private ListAdapter adapter;

	/** True if the ListView/GridView is shown. */
	private boolean absListShown;

	/**
	 * Provide default implementation to return a simple list view.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_content, container, false);
	}

	/**
	 * Attach to list view once the view hierarchy has been created.
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ensureAbsList();
	}

	/**
	 * Detach from abstract list view.
	 */
	@Override
	public void onDestroyView() {
		headerViews.clear();
		absList = null;
		absListShown = false;
		listContainer = null;
		progressContainer = null;
		super.onDestroyView();
	}

	/**
	 * This method will be called when an item in the list is selected.
	 *
	 * @param l The AbsListView where the click happened
	 * @param v The view that was clicked within the AbsListView
	 * @param position The position of the view in the list
	 * @param id The row id of the item that was clicked
	 */
	public void onListItemClick(AbsListView l, View v, int position, long id) {
	}

	/**
	 * Get the fragment's abstract list view widget.
	 */
	public AbsListView getAbsListView() {
		ensureAbsList();
		return absList;
	}

	public ListAdapter getAbsListAdapter() {
		return adapter;
	}

	public void setAbsListAdapter(ListAdapter adapter) {
		boolean hadAdapter = this.adapter != null;
		this.adapter = adapter;
		if (absList != null) {
			// setAbsListAdapter of AbsListView is available since API level 11
			//noinspection RedundantCast
			((AdapterView<ListAdapter>) getAbsListView()).setAdapter(adapter);
			if (!absListShown && !hadAdapter) {
				showAbsList();
			}
		}
	}

	/**
	 * The default content for this Fragment has a TextView that is shown when the list is empty.
	 *
	 * @param resId String resource ID.
	 * @see #setEmptyText(CharSequence)
	 */
	public void setEmptyText(int resId) {
		setEmptyText(getString(resId));
	}

	/**
	 * The default content for this Fragment has a TextView that is shown when the list is empty.
	 * If you would like to change the text, call this method to supply the text it should use.
	 */
	public void setEmptyText(CharSequence emptyText) {
		View emptyView = absList.getEmptyView();

		if (emptyView instanceof TextView) {
			((TextView) emptyView).setText(emptyText);
		}
	}

	/**
	 * Control whether the list is being displayed.
	 */
	public void showAbsList() {
		if (absListShown) return;
		absListShown = true;
		progressContainer.setVisibility(View.GONE);
		listContainer.setVisibility(View.VISIBLE);
	}

	/**
	 * Control whether the progress indicator is being displayed.
	 */
	public void showProgress() {
		if (!absListShown) return;
		absListShown = false;
		progressContainer.setVisibility(View.VISIBLE);
		listContainer.setVisibility(View.GONE);
	}

	/**
	 * Add a fixed view to appear at the top of the list. If addHeaderView is called more than once,
	 * the views will appear in the order they were added. Views must be added before the {@link
	 * #onViewCreated(android.view.View, android.os.Bundle)}.
	 * @param header View to add
	 */
	public void addHeaderView(View header) {
		headerViews.add(header);
	}

	/**
	 * Sets top padding of AbsListView.
	 * @param top the top padding in pixels
	 */
	public void setPaddingTop(int top) {
		Rect rect = new Rect(absList.getPaddingLeft(), top, absList.getPaddingRight(),
				absList.getPaddingBottom());
		setInsets(rect);
	}

	/**
      * Sets the padding of AbsListView.
	  */
	public void setInsets(Rect insets) {
		absList.setClipToPadding(false);
		absList.setPadding(insets.left, insets.top, insets.right, insets.bottom);
	}

	/**
	 * Check if this view can be scrolled up.
	 */
	public boolean canScrollUp() {
		return absListShown && absList != null && canAbsListScrollUp();
	}

	private boolean canAbsListScrollUp() {
		return ViewCompat.canScrollVertically(absList, -1);
	}

	private void ensureAbsList() {
		if (absList != null) {
			return;
		}

		View root = getView();
		if (root == null) {
			throw new IllegalStateException("Content view not yet created");
		}

		progressContainer = root.findViewById(R.id.progress_container);
		listContainer = root.findViewById(R.id.list_container);
		absList = (AbsListView) listContainer.findViewById(android.R.id.list);

		// Set headers
		if (!headerViews.isEmpty()) {
			if (absList instanceof ListView) {
				for (View v : headerViews) {
					((ListView) absList).addHeaderView(v);
				}
			} else {
				Log.e(TAG, "Header support is only available for ListView");
			}
		}

		// Set the empty view
		absList.setEmptyView(listContainer.findViewById(android.R.id.empty));

		// Set OnItemClickListener so we can be notified on item clicks
		absList.setOnItemClickListener(onClickListener);

		absListShown = true;
		if (adapter != null) {
			setAbsListAdapter(adapter);
		} else {
			showProgress();
		}
	}
}
