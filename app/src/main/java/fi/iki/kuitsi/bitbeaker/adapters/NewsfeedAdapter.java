package fi.iki.kuitsi.bitbeaker.adapters;

import zeroone.rss.Channel;
import zeroone.rss.Item;

import android.content.Context;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.MarkupHelper;
import fi.iki.kuitsi.bitbeaker.R;

public class NewsfeedAdapter extends BaseAdapter {
	private final Context context;
	private final Channel news;

	public NewsfeedAdapter(Context context, Channel news) {
		this.context = context;
		this.news = news;
	}

	@Override
	public int getCount() {
		return news.getItems().size();
	}

	@Override
	public Item getItem(int position) {
		return news.getItems().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView == null) {
			view = LayoutInflater.from(context).inflate(R.layout.listitem_two_rows, parent, false);
		} else {
			view = convertView;
		}
		view.setId(position);
		TextView title = (TextView) view.findViewById(R.id.title);
		TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
		final Item item = getItem(position);
		try {
			title.setText(getTitle(item));
			title.setMovementMethod(LinkMovementMethod.getInstance());

			final Spanned subtitleText = getSubtitle(item);
			if (subtitleText != null) {
				subtitle.setText(subtitleText);
				subtitle.setVisibility(View.VISIBLE);
			} else {
				subtitle.setVisibility(View.GONE);
			}
			subtitle.setMovementMethod(LinkMovementMethod.getInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return view;
	}

	private Spanned getTitle(Item item) {
		String[] itemDescription = splitItemDescription(item.getDescription());
		final String date = Helper.formatRelativeDate(context, item.getPubDate());
		String title = MarkupHelper.getBareUrlsLinkified(itemDescription[0]);
		return MarkupHelper.handleHTML(combineTitleAndDate(title, date));
	}

	private String combineTitleAndDate(final String title, final String date) {
		return "<small>" + date + "</small>: " + trimChangesetIds(title);
	}

	private Spanned getSubtitle(Item item) {
		String[] itemDescription = splitItemDescription(item.getDescription());
		if (itemDescription.length == 1) {
			return null;
		}
		return MarkupHelper.handleHTML(trimChangesetIds(itemDescription[1]));
	}

	/**
	 * The Bitbucket RSS feed duplicates the contents of the title field in the
	 * description field, but in such a fashion that the description field also
	 * contains links and is thus more useful. In the description field the
	 * title is separated from other content (such as user's commit message) so
	 * that the other content is wrapped with paragraph tags.
	 *
	 * @param description The description field of an item element
	 * @return The description split to a title and the other part,
	 * or just the title if there wasn't more.
	 */
	private String[] splitItemDescription(final String description) {
		String[] result;
		if (description.contains("<p>")) {
			result = description.split("<p>", 2);
			result[0] = result[0].trim();
			result[1] = result[1].replace("</p>", "").trim();
		} else {
			result = new String[1];
			result[0] = description;
		}
		return result;
	}

	/**
	 * Cuts the long changeset ids.
	 *
	 * @param text A string with some changeset ids
	 * @return The input string but with shorter changeset ids.
	 */
	private String trimChangesetIds(String text) {
		return text.replaceAll("([a-fA-F0-9]{7})([a-fA-F0-9]{25,33})", "$1");
	}
}
