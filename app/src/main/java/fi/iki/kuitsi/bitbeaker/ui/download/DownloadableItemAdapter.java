package fi.iki.kuitsi.bitbeaker.ui.download;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.data.api.model.repositories.DownloadableItem;

public final class DownloadableItemAdapter extends RecyclerView.Adapter<DownloadableItemAdapter.ViewHolder> {

	private final List<DownloadableItem> items;
	private final OnItemClickListener listener;

	public DownloadableItemAdapter(OnItemClickListener listener) {
		this.items = new ArrayList<>();
		this.listener = listener;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View itemView = inflater.inflate(R.layout.listitem_two_rows, parent, false);
		return new ViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.bind(items.get(position), listener);
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public void setItems(Collection<DownloadableItem> newItems) {
		items.clear();
		items.addAll(newItems);
		notifyDataSetChanged();
	}

	public interface OnItemClickListener {
		void onItemClick(DownloadableItem item);
	}

	static final class ViewHolder extends RecyclerView.ViewHolder {

		@BindView(R.id.title) TextView title;
		@BindView(R.id.subtitle) TextView subtitle;

		public ViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}

		public void bind(final DownloadableItem item, final OnItemClickListener listener) {
			title.setText(item.name);
			subtitle.setText(String.valueOf(item.size));
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					listener.onItemClick(item);
				}
			});
		}
	}
}
