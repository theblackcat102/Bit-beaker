package fi.iki.kuitsi.bitbeaker.navigationdrawer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.R;

public final class ListItem implements Item {
	@DrawableRes private final int iconResId;
	@StringRes private final int strId;
	@ClickAction private final int action;
	@State private final int initialState;

	ListItem(Builder builder) {
		this.iconResId = builder.iconResId;
		this.strId = builder.strId;
		this.action = builder.action;
		this.initialState = builder.initialState;
	}

	@NonNull
	@Override
	public View getView(LayoutInflater inflater, ViewGroup parent) {
		Context context = inflater.getContext();
		View view = inflater.inflate(R.layout.listitem_navigationdrawer,
				parent, false);

		TextView txtTitle = (TextView) view.findViewById(R.id.title);
		txtTitle.setText(strId);

		ImageView icon = (ImageView) view.findViewById(R.id.icon);
		Drawable iconDrawable = ContextCompat.getDrawable(context, iconResId);
		iconDrawable = iconDrawable.mutate();
		iconDrawable = DrawableCompat.wrap(iconDrawable);
		DrawableCompat.setTint(iconDrawable,
				ContextCompat.getColor(context, R.color.bitbeaker_control));
		icon.setImageDrawable(iconDrawable);

		View progress = view.findViewById(R.id.progressBar);
		if (initialState == STATE_LOADING) {
			progress.setVisibility(View.VISIBLE);
		} else {
			progress.setVisibility(View.GONE);
		}

		return view;
	}

	@Override
	@ClickAction
	public int getAction() {
		return action;
	}

	public static class Builder {

		@StringRes int strId;
		@DrawableRes int iconResId;
		@ClickAction int action;
		@State int initialState;

		public Builder() {
			this.action = CLICK_ACTION_NOTHING;
			this.initialState = STATE_VISIBLE;
		}

		public Builder setStrId(@StringRes int strId) {
			this.strId = strId;
			return this;
		}

		public Builder setIconResId(@DrawableRes int iconResId) {
			this.iconResId = iconResId;
			return this;
		}

		public Builder setAction(@ClickAction int action) {
			this.action = action;
			return this;
		}

		public Builder setInitialState(@State int initialState) {
			this.initialState = initialState;
			return this;
		}

		public Item build() {
			return new ListItem(this);
		}
	}

}
