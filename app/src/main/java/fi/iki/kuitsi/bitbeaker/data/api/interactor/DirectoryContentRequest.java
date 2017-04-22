package fi.iki.kuitsi.bitbeaker.data.api.interactor;

import com.google.auto.value.AutoValue;

import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import fi.iki.kuitsi.bitbeaker.data.api.model.repositories.DirectoryContent;
import retrofit2.Call;

/**
 * Gets a list of the src in a repository.
 */
@AutoValue  public abstract class DirectoryContentRequest implements BitbucketApiCall<DirectoryContent> {

	public static DirectoryContentRequest create(String accountName, String repoSlug, String revision) {
		return create(accountName, repoSlug, revision, "/");
	}

	public static DirectoryContentRequest create(String accountName, String repoSlug, String revision, String path) {
		return new AutoValue_DirectoryContentRequest(accountName, repoSlug, revision, path);
	}

	@Override
	public Class<DirectoryContent> responseType() {
		return DirectoryContent.class;
	}

	@Override
	public Call<DirectoryContent> call(BitbucketService service) {
		return service.repoSource(accountName(), repoSlug(), revision(), path());
	}

	abstract String accountName();
	abstract String repoSlug();
	abstract String revision();
	abstract String path();
}
