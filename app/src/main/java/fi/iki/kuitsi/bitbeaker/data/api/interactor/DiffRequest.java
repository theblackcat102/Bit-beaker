package fi.iki.kuitsi.bitbeaker.data.api.interactor;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import retrofit2.Call;

/**
 * Gets the diff for the current Bitbucket Cloud repository.
 * @see <a href="https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/diff/%7Bspec%7D">Bitbucket API</a>
 */
@AutoValue public abstract class DiffRequest implements BitbucketApiCall<String> {

	public static Builder builder() {
		return new AutoValue_DiffRequest.Builder();
	}

	@Override
	public Class<String> responseType() {
		return String.class;
	}

	@Override
	public Call<String> call(BitbucketService service) {
		return service.diff(username(), repoSlug(), spec(), path());
	}

	abstract String username();
	abstract String repoSlug();
	abstract String spec();
	abstract @Nullable String path();

	@AutoValue.Builder public abstract static class Builder {
		/** The team or individual account owning the repo. */
		public abstract Builder username(String value);
		/** The repo identifier. */
		public abstract Builder repoSlug(String value);
		/** A specification such as a branch name, revision, or commit SHA. */
		public abstract Builder spec(String value);
		/** Limit the diff to a single file. */
		public abstract Builder path(String value);
		public abstract DiffRequest build();
	}
}
