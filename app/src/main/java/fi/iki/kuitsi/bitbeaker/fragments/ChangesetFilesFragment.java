package fi.iki.kuitsi.bitbeaker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import fi.iki.kuitsi.bitbeaker.view.ChangesetDetailsLayout;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.adapters.ChangesetAdapter;
import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;

import java.util.ArrayList;

/**
 * Display list of files part of a changeset.
 */
public class ChangesetFilesFragment extends ListFragment {

	/**
	 * Creates a ChangesetFilesFragment and sets its arguments.
	 *
	 * @param intent The host activity's intent
	 */
	public static ChangesetFilesFragment newInstance(Intent intent) {
		ChangesetFilesFragment fragment = new ChangesetFilesFragment();
		fragment.setArguments(new Bundle(intent.getExtras()));
		return fragment;
	}

	private ListAdapter adapter;
	private Changeset changeset;
	private ChangesetDetailsLayout header;
	private ArrayList<String> tags;

	public ChangesetFilesFragment() {
		// Mandatory empty constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String owner = getArguments().getString("owner");
		String slug = getArguments().getString("slug");
		changeset = getArguments().getParcelable("changeset");
		tags = getArguments().getStringArrayList("tags");

		adapter = new ChangesetAdapter(getActivity(), changeset, owner, slug);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
			savedInstanceState) {
		if (getResources().getBoolean(R.bool.changeset_header)) {
			header = new ChangesetDetailsLayout(getActivity());
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (header != null) {
			header.setChangeset(changeset);
			header.setTags(tags);
			getListView().addHeaderView(header);
		}
		setListAdapter(adapter);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		header = null;
	}
}
