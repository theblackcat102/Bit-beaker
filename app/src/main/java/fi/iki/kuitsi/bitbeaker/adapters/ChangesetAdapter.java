package fi.iki.kuitsi.bitbeaker.adapters;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.activities.DiffActivity;
import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;
import fi.iki.kuitsi.bitbeaker.domainobjects.ChangesetFile;
import fi.iki.kuitsi.bitbeaker.domainobjects.ChangesetFile.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is an adapter that can be used with ListView elements in order to
 * render individual changesets in them, i.e. to list the files that were added,
 * modified or deleted.
 */
public class ChangesetAdapter extends ParameterizedAdapter<ChangesetFile> {
	protected final String owner;
	protected final String slug;
	protected final String changesetId;
	protected final String parentChangesetId;

	private static final Map<Type, Integer> TYPE_TO_IMAGE_RESOURCE;

	static {
		TYPE_TO_IMAGE_RESOURCE = new HashMap<Type, Integer>();
		TYPE_TO_IMAGE_RESOURCE.put(Type.ADDED, R.drawable.icon_added);
		TYPE_TO_IMAGE_RESOURCE.put(Type.MODIFIED, R.drawable.icon_modified);
		TYPE_TO_IMAGE_RESOURCE.put(Type.REMOVED, R.drawable.icon_removed);
	}

	public ChangesetAdapter(Context context, Changeset changeset, String owner, String slug) {
		super(context, changeset.getFiles());
		this.owner =  owner;
		this.slug = slug;
		this.changesetId = changeset.getNode();
		String[] parents = changeset.getParents();
		this.parentChangesetId = (parents.length > 0) ? parents[0] : "";
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = inflateViewIfRequired(convertView, R.layout.listitem_two_rows_icon);
		view.setId(position);
		TextView title = (TextView) view.findViewById(R.id.title);
		TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
		ImageView icon = (ImageView) view.findViewById(R.id.icon);
		final String file = this.getItem(position).getFile();
		Type type = this.getItem(position).getType();
		title.setText(Helper.translateApiString(type.toString()));
		icon.setImageResource(TYPE_TO_IMAGE_RESOURCE.get(type));
		subtitle.setText(file);
		view.setOnClickListener(createShowDiffClickListener(file, type));
		return view;
	}

	private OnClickListener createShowDiffClickListener(final String file, final Type type) {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.getContext().startActivity(DiffActivity.createIntent(v.getContext(), owner, slug,
						changesetId, file, type, parentChangesetId));
			}
		};
	}

}
