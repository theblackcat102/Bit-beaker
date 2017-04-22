package fi.iki.kuitsi.bitbeaker.view;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Widget that looks like a {@linkplain android.widget.Spinner} when it is collapsed, and display
 * a dialog with multiple choices instead of a dropdown view.
 */
public class MultiSelectionSpinner extends AppCompatSpinner implements
		DialogInterface.OnMultiChoiceClickListener, DialogInterface.OnCancelListener {

	private List<String> items;
	private boolean[] selected;
	private String allText;
	private String text;
	ArrayAdapter<String> adapter;
	private OnItemSelectedListener listener;

	/**
	 * Construct a new MultiSelectionSpinner with the given context's theme.
	 *
	 * @param context The Context the view is running in, through which it can
	 *        access the current theme, resources, etc.
	 */
	public MultiSelectionSpinner(Context context) {
		super(context);
	}

	/**
	 * Construct a new MultiSelectionSpinner with the given context's theme and the supplied
	 * attribute set.
	 *
	 * @param context The Context the view is running in, through which it can
	 *        access the current theme, resources, etc.
	 * @param attrs The attributes of the XML tag that is inflating the view.
	 */
	public MultiSelectionSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Construct a new MultiSelectionSpinner with the given context's theme, the supplied
	 * attribute set, and default style.
	 *
	 * @param context The Context the view is running in, through which it can
	 *        access the current theme, resources, etc.
	 * @param attrs The attributes of the XML tag that is inflating the view.
	 * @param defStyle The default style to apply to this view. If 0, no style
	 *        will be applied (beyond what is included in the theme). This may
	 *        either be an attribute resource, whose value will be retrieved
	 *        from the current theme, or an explicit style resource.
	 */
	public MultiSelectionSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		selected[which] = isChecked;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		onDialogClosed();
	}

	@Override
	public boolean performClick() {
		new AlertDialog.Builder(getContext())
				.setMultiChoiceItems(items.toArray(new CharSequence[items.size()]), selected, this)
				.setOnCancelListener(this)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
				.show();
		return true;
	}

	/**
	 * Populate items.
	 * @param items Available items
	 * @param allText Text displayed when all items are selected.
	 * @param listener The callback that will run
	 */
	public void setItems(List<String> items, String allText, OnItemSelectedListener listener) {
		// all selected by default
		boolean[] selectedItems = new boolean[items.size()];
		for (int i = 0; i < selectedItems.length; ++i) {
			selectedItems[i] = true;
		}
		init(items, selectedItems, allText, listener);
	}

	/**
	 * Populate items.
	 * @param items Available items
	 * @param selectedItems Boolean array of selected items
	 * @param allText Text displayed when all items are selected.
	 * @param listener The callback that will run
	 */
	public void setItems(List<String> items, boolean[] selectedItems, String allText,
			OnItemSelectedListener listener) {
		if (items.size() != selectedItems.length) {
			throw new IllegalArgumentException("Size of items and selectedItems should be equal");
		}
		init(items, selectedItems, allText, listener);
	}

	public boolean[] getSelected() {
		return selected;
	}

	private void init(List<String> items, boolean[] selectedItems, String allText,
			OnItemSelectedListener listener) {
		this.items = items;
		this.allText = allText;
		this.listener = listener;
		this.selected = selectedItems;

		buildText();
		adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
		adapter.add(text);
		adapter.setNotifyOnChange(false);
		setAdapter(adapter);
	}

	private boolean buildText() {
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		boolean allSelected = true;
		for (int i = 0; i < items.size(); ++i) {
			if (selected[i]) {
				if (firstTime) {
					firstTime = false;
				} else {
					sb.append(", ");
				}
				sb.append(items.get(i));
			} else {
				allSelected = false;
			}
		}
		if (allSelected) {
			text = allText;
		} else {
			text = sb.toString();
		}
		return allSelected;
	}

	private void onDialogClosed() {
		boolean allSelected = buildText();
		adapter.clear();
		adapter.add(text);
		adapter.notifyDataSetChanged();
		listener.onItemsSelected(allSelected, selected);
	}

	/**
	 * Interface definition for a callback to be invoked when the spinner's dialog with
	 * multiple choices closed.
	 */
	public interface OnItemSelectedListener {

		/**
		 * Callback method to be invoked when when the spinner's dialog with
		 * multiple choices closed.
		 * @param allSelected True if all available item selected
		 * @param selected boolean array of selection
		 */
		void onItemsSelected(boolean allSelected, boolean[] selected);
	}
}
