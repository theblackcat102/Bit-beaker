package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.comparators.RepositoryRelevanceComparator;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;

import java.util.Collections;

public class RequestSearchRepository extends BitbucketRequest<Repository.List> {

	private final String query;

	public RequestSearchRepository(String query) {
		super(Repository.List.class);
		this.query = query;
	}

	@Override
	public Repository.List loadDataFromNetwork() throws Exception {
		Repository.List repositories = getService().repositorySearch(query).loadDataFromNetwork().getRepositories();

		// Do sorting in the background thread.
		Collections.sort(repositories, new RepositoryRelevanceComparator());
		return repositories;
	}

	@Override
	public String getCacheKey() {
		// Unique cache key for every repository search.
		return "repositorysearch+" + query;
	}

	@Override
	public long getCacheExpireDuration() {
		return 10L * DurationInMillis.ONE_MINUTE;
	}
}
