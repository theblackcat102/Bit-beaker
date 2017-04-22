package fi.iki.kuitsi.bitbeaker.view;

import android.widget.SpinnerAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import fi.iki.kuitsi.bitbeaker.BuildConfig;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = BuildConfig.ROBOLECTRIC_SDK)
public class MultiSelectionSpinnerTest {

	@Mock MultiSelectionSpinner.OnItemSelectedListener listener;
	private MultiSelectionSpinner spinner;
	private List<String> items;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		spinner = new MultiSelectionSpinner(RuntimeEnvironment.application);
		items = Arrays.asList("one", "two", "three");
		spinner.setItems(items, "All item selected", listener);
	}

	@Test
	public void testAllItemSelectedByDefault() {
		boolean[] selected = spinner.getSelected();
		assertThat(selected).isNotNull();
		assertThat(selected).hasLength(3);
		assertThat(selected).asList().containsExactly(true, true, true);
	}

	@Test
	public void testAllSelectedText() throws Exception {
		final SpinnerAdapter adapter = spinner.getAdapter();
		assertThat(adapter.isEmpty()).isFalse();
		assertThat(adapter.getItem(0).toString()).isEqualTo("All item selected");
	}

	@Test
	public void testChangeSelection() throws Exception {
		selectItem(0, false);
		boolean[] selected = spinner.getSelected();
		assertThat(selected).isNotNull();
		assertThat(selected).hasLength(3);
		assertThat(selected).asList().containsExactly(false, true, true).inOrder();
	}

	@Test
	public void testEnumerateSelectedItems() throws Exception {
		selectItem(1, false);
		boolean[] selected = spinner.getSelected();
		assertThat(selected).isNotNull();
		assertThat(selected).hasLength(3);
		assertThat(selected).asList().containsExactly(true, false, true).inOrder();

		final SpinnerAdapter adapter = spinner.getAdapter();
		assertThat(adapter.isEmpty()).isFalse();
		assertThat(adapter.getItem(0).toString()).isEqualTo("one, three");
	}

	@Test
	public void testOnClickListener() throws Exception {
		selectItem(2, false);
		verify(listener, times(1)).onItemsSelected(false, new boolean[]{true, true, false});
		selectItem(2, true);
		verify(listener, times(1)).onItemsSelected(true, new boolean[]{true, true, true});
	}

	private void selectItem(int position, boolean select) {
		if (position >= items.size()) {
			throw new IndexOutOfBoundsException("Invalid position " + position
					+ ", size is " + items.size());
		}
		spinner.onClick(null, position, select);
		spinner.onCancel(null);
	}
}
