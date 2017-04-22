package fi.iki.kuitsi.bitbeaker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchableRepositoriesAdapter extends RepositoriesAdapter {

	private String query;

	public SearchableRepositoriesAdapter(Context context, String query) {
		super(context);
		this.query = query;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = inflateViewIfRequired(convertView, R.layout.listitem_two_rows_icon_checkbox);
		view.setId(position);
		TextView title = (TextView) view.findViewById(R.id.title);
		TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
		ImageView icon = (ImageView) view.findViewById(R.id.icon);
		final Repository item = getItem(position);
		try {
			final String lastUpdated = getContext().getString(R.string.last_updated) + ": "
					+ Helper.formatDate(item.getLastUpdated());
			final String description = item.getDescription();
			title.setText(getHighlightedName(item, query, Color.YELLOW));
			subtitle.setText(lastUpdated + (StringUtils.isBlank(description) ? "" : "\n" + description));
			if (item.isFork()) {
				icon.setImageResource(R.drawable.icon_fork);
				icon.setVisibility(View.VISIBLE);
			} else {
				icon.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		view.setOnClickListener(clickListener);
		initFavoriteCheckbox(view, item);

		return view;
	}

	private static CharSequence getHighlightedName(Repository repository, String query,
			int highlightedColor) {
		SpannableStringBuilder sb = new SpannableStringBuilder();
		sb.append(repository.getOwner());
		sb.append(" / ");
		sb.append(repository.getName());
		Pattern pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sb.toString());
		if (matcher.find()) {
			sb.setSpan(
					new BackgroundColorSpan(highlightedColor),
					matcher.start(), matcher.end(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			sb.setSpan(
					new StyleSpan(Typeface.BOLD), matcher.start(), matcher.end(),
					Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			return sb;
		}
		return sb;
	}
}
