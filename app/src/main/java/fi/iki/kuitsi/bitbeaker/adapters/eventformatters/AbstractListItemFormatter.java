package fi.iki.kuitsi.bitbeaker.adapters.eventformatters;

import android.content.Context;
import android.view.View.OnClickListener;

public abstract class AbstractListItemFormatter<T> implements ListItemFormatter {
	private final T item;
	private final Context context;

	public AbstractListItemFormatter(Context context, T item) {
		this.item = item;
		this.context = context;
	}

	protected final T getItem() {
		return item;
	}

	protected final Context getContext() {
		return context;
	}

	@Override
	public abstract String getTitle();

	@Override
	public abstract String getSubtitle();

	@Override
	public OnClickListener getClickListener() {
		return null;
	}
}
