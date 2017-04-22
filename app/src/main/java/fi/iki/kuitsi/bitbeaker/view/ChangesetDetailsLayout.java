package fi.iki.kuitsi.bitbeaker.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.clicklisteners.UserProfileActivityStartingClickListener;
import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;

import java.util.List;

/**
 * Layout for ChangesetDetailsFragment.
 */
public class ChangesetDetailsLayout extends TableLayout {

	private TextView message;
	private TextView author;
	private TextView branch;
	private TextView tagsView;
	private TableRow branchRow;
	private TableRow tagsRow;

	private Changeset changeset;
	private List<String> tags;

	public ChangesetDetailsLayout(Context context) {
		super(context);
		initView();
	}

	public ChangesetDetailsLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		setOrientation(LinearLayout.VERTICAL);
		setGravity(Gravity.TOP);
		setStretchAllColumns(true);

		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.changeset_details, this, true);

		message = (TextView) findViewById(R.id.changeset_message);
		author = Helper.renderAsLink((TextView) findViewById(R.id.changeset_author));
		branchRow = (TableRow) findViewById(R.id.branch_row);
		branch = (TextView) findViewById(R.id.changeset_branch);
		tagsRow = (TableRow) findViewById(R.id.tags_row);
		tagsView = (TextView) findViewById(R.id.tags);
	}

	/**
	 * Sets the changeset displayed by ChangesetDetailsLayout.
	 */
	public void setChangeset(Changeset changeset) {
		this.changeset = changeset;
		updateLayout();
	}

	/**
	 * Sets the tags associated to changeset.
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
		updateLayout();
	}

	private void updateLayout() {
		this.removeAllViews();
		initView();

		if (changeset != null) {
			message.setText(changeset.getMessage());
			final String authorName = changeset.getAuthor();
			author.setText(authorName);
			author.setOnClickListener(new UserProfileActivityStartingClickListener(authorName));
			final String branch_tmp = changeset.getBranch();
			if (!Helper.isJsonEmpty(branch_tmp)) {
				branch.setText(branch_tmp);
			} else {
				branchRow.setVisibility(View.GONE);
			}
		}

		if (tags != null && tags.size() > 0) {
			String listString = TextUtils.join(" ", tags);
			tagsView.setText(listString);
		} else {
			tagsRow.setVisibility(View.GONE);
		}
	}
}
