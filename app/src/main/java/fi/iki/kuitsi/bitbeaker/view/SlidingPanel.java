package fi.iki.kuitsi.bitbeaker.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

import fi.iki.kuitsi.bitbeaker.R;

/**
 * Modified {@link SlidingPaneLayout}. Left/first/sliding pane remains partially visible.
 */
public class SlidingPanel extends SlidingPaneLayout {

	private final int collapsedWidth;
	private final int expandedWidth;
	/** X coordinate of latest pressed gesture motion event. */
	private float actionDown = 0.0f;

	public SlidingPanel(Context context) {
		this(context, null, 0);
	}

	public SlidingPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidingPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SlidingPanel);

		collapsedWidth = array.getDimensionPixelSize(
				R.styleable.SlidingPanel_slidingPanelCollapsedWidth, 0);
		expandedWidth = array.getDimensionPixelOffset(
				R.styleable.SlidingPanel_slidingPanelExpandedWidth, 0);

		array.recycle();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			// A pressed gesture has started, the motion contains the initial starting location.
			actionDown = ev.getX();
			return super.onInterceptTouchEvent(ev);
		} else if (action == MotionEvent.ACTION_MOVE) {
			// A change has happened during a press gesture (between ACTION_DOWN and ACTION_UP).
			// Steal motion event from super class.
			// Return true to steal motion events from the children.
			return ((isOpen() && actionDown > ev.getX() && actionDown > expandedWidth)
					|| (!isOpen() && actionDown < ev.getX() && actionDown < collapsedWidth));
		} else {
			return super.onInterceptTouchEvent(ev);
		}
	}

}
