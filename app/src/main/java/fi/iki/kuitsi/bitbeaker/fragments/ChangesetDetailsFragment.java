package fi.iki.kuitsi.bitbeaker.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fi.iki.kuitsi.bitbeaker.view.ChangesetDetailsLayout;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;

import java.util.ArrayList;

/**
 * Display changeset details.
 */
public class ChangesetDetailsFragment extends Fragment {

	private static final String TAG = "ChangesetDetailsFragment";

	ChangesetDetailsLayout changesetDetails;

	public ChangesetDetailsFragment() {
		// Mandatory empty constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
			savedInstanceState) {
		return inflater.inflate(R.layout.changeset_details_scrollable, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		changesetDetails = (ChangesetDetailsLayout) view.findViewById(R.id.changeset_details);
	}

	@Override
	public void onDestroyView() {
		changesetDetails = null;
		super.onDestroyView();
	}

	/**
	 * Sets changeset displayed by the fragment.
	 */
	public void setChangeset(Changeset changeset) {
		Log.d(TAG, "setChangeset");
		if (changesetDetails != null) {
			changesetDetails.setChangeset(changeset);
		} else {
			Log.e(TAG, "failed to set changeset");
		}
	}

	/**
	 * Sets tags displayed by the fragment.
	 */
	public void setTags(ArrayList<String> tags) {
		Log.d(TAG, "setTags from ChangesetDetailsFragment");
		if (changesetDetails != null) {
			changesetDetails.setTags(tags);
		} else {
			Log.e(TAG, "failed to set tags");
		}
	}

}
