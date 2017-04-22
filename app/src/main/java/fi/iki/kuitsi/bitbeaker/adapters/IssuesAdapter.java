package fi.iki.kuitsi.bitbeaker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.data.api.resource.DrawableProvider;
import fi.iki.kuitsi.bitbeaker.data.api.resource.StringProvider;
import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;
import fi.iki.kuitsi.bitbeaker.ui.common.BindableAdapter;

/**
 * List adapter for {@linkplain Issue}s.
 */
public final class IssuesAdapter extends BindableAdapter<Issue> {

	private final List<Issue> issues;
	private final StringProvider<Issue.Status> issueStatusStringProvider;
	private final DrawableProvider<Issue.Kind> issueKindDrawableProvider;

	public IssuesAdapter(Context context, StringProvider<Issue.Status> issueStatusStringProvider,
			DrawableProvider<Issue.Kind> issueKindDrawableProvider) {
		super(context);
		this.issueStatusStringProvider = issueStatusStringProvider;
		this.issueKindDrawableProvider = issueKindDrawableProvider;
		issues = new ArrayList<>();
	}

	@Override
	public int getCount() {
		return issues.size();
	}

	@Override
	public Issue getItem(int position) {
		return issues.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	protected View newView(LayoutInflater inflater, int position, ViewGroup container) {
		View view = inflater.inflate(R.layout.listitem_two_rows_icon, container, false);
		ViewHolder viewHolder = new ViewHolder(view);
		view.setTag(viewHolder);
		return view;
	}

	@Override
	protected void bindView(Issue item, int position, View view) {
		final ViewHolder viewHolder = (ViewHolder) view.getTag();
		final Issue issue = getItem(position);

		viewHolder.title.setText("#" + issue.getLocalId() + ": " + issue.getTitle());
		viewHolder.subtitle.setText(getString(issueStatusStringProvider.getStringRes(issue.getStatus()))
				+ " - " + Helper.formatDate(issue.getCreatedOn())
				+ " - "
				+ "\n" + getString(R.string.last_updated) + ": "
				+ Helper.formatDate(issue.getLastUpdated()));
		viewHolder.icon.setImageResource(issueKindDrawableProvider.getDrawableRes(issue.getKind()));
	}

	public void set(List<Issue> items) {
		issues.clear();
		add(items);
	}

	public void add(List<Issue> items) {
		issues.addAll(items);
		notifyDataSetChanged();
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
