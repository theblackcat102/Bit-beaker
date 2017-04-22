package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.Tags;

/**
 * GET a list of tags using `tags` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/repository+Resource+1.0#repositoryResource1.0-GETalistofthetags">repository Resources - Bitbucket - Atlassian Documentation</a>
 */
public class TagsRequest extends BaseRepositoriesRequest<Tags> {

	/**
	 * Constructs a tag list request associated with the specified repository.
	 *
	 * @param accountname The team or individual account owning the repo
	 * @param slug The repo identifier
	 */
	public TagsRequest(String accountname, String slug) {
		super(Tags.class, accountname, slug);
	}

	@Override
	public String getCacheKey() {
		return "tags" + Integer.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ONE_WEEK;
	}

	@Override
	public Tags loadDataFromNetwork() throws Exception {
		return getService().tags(accountname, slug).loadDataFromNetwork();
	}

}
