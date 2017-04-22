package fi.iki.kuitsi.bitbeaker.data.api.resource;

import android.support.annotation.DrawableRes;

public interface DrawableProvider<T> {

	@DrawableRes int getDrawableRes(T input);

}
