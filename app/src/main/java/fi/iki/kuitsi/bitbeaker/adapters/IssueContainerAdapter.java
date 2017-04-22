package fi.iki.kuitsi.bitbeaker.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.viewmodel.IssueContainerStat;

import java.text.NumberFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IssueContainerAdapter extends ParameterizedAdapter<IssueContainerStat> {

	static class ViewHolder {
		@BindView(R.id.title) TextView title;
		@BindView(R.id.meta) TextView meta;
		@BindView(R.id.progress) TextView progress;
		@BindView(R.id.progressBar) ProgressBar progressBar;

		ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}

	public IssueContainerAdapter(Context context) {
		super(context);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			rowView = inflater.inflate(R.layout.listitem_milestones, parent, false);
			ViewHolder viewHolder = new ViewHolder(rowView);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		IssueContainerStat item = getItem(position);
		holder.title.setText(item.getName());

		final int issueCount = item.getIssueCount();
		final int closedIssueCount = item.getClosedIssueCount();

		if (issueCount != -1 && closedIssueCount != -1) {
			holder.meta.setText(context.getResources().getQuantityString(
					R.plurals.milestones_meta_issues, issueCount, issueCount, closedIssueCount));

			holder.progressBar.setIndeterminate(false);

			if (issueCount > 0) {
				final float percent = closedIssueCount * 1.0f / issueCount * 1.0f;
				final NumberFormat nf = NumberFormat.getPercentInstance();
				nf.setMaximumFractionDigits(1);
				nf.setMinimumFractionDigits(1);
				holder.progress.setText(context.getString(R.string.milestones_meta_progress,
						nf.format(percent)));
				holder.progress.setVisibility(View.VISIBLE);
				holder.progressBar.setProgress(Math.round(percent * 100.0f));
			} else {
				holder.progress.setVisibility(View.INVISIBLE);
				holder.progressBar.setProgress(0);
			}
		} else {
			holder.progress.setVisibility(View.INVISIBLE);
			holder.progressBar.setIndeterminate(true);
		}

		return rowView;
	}
}
