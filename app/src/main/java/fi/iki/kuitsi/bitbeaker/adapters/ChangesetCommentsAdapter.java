package fi.iki.kuitsi.bitbeaker.adapters;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.MarkupHelper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.ChangesetComment;

/**
 * This is an adapter class for ListView elements displaying the
 * comments of a changeset.
 */
public class ChangesetCommentsAdapter extends ParameterizedAdapter<ChangesetComment> {

	public ChangesetCommentsAdapter(Context context) {
		super(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = inflateViewIfRequired(convertView, R.layout.listitem_issue_comment);
		view.setId(position);
		TextView header = (TextView) view.findViewById(R.id.issue_comment_header);
		TextView content = (TextView) view.findViewById(R.id.issue_comment_content);

		try {
			header.setText(getItem(position).getUsername() + " - "
					+ Helper.formatDate(getItem(position).getCreationDate()));
			String contents = getItem(position).getContent();
			contents = MarkupHelper.fixRelativeLinks(contents);
			content.setText(MarkupHelper.handleHTML(contents, content, getContext()));
			content.setMovementMethod(LinkMovementMethod.getInstance());
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return view;
	}
}
