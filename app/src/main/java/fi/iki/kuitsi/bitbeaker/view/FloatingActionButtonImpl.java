package fi.iki.kuitsi.bitbeaker.view;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Interface for platform specific FloatingActionButton implementations.
 */
interface FloatingActionButtonImpl {
	int getMeasuredDimension(FloatingActionButtonDelegate fabDelegate);
	Drawable createBackground(FloatingActionButtonDelegate fabDelegate);
	void configureOutline(View view);
}
