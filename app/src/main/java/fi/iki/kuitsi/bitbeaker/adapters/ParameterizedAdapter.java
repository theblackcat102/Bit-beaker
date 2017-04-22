package fi.iki.kuitsi.bitbeaker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fi.iki.kuitsi.bitbeaker.util.StringUtils;

/**
 * A concrete BaseAdapter that is backed by an array of arbitrary objects.
 * It's similar to the {@link android.widget.ArrayAdapter}.
 *
 * @param <T> The type of array items.
 */
public abstract class ParameterizedAdapter<T> extends BaseAdapter implements Filterable {

	protected Context context;
	protected LayoutInflater inflater;

	/**
	 * Contains the list of objects that represent the data of this {@link ParameterizedAdapter}.
	 */
	private List<T> items;

	/**
	 * A copy of the original {@link #items} array, initialized from and then used instead as soon
	 * as the {#link filter} is used. {@link #items} will then only contain the filtered values.
	 */
	private List<T> originalItems;

	/**
	 * Lock used to modify the content of {@link #items}. Any write operation
	 * performed on the array should be synchronized on this lock.
	 */
	private final Object lock = new Object();

	private Filter filter;

	/**
	 * Constructor.
	 *
	 * @param context The current context.
	 */
	public ParameterizedAdapter(Context context) {
		init(context, new ArrayList<T>());
	}

	/**
	 * Constructor.
	 *
	 * @param context The current context.
	 * @param itemList The objects to represent in the ListView.
	 */
	public ParameterizedAdapter(Context context, List<T> itemList) {
		init(context, itemList);
	}

	private void init(Context contextParam, List<T> itemList) {
		items = itemList;
		context = contextParam;
		inflater = LayoutInflater.from(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getCount() {
		if (items != null) return items.size();
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final T getItem(int position) {
		return items.get(position);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	protected final View inflateViewIfRequired(final View convertView, final int resource) {
		if (convertView == null) {
			return inflater.inflate(resource, null);
		}
		return convertView;
	}

	/**
	 * Returns the context associated with this array adapter. The context is used
	 * to create views from the resource passed to the constructor.
	 *
	 * @return The Context associated with this adapter.
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * Remove all elements from the list.
	 */
	public void clear() {
		synchronized (lock) {
			if (originalItems != null) {
				originalItems.clear();
			} else {
				items.clear();
			}
		}
		notifyDataSetChanged();
	}

	/**
	 * Adds the specified object at the end of the array.
	 *
	 * @param object The object to add at the end of the array.
	 */
	public void add(T object) {
		synchronized (lock) {
			if (originalItems != null) {
				originalItems.add(object);
			} else {
				items.add(object);
			}
		}
		notifyDataSetChanged();
	}

	/**
	 * Adds the specified Collection at the end of the array.
	 *
	 * @param collection The Collection to add at the end of the array.
	 */
	public void addAll(Collection<? extends T> collection) {
		synchronized (lock) {
			if (originalItems != null) {
				originalItems.addAll(collection);
			} else {
				items.addAll(collection);
			}
		}
		notifyDataSetChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new ArrayFilter();
		}
		return filter;
	}

	/**
	 * Sort items.
	 *
	 * @param comparator Sort using this Comparator
	 */
	public void sort(Comparator<? super T> comparator) {
		synchronized (lock) {
			Collections.sort(items, comparator);
		}
		notifyDataSetChanged();
	}

	final class ArrayFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			if (originalItems == null) {
				synchronized (lock) {
					originalItems = new ArrayList<>(items);
				}
			}

			ArrayList<T> values;
			synchronized (lock) {
				values = new ArrayList<>(originalItems);
			}

			FilterResults results = new FilterResults();

			if (StringUtils.isNotBlank(constraint)) {
				final ArrayList<T> newValues = new ArrayList<>();

				for (final T value : values) {
					if (filterItem(value, constraint)) {
						newValues.add(value);
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			} else {
				results.values = values;
				results.count = values.size();
			}

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			//noinspection unchecked
			items = (List<T>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}

	@SuppressWarnings("UnusedParameters")
	protected boolean filterItem(T value, CharSequence constraint) {
		return true;
	}
}
