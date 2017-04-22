package fi.iki.kuitsi.bitbeaker.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.Tags;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an adapter class for ListView elements displaying the changesets.
 */
public class ChangesetsAdapter extends ParameterizedAdapter<Changeset> {
	protected final String owner;
	protected final String slug;
	private Tags tags;

	public ChangesetsAdapter(Context context, String user, String repository) {
		super(context);
		owner = user;
		slug = repository;
		this.tags = null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_changeset, parent, false);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.subtitle = (TextView) convertView.findViewById(R.id.subtitle);
			holder.tag = (TextView) convertView.findViewById(R.id.tag);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		try {
			Changeset item = getItem(position);
			String title = Helper.formatDate(item.getTimestamp()) + " - " + item.getAuthor();
			if (tags != null) {
				List<String> foundTags = tags.getTagsForChangeset(item.getRawNode());
				foundTags.remove("tip");
				if (foundTags.isEmpty()) {
					holder.tag.setVisibility(View.GONE);
				} else {
					if (foundTags.size() == 1) {
						holder.tag.setText(foundTags.get(0));
					} else {
						Resources res = getContext().getResources();
						String numberOfTags = res.getQuantityString(R.plurals.tag_number,
								foundTags.size(), foundTags.size());
						holder.tag.setText(numberOfTags);
					}
					holder.tag.setVisibility(View.VISIBLE);
				}
			} else {
				holder.tag.setVisibility(View.GONE);
			}
			holder.title.setText(title);
			holder.subtitle.setText(item.getNode() + " | " + item.getMessage().trim());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return convertView;
	}

	private static class ViewHolder {
		TextView title;
		TextView subtitle;
		TextView tag;
	}

	/**
	 * Get hash value of the parent of the last item.
	 *
	 * @return Hash value
	 */
	public String getNextChangeset() {
		if (getCount() > 0) {
			Changeset item = getItem(getCount() - 1);
			String[] parents = item.getParents();
			if (parents.length > 0) {
				return parents[0];
			}
		}
		return "";
	}

	public void setTags(Tags tags) {
		this.tags = tags;
		notifyDataSetChanged();
	}

	public ArrayList<String> getTags(int position) {
		if (tags == null) {
			return null;
		}
		final Changeset changeset = getItem(position);
		return tags.getTagsForChangeset(changeset.getRawNode());
	}
}
