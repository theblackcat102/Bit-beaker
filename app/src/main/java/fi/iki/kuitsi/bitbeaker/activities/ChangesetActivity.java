package fi.iki.kuitsi.bitbeaker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.adapters.SimpleFragmentPagerAdapter;
import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;
import fi.iki.kuitsi.bitbeaker.fragments.ChangesetCommentListFragment;
import fi.iki.kuitsi.bitbeaker.fragments.ChangesetDetailsFragment;
import fi.iki.kuitsi.bitbeaker.fragments.ChangesetFilesFragment;
import fi.iki.kuitsi.bitbeaker.network.RestService;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.TagsRequest;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.Tags;

import java.util.ArrayList;

/**
 * Activity that hosts changeset related fragments.
 */
public class ChangesetActivity extends BaseRepositoryActivity implements
		ChangesetCommentListFragment.Callbacks {

	private final SpiceManager spiceManager = new SpiceManager(RestService.class);
	private SimpleFragmentPagerAdapter fragmentPagerAdapter;
	private String rawNode;
	private ArrayList<String> tags;

	/**
	 * Create an intent for this activity.
	 */
	public static Intent createIntent(Context context, String owner, String slug,
			Changeset changeset, ArrayList<String> tags) {
		Intent intent = new Intent(context, ChangesetActivity.class);
		intent = BaseRepositoryActivity.addExtendedDataToIntent(intent, owner, slug);
		intent.putExtra("changeset", changeset);
		intent.putStringArrayListExtra("tags", tags);
		return intent;
	}

	public ChangesetActivity() {
		super(R.layout.changeset);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle b = getIntent().getExtras();
		Changeset changeset = b.getParcelable("changeset");

		if (changeset == null) return;

		String changesetId = changeset.getNode();

		setTitle(getSlug());
		setToolbarSubtitle(this.getString(R.string.changeset) + " " + changesetId);

		// Create the adapter that will return a fragment for each sections.
		fragmentPagerAdapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());
		fragmentPagerAdapter.addPage(
				ChangesetFilesFragment.newInstance(getIntent()),
				getString(R.string.changeset_changes));
		fragmentPagerAdapter.addPage(
				ChangesetCommentListFragment.newInstance(getIntent()),
				getString(R.string.changeset_comments));

		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(fragmentPagerAdapter);

		// Set up the View Pager title strip.
		PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
		pagerTabStrip.setDrawFullUnderline(true);
		pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.bitbeaker_control));

		ChangesetDetailsFragment fragment = (ChangesetDetailsFragment) getSupportFragmentManager()
				.findFragmentById(R.id.changeset_details_fragment);
		fragment.setChangeset(changeset);

		tags = b.getStringArrayList("tags");
		rawNode = changeset.getRawNode();
		if (tags == null) {
			TagsRequest tagsRequest = new TagsRequest(getOwner(), getSlug());
			spiceManager.execute(tagsRequest, tagsRequest.getCacheKey(),
					tagsRequest.getCacheExpireDuration(), new TagsRequestListener());
			showProgressBar(true);
		} else {
			fragment.setTags(tags);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		spiceManager.start(this);
	}

	@Override
	protected void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}

	@Override
	public void onCommentsLoaded(int num) {
		if (num > 0) {
			fragmentPagerAdapter.setPageTitle(1, getString(R.string.changeset_comments) + " (" + num + ")");
		}
	}

	private final class TagsRequestListener implements RequestListener<Tags> {
		@Override
		public void onRequestFailure(SpiceException e) {
			showProgressBar(false);
		}

		@Override
		public void onRequestSuccess(final Tags tagList) {
			tags = tagList.getTagsForChangeset(rawNode);
			getIntent().removeExtra("tags");
			setIntent(getIntent().putStringArrayListExtra("tags", tags));

			((ChangesetDetailsFragment) getSupportFragmentManager()
				.findFragmentById(R.id.changeset_details_fragment)).setTags(tags);
			showProgressBar(false);
		}
	}

}
