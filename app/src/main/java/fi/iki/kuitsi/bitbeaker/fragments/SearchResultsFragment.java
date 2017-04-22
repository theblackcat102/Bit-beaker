package fi.iki.kuitsi.bitbeaker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.adapters.SearchableRepositoriesAdapter;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.RequestSearchRepository;
import fi.iki.kuitsi.bitbeaker.network.request.users.AccountProfileRequest;
import fi.iki.kuitsi.bitbeaker.network.response.users.AccountProfile;

/**
 * Search results.
 */
public class SearchResultsFragment extends SpiceFragment {

	private View progressContainer;
	private View listContainer;
	private SearchableRepositoriesAdapter adapter;
	private RequestSearchRepository repositorySearchRequest;
	private AccountProfileRequest accountProfileRequest;
	private boolean repositoriesLoaded;
	private boolean accountLoaded;

	public SearchResultsFragment() {
		// Mandatory empty constructor
	}

	public static SearchResultsFragment newInstance(String query) {
		SearchResultsFragment fragment = new SearchResultsFragment();
		Bundle arguments = new Bundle();
		arguments.putString("query", query);
		fragment.setArguments(arguments);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load arguments
		String query = getArguments().getString("query");
		repositorySearchRequest = new RequestSearchRepository(query);
		accountProfileRequest = new AccountProfileRequest(query);
		adapter = new SearchableRepositoriesAdapter(getActivity(), query);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_content, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		progressContainer = view.findViewById(R.id.progress_container);
		listContainer = view.findViewById(R.id.list_container);
		ListView list = (ListView) view.findViewById(android.R.id.list);
		TextView emptyText = (TextView) view.findViewById(android.R.id.empty);
		emptyText.setText(R.string.no_repositories_found);
		list.setAdapter(adapter);
		list.setEmptyView(emptyText);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getSpiceManager().execute(repositorySearchRequest, repositorySearchRequest.getCacheKey(),
				repositorySearchRequest.getCacheExpireDuration(), new SearchRequestListener());
		getSpiceManager().execute(accountProfileRequest, accountProfileRequest.getCacheKey(),
				accountProfileRequest.getCacheExpireDuration(),
				new AccountProfileRequestListener());
		progressContainer.setVisibility(View.VISIBLE);
		listContainer.setVisibility(View.INVISIBLE);
	}

	private void showResults() {
		if (repositoriesLoaded && accountLoaded) {
			progressContainer.setVisibility(View.INVISIBLE);
			listContainer.setVisibility(View.VISIBLE);
		}
	}

	private final class SearchRequestListener implements RequestListener<Repository.List> {
		@Override
		public void onRequestFailure(SpiceException e) {
			repositoriesLoaded = true;
			showResults();
		}

		@Override
		public void onRequestSuccess(Repository.List repositories) {
			repositoriesLoaded = true;
			adapter.addAll(repositories);
			showResults();
		}
	}

	private class AccountProfileRequestListener implements RequestListener<AccountProfile> {
		@Override
		public void onRequestFailure(SpiceException spiceException) {
			accountLoaded = true;
			showResults();
		}

		@Override
		public void onRequestSuccess(AccountProfile accountProfile) {
			accountLoaded = true;
			adapter.addAll(accountProfile.getRepositories());
			showResults();
		}
	}
}
