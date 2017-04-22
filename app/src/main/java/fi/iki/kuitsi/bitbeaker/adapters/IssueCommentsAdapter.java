package fi.iki.kuitsi.bitbeaker.adapters;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.MarkupHelper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.IssueComment;

/**
 * List adapter for {@linkplain IssueComment}s.
 */
public class IssueCommentsAdapter extends ParameterizedAdapter<IssueComment> {

	private final String owner;
	private final String slug;

	public IssueCommentsAdapter(Context context, String owner, String slug) {
		super(context);
		this.owner = owner;
		this.slug = slug;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_issue_comment, parent, false);
		}
		TextView header = (TextView) convertView.findViewById(R.id.issue_comment_header);
		TextView content = (TextView) convertView.findViewById(R.id.issue_comment_content);

		final IssueComment issueComment = getItem(position);
		final String contents = issueComment.getContent();

		header.setText((issueComment.getAuthor() == null ? "" : issueComment.getAuthor().getUsername() + " - ")
				+ Helper.formatDate(issueComment.getUtcCreatedOn()));
		if (contents != null) {
			content.setText(MarkupHelper.handleMarkup(contents, owner, slug, MarkupHelper.isCreole(
					issueComment.getUtcUpdatedOn())));
			content.setMovementMethod(LinkMovementMethod.getInstance());
		} else {
			content.setText(R.string.issue_details_unavailable);
		}

		return convertView;
	}
}
