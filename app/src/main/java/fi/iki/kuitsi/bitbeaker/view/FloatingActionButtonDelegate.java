package fi.iki.kuitsi.bitbeaker.view;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Interface provided by FloatingActionButton to implementations.
 *
 * Necessary to resolve circular dependency between base FloatingActionButton
 * and platform implementations.
 */
interface FloatingActionButtonDelegate {
	int getColorNormal();
	int getColorPressed();
	int getColorRipple();
	Drawable getShadowDrawable();
	int getShadowInset();
	int getSize();
	View getView();
	boolean isMarginsConfigured();
	void setMarginsConfigured();
}
