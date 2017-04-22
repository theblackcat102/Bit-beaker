package fi.iki.kuitsi.bitbeaker.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.ImageButton;

import fi.iki.kuitsi.bitbeaker.R;

/**
 * A circular button made of paper that lifts and emits ink reactions on press. Floating action
 * buttons come in two sizes, but current implementation supports only the default size. It extends
 * {@link ImageButton} so {@code android:src} attribute is required. Use a {@code 24dp} drawable.
 * The widget automatically creates its own background drawables based on the color values (see
 * {@link FloatingActionButton#colorNormal}, {@link FloatingActionButton#colorPressed},
 * {@link FloatingActionButton#colorRipple}).
 *
 * @see <a href="http://www.google.com/design/spec/components/buttons.html#buttons-floating-action-button">Buttons - Components - Google design guidelines</a>
 */
public class FloatingActionButton extends ImageButton implements FloatingActionButtonDelegate {

	private static final int TRANSLATE_DURATION_MILLIS = 300;

	private int colorNormal; // Normal button color.
	private int colorPressed; // Pressed color (pre-Lollipop).
	private int colorRipple; // Ripple color (Lollipop).
	private final Interpolator interpolator = new AccelerateInterpolator(1.5f);
	private boolean marginsConfigured; // shadowInset subtracted from margins (pre-Lollipop).
	private int movementY;
	private ViewTreeObserver.OnPreDrawListener preDrawListener;
	private Drawable shadowDrawable; // Shadow effect instead of elevation (pre-Lollipop).
	private int shadowInset;
	private boolean visible;

