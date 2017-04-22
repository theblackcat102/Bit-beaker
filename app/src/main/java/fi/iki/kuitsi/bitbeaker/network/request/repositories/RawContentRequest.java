package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;

import fi.iki.kuitsi.bitbeaker.util.Objects;

/**
 * GET raw content of an individual file.
 *
 * @see <a href="https://confluence.atlassian.com/bitbucket/src-resources-296095214.html#srcResources-GETrawcontentofanindividualfile">src Resources - Atlassian Documentation</a>
 */
public final class RawContentRequest extends BaseRepositoriesRequest<String> {

	private final String revision;
	private final String path;

	public RawContentRequest(String accountname, String slug, String revision, String path) {
		super(String.class, accountname, slug);
		this.revision = revision;
		this.path = path;
	}

	@Override
	public String loadDataFromNetwork() throws Exception {
		return getService().fileContent(accountname, slug, revision, path).loadDataFromNetwork();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(accountname, slug, revision, path);
	}

	@Override
	public String getCacheKey() {
		return "rawcontent" + String.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ONE_MINUTE;
	}
}
