package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.Changesets;
import fi.iki.kuitsi.bitbeaker.util.Objects;

import java.util.Collections;

/**
 * GET a list of changesets using `changesets` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/changesets+Resource#changesetsResource-GETalistofchangesets">changesets Resource - Bitbucket - Atlassian Documentation</a>
 */
public class ChangesetsRequest extends BaseRepositoriesRequest<Changeset.List> {

	/**
	 * A hash value representing the earliest node to start with. Can be null.
	 */
	private String start;
	/**
	 * An integer representing how many changesets to return. Can be null.
	 */
	private Integer limit;

	/**
	 * Constructs a changeset list request.
	 *
	 * @param accountname The team or individual account owning the repo
	 * @param slug The repo identifier
	 */
	public ChangesetsRequest(String accountname, String slug) {
		super(Changeset.List.class, accountname, slug);
		init(null, null);
	}

	/**
	 * Constructs a changeset list request.
	 *
	 * @param accountname The team or individual account owning the repo
	 * @param slug The repo identifier
	 * @param limit An integer representing how many changesets to return
	 */
	public ChangesetsRequest(String accountname, String slug, int limit) {
		super(Changeset.List.class, accountname, slug);
		init(null, limit);
	}

	private void init(String start, Integer limit) {
		this.start = start;
		if (limit != null && limit > 0) {
			this.limit = limit;
		}
	}

	public Integer getLimit() {
		return limit;
	}

	/**
	 * Set a hash value representing the earliest node to start with.
	 *
	 * @param start Hash value of starting changeset.
	 */
	public void setStart(String start) {
		this.start = start;
	}

	@Override
	public String getCacheKey() {
		return "changesets" + String.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return 15 * DurationInMillis.ONE_MINUTE;
	}

	@Override
	public Changeset.List loadDataFromNetwork() throws Exception {
		Changesets response = getService().changesets(accountname, slug, start, limit).loadDataFromNetwork();
		Changeset.List changesets = response.changesets;
		Collections.reverse(changesets);
		return changesets;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(accountname, slug, start, limit);
	}
}
