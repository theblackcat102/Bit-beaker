package fi.iki.kuitsi.bitbeaker.network.request.users;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;
import fi.iki.kuitsi.bitbeaker.network.response.users.AccountProfile;

/**
 * GET the account profile using `account` resource of `users` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/account+Resource#accountResource-GETtheaccountprofile">account Resource - Bitbucket - Atlassian Documentation</a>
 */
public class AccountProfileRequest extends BitbucketRequest<AccountProfile> {

	private final String accountname;

	public AccountProfileRequest(String accountname) {
		super(AccountProfile.class);
		this.accountname = accountname;
	}

	@Override
	public String getCacheKey() {
		return "accountprofile" + String.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ONE_HOUR;
	}

	@Override
	public AccountProfile loadDataFromNetwork() throws Exception {
		return getService().accountProfile(accountname).loadDataFromNetwork();
	}

	@Override
	public int hashCode() {
		return accountname.hashCode();
	}
}
