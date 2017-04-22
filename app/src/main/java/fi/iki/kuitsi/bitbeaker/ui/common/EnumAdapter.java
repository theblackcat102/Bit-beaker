package fi.iki.kuitsi.bitbeaker.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;

abstract class EnumAdapter<T extends Enum<T>> extends BindableAdapter<T> {

	private final T[] enumConstants;

	public EnumAdapter(Context context, Class<T> enumType) {
		super(context);
		this.enumConstants = enumType.getEnumConstants();
	}

	@Override
	public final int getCount() {
		return enumConstants.length;
	}

	@Override
	public final T getItem(int position) {
		return enumConstants[position];
	}

	@Override
	public final long getItemId(int position) {
		return position;
	}

	@Override
	protected final View newView(LayoutInflater inflater, int position, ViewGroup container) {
		return inflater.inflate(android.R.layout.simple_spinner_item, container, false);
	}

	@Override
	protected final void bindView(T item, int position, View view) {
		TextView tv = ButterKnife.findById(view, android.R.id.text1);
		tv.setText(getName(item));
	}

	@Override
	public final View newDropDownView(LayoutInflater inflater, int position, ViewGroup container) {
		return inflater.inflate(android.R.layout.simple_spinner_dropdown_item, container, false);
	}

	protected String getName(T item) {
		return String.valueOf(item);
	}
}
