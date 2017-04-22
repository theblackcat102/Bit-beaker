package fi.iki.kuitsi.bitbeaker.ui.common;

import android.content.Context;

import fi.iki.kuitsi.bitbeaker.data.api.resource.StringProvider;

public final class ApiEnumAdapter<T extends Enum<T>> extends EnumAdapter<T> {

	private final StringProvider<T> stringProvider;

	public ApiEnumAdapter(Context context, Class<T> enumType, StringProvider<T> stringProvider) {
		super(context, enumType);
		this.stringProvider = stringProvider;
	}

	@Override
	protected String getName(T item) {
		return getString(stringProvider.getStringRes(item));
	}
}