	/** Platform specific parts. Strategy pattern. */
	private static final FloatingActionButtonImpl IMPL;
	static {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			IMPL = new FloatingActionButtonLollipop();
		} else {
			IMPL = new FloatingActionButtonPreLollipop();
		}
	}

	public FloatingActionButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size = IMPL.getMeasuredDimension(this);
		setMeasuredDimension(size, size);
	}

	// ----------------------------------
	// FAB-Delegate
	// ----------------------------------

	@Override
	public int getColorNormal() {
		return colorNormal;
	}

	@Override
	public int getColorPressed() {
		return colorPressed;
	}

	@Override
	public int getColorRipple() {
		return colorRipple;
	}

	@Override
	public Drawable getShadowDrawable() {
		return shadowDrawable;
	}

	@Override
	public int getShadowInset() {
		return shadowInset;
	}

	@Override
	public int getSize() {
		return getDimensionPixelSize(R.dimen.fab_size);
	}

	@Override
	public View getView() {
		return this;
	}

	@Override
	public boolean isMarginsConfigured() {
		return marginsConfigured;
	}

	@Override
	public void setMarginsConfigured() {
		marginsConfigured = true;
	}

	// ----------------------------------
	// API
	// ----------------------------------

	/**
	 * Make this widget visible.
	 */
	public void show() {
		toggle(true);
	}

	/**
	 * Make this widget invisible.
	 */
	public void hide() {
		toggle(false);
	}

	/**
	 * Change vertical position of this widget.
	 * @param movementY vertical position offset
	 */
	public void setMovementY(int movementY) {
		this.movementY = movementY;
		reposition(true, true, false);
	}

	/**
	 * Creates an {@link AbsListView.OnScrollListener} that can be attached to an
	 * {@link AbsListView}.
	 */
	public AbsListView.OnScrollListener createScrollListener() {
		return new AbsListViewScrollDirectionDetector() {
			@Override
			public void onScrollDown() {
				show();
			}
			@Override
			public void onScrollUp() {
				hide();
			}
		};
	}

	// internal

	private int getColor(@ColorRes int id) {
		return ContextCompat.getColor(getContext(), id);
	}

	private int getDimensionPixelSize(@DimenRes int id) {
		return getResources().getDimensionPixelSize(id);
	}

	private int getMarginBottom() {
		int marginBottom = 0;
		final ViewGroup.LayoutParams layoutParams = getLayoutParams();
		if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
			marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
		}
		return marginBottom;
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr) {
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton,
				defStyleAttr, 0);

		try {
			colorNormal = a.getColor(R.styleable.FloatingActionButton_fabColorNormal,
					getColor(android.R.color.black));
			colorPressed = a.getColor(R.styleable.FloatingActionButton_fabColorPressed,
					getColor(android.R.color.darker_gray));
			colorRipple = a.getColor(R.styleable.FloatingActionButton_fabColorRipple,
					getColor(android.R.color.white));
			shadowDrawable = a.getDrawable(R.styleable.FloatingActionButton_fabShadow);
		} finally {
			a.recycle();
		}

		visible = true;
		movementY = 0;
		marginsConfigured = false;

		int width = shadowDrawable.getIntrinsicWidth();
		int height = shadowDrawable.getIntrinsicHeight();
		int sizeWithShadow = Math.max(width, height);
		int sizeWithoutShadow = getSize();
		shadowInset = (sizeWithShadow - sizeWithoutShadow) / 2;

		Drawable backgroundDrawable = IMPL.createBackground(this);
		setBackground(backgroundDrawable);
		IMPL.configureOutline(this);
	}

	private void toggle(boolean visible) {
		if (this.visible != visible) {
			this.visible = visible;
			reposition(true, true, false);
		}
	}
	private void reposition(final boolean newPosition, final boolean animate, boolean force) {
		ViewTreeObserver vto = null;

		if (newPosition && preDrawListener != null) {
			vto = getViewTreeObserver();
			vto.removeOnPreDrawListener(preDrawListener);
			preDrawListener = null;
		}
		int height = getHeight();
		if (height == 0 && !force) {
			if (vto == null) {
				vto = getViewTreeObserver();
			}
			preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					getViewTreeObserver().removeOnPreDrawListener(this);
					reposition(false, animate, true);
					return true;
				}
			};
			vto.addOnPreDrawListener(preDrawListener);
			return;
		}
		int translationY = visible ? movementY : height + getMarginBottom();
		if (animate) {
			animate().setInterpolator(interpolator).setDuration(TRANSLATE_DURATION_MILLIS).translationY(translationY);
		} else {
			setTranslationY(translationY);
		}
	}

	/**
	 * Base class for platform specific behaviour.
	 * Target platform: all.
	 */
	abstract static class FloatingActionButtonBase implements FloatingActionButtonImpl {
		@Override
		public int getMeasuredDimension(FloatingActionButtonDelegate fabDelegate) {
			return fabDelegate.getSize();
		}

		protected Drawable createOvalShapeDrawable(FloatingActionButtonDelegate fabDelegate,
				int color) {
			OvalShape ovalShape = new OvalShape();
			ShapeDrawable shapeDrawable = new ShapeDrawable(ovalShape);
			shapeDrawable.getPaint().setColor(color);
			return shapeDrawable;
		}
	}

	/**
	 * Lollipop implementation of FloatingActionButton. Set outline, use ripple drawable, elevation.
	 * Target platform: Lollipop.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	static class FloatingActionButtonLollipop extends FloatingActionButtonBase {
		@Override
		public Drawable createBackground(FloatingActionButtonDelegate fabDelegate) {
			ColorStateList color = ColorStateList.valueOf(fabDelegate.getColorRipple());
			Drawable drawable = createOvalShapeDrawable(fabDelegate, fabDelegate.getColorNormal());
			return new RippleDrawable(color, drawable, null);
		}

		@Override
		public void configureOutline(View view) {
			view.setOutlineProvider(new ViewOutlineProvider() {
				@Override
				public void getOutline(View view, Outline outline) {
					outline.setOval(0, 0, view.getWidth(), view.getHeight());
				}
			});
			view.setClipToOutline(true);
		}
	}

	/**
	 * Base class for implement FloatingActionButton on pre-Lollipop devices. No elevation -> use
	 * shadow. Shadow inset subtracted from margin size.
	 * Target platform: Gingerbread, Honeycomb, Ice Cream Sandwich, Jelly Bean, KitKat.
	 */
	static class FloatingActionButtonPreLollipop extends FloatingActionButtonBase {
		@Override
		public int getMeasuredDimension(FloatingActionButtonDelegate fabDelegate) {
			int size = super.getMeasuredDimension(fabDelegate);
			int shadowInset = fabDelegate.getShadowInset();
			if (!fabDelegate.isMarginsConfigured() && shadowInset > 0) {
				configureMargins(fabDelegate.getView(), shadowInset);
				fabDelegate.setMarginsConfigured();
			}
			size += shadowInset * 2;
			return size;
		}

		@Override
		public Drawable createBackground(FloatingActionButtonDelegate fabDelegate) {
			StateListDrawable drawable = new StateListDrawable();
			drawable.addState(new int[]{android.R.attr.state_pressed},
					createOvalShapeDrawable(fabDelegate, fabDelegate.getColorPressed()));
			drawable.addState(new int[]{},
					createOvalShapeDrawable(fabDelegate, fabDelegate.getColorNormal()));
			return drawable;
		}

		@Override
		public void configureOutline(View view) {
			// do nothing
		}

		@Override
		protected Drawable createOvalShapeDrawable(FloatingActionButtonDelegate fabDelegate,
				int color) {
			Drawable shapeDrawable = super.createOvalShapeDrawable(fabDelegate, color);
			LayerDrawable layerDrawable = new LayerDrawable(
					new Drawable[]{fabDelegate.getShadowDrawable(), shapeDrawable});
			int shadowInset = fabDelegate.getShadowInset();
			layerDrawable.setLayerInset(1, shadowInset, shadowInset, shadowInset, shadowInset);
			return layerDrawable;
		}

		private static void configureMargins(View view, int extra) {
			if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
				ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)
						view.getLayoutParams();
				int leftMargin = layoutParams.leftMargin - extra;
				int topMargin = layoutParams.topMargin - extra;
				int rightMargin = layoutParams.rightMargin - extra;
				int bottomMargin = layoutParams.bottomMargin - extra;
				layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
				view.requestLayout();
			}
		}
	}
}
