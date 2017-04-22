package fi.iki.kuitsi.bitbeaker.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.data.ImageLoader;
import fi.iki.kuitsi.bitbeaker.domainobjects.PullRequestComment;

public class PullRequestCommentAdapter extends ParameterizedAdapter<PullRequestComment> {

	private final ImageLoader imageLoader;

	public PullRequestCommentAdapter(Context context, ImageLoader imageLoader) {
		super(context);
		this.imageLoader = imageLoader;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_user_comment, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final PullRequestComment comment = getItem(position);
		holder.title.setText(Helper.formatRelativeDate(context, comment.getCreated()) + ": "
				+ comment.getAuthor().getUsername());
		holder.subtitle.setText(comment.getContent());
		String avatarUrl = comment.getAuthor().getAvatarUrl();
		imageLoader.loadAvatar(getContext(), avatarUrl, holder.icon);
		return convertView;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	static class ViewHolder {

		@BindView(R.id.title) TextView title;
		@BindView(R.id.subtitle) TextView subtitle;
		@BindView(R.id.icon) ImageView icon;

		ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}
