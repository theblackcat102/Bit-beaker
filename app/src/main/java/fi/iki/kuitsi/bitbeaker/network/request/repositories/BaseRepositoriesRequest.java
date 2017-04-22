package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;
import fi.iki.kuitsi.bitbeaker.util.Objects;

/**
 * Abstract request class for `repositories` endpoint. It stores the account name and the repository
 * name. It calculates hash code from these fields.
 */
abstract class BaseRepositoriesRequest<RESULT> extends BitbucketRequest<RESULT> {

	protected final String accountname;
	protected final String slug;

	public BaseRepositoriesRequest(Class<RESULT> clazz, String accountname, String slug) {
		super(clazz);
		this.accountname = accountname;
		this.slug = slug;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(accountname, slug);
	}
}
