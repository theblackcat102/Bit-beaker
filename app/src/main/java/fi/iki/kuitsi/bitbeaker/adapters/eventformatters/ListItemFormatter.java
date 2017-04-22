package fi.iki.kuitsi.bitbeaker.adapters.eventformatters;

import android.view.View.OnClickListener;

/**
 * This interface defines the necessary methods to format a simple
 * item with two text fields in a ListView.
 */
public interface ListItemFormatter {
	String getTitle();

	String getSubtitle();

	OnClickListener getClickListener();
}
