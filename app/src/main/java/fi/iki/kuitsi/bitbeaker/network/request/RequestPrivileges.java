package fi.iki.kuitsi.bitbeaker.network.request;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.domainobjects.Privilege;

public class RequestPrivileges extends BitbucketRequest<Privilege.List> {

	private final String owner;
	private final String slug;
	private final String account;

	public RequestPrivileges(String owner, String slug, String account) {
		super(Privilege.List.class);
		this.owner = owner;
		this.slug = slug;
		this.account = account;
	}

	@Override
	public Privilege.List loadDataFromNetwork() throws Exception {
		return getService().privilege(owner, slug, account).loadDataFromNetwork();
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ALWAYS_EXPIRED;
	}
}
