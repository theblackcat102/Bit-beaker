package fi.iki.kuitsi.bitbeaker.view;

import android.view.View;
import android.widget.AbsListView;

/**
 * {@link AbsListView.OnScrollListener} that implements {@link ScrollDirectionListener}.
 */
public abstract class AbsListViewScrollDirectionDetector implements AbsListView.OnScrollListener,
		ScrollDirectionListener {

	private static final int DEFAULT_THRESHOLD = 10;

	private int lastScrollY;
	private int previousFirstVisibleItem;
	private final int scrollThreshold;

	public AbsListViewScrollDirectionDetector() {
		this(DEFAULT_THRESHOLD);
	}

	public AbsListViewScrollDirectionDetector(int scrollThreshold) {
		this.scrollThreshold = scrollThreshold;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		if (totalItemCount == 0) return;
		if (isSameRow(firstVisibleItem)) {
			int newScrollY = getTopItemScrollY(view);
			boolean isSignificantDelta = Math.abs(lastScrollY - newScrollY) > scrollThreshold;
			if (isSignificantDelta) {
				if (lastScrollY > newScrollY) {
					onScrollUp();
				} else {
					onScrollDown();
				}
			}
			lastScrollY = newScrollY;
		} else {
			if (firstVisibleItem > previousFirstVisibleItem) {
				onScrollUp();
			} else {
				onScrollDown();
			}
			lastScrollY = getTopItemScrollY(view);
			previousFirstVisibleItem = firstVisibleItem;
		}
	}

	private int getTopItemScrollY(AbsListView listView) {
		if (listView == null || listView.getChildAt(0) == null) return 0;
		View topChild = listView.getChildAt(0);
		return topChild.getTop();
	}

	private boolean isSameRow(int firstVisibleItem) {
		return firstVisibleItem == previousFirstVisibleItem;
	}

	public void reset() {
		lastScrollY = 0;
		previousFirstVisibleItem = 0;
	}

}
