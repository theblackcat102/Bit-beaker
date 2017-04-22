package fi.iki.kuitsi.bitbeaker.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.activities.UserProfileActivity;
import fi.iki.kuitsi.bitbeaker.data.ImageLoader;
import fi.iki.kuitsi.bitbeaker.domainobjects.User;
import fi.iki.kuitsi.bitbeaker.util.StringUtils;

import java.util.Locale;

public class RepositoryFollowersAdapter extends ParameterizedAdapter<User> {

	private final ImageLoader imageLoader;

	public RepositoryFollowersAdapter(Context context, ImageLoader imageLoader) {
		super(context);
		this.imageLoader = imageLoader;
	}

	private final OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String username = getItem(v.getId()).getUsername();
			Bundle b = new Bundle();
			b.putString("user", username);
			Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
			intent.putExtras(b);
			getContext().startActivity(intent);
		}
	};

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_user, parent, false);
			holder = new ViewHolder();
			holder.accountName = (TextView) convertView.findViewById(R.id.accountname);
			holder.displayName = (TextView) convertView.findViewById(R.id.displayname);
			holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		convertView.setId(position);
		convertView.setOnClickListener(clickListener);

		final User user = getItem(position);

		holder.accountName.setText(user.getUsername());

		final String displayName = getDisplayName(user);
		if (!TextUtils.isEmpty(displayName)) {
			holder.displayName.setVisibility(View.VISIBLE);
			holder.displayName.setText(displayName);
		} else {
			holder.displayName.setVisibility(View.GONE);
		}

		imageLoader.loadAvatar(getContext(), user.getAvatarUrl(), holder.avatar);

		return convertView;
	}

	@Override
	protected boolean filterItem(User user, CharSequence constraint) {
		Locale locale = Helper.getCurrentLocale();
		String constraintLowercase = constraint.toString().toLowerCase(locale);
		return user.getUsername().toLowerCase(locale).contains(constraintLowercase)
				|| user.getFirstName().toLowerCase(locale).contains(constraintLowercase)
				|| user.getLastName().toLowerCase(locale).contains(constraintLowercase)
				|| user.getDisplayName().toLowerCase().contains(constraintLowercase);
	}

	static String getDisplayName(User user) {
		if (StringUtils.isNotEmpty(user.getDisplayName())) {
			return user.getDisplayName();
		} else {
			String displayName = "";
			if (StringUtils.isNotEmpty(user.getFirstName())) {
				displayName = user.getFirstName();
				if (StringUtils.isNotEmpty(user.getLastName())) {
					displayName += " ";
					displayName += user.getLastName();
				}
			}
			return displayName;
		}
	}

	static class ViewHolder {
		TextView accountName;
		TextView displayName;
		ImageView avatar;
	}
}
