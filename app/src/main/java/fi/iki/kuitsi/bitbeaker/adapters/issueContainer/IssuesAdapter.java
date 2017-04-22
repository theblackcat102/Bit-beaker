package fi.iki.kuitsi.bitbeaker.adapters.issueContainer;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.adapters.ParameterizedAdapter;
import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;

import java.util.List;

public class IssuesAdapter extends ParameterizedAdapter<Issue> {

	static class ViewHolder {
		TextView title;
		TextView subtitle;
		ImageView icon;
	}

	public IssuesAdapter(Context context, List<Issue> issues) {
		super(context, issues);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			rowView = inflater.inflate(R.layout.listitem_two_rows_icon, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.title = (TextView) rowView.findViewById(R.id.title);
			viewHolder.subtitle = (TextView) rowView.findViewById(R.id.subtitle);
			viewHolder.icon = (ImageView) rowView.findViewById(R.id.icon);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();

		Resources resources = context.getResources();

		Issue issue = getItem(position);
		String issueId = issue.getLocalId() + "";
		Issue.Kind kind = issue.getMetadata().getKind();
		String status = issue.getStatus().toString();
		status = Helper.translateApiString(status);

		holder.title.setText("#" + issueId + ": " + issue.getTitle());

		int commentCount = issue.getCommentCount();
		String comments = resources.getQuantityString(R.plurals.comments, commentCount, commentCount);

		String formattedCreationDate = DateUtils.getRelativeDateTimeString(
				getContext(), issue.getCreatedOn().getTime(), DateUtils.MINUTE_IN_MILLIS,
				DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
		String formattedLastUpdateDate = DateUtils.getRelativeDateTimeString(
				getContext(), issue.getLastUpdated().getTime(), DateUtils.MINUTE_IN_MILLIS,
				DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();

		holder.subtitle.setText(status + " | " + formattedCreationDate + "\n" + comments + "\n"
				+ resources.getString(R.string.last_updated) + ": " + formattedLastUpdateDate);
		switch (kind) {
			case BUG:
				holder.icon.setImageResource(R.drawable.icon_bug);
				break;
			case ENHANCEMENT:
				holder.icon.setImageResource(R.drawable.icon_enhancement);
				break;
			case PROPOSAL:
				holder.icon.setImageResource(R.drawable.icon_proposal);
				break;
			case TASK:
				holder.icon.setImageResource(R.drawable.icon_task);
				break;
		}
		return rowView;
	}
}
