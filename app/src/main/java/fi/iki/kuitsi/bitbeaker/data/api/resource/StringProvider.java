package fi.iki.kuitsi.bitbeaker.data.api.resource;

import android.support.annotation.StringRes;

public interface StringProvider<T> {
	@StringRes int getStringRes(T input);
}
