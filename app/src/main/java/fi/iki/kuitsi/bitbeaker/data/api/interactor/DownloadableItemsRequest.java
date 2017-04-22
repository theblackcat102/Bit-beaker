package fi.iki.kuitsi.bitbeaker.data.api.interactor;

import com.google.auto.value.AutoValue;

import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import fi.iki.kuitsi.bitbeaker.data.api.model.repositories.DownloadableItems;
import retrofit2.Call;

/**
 * Gets a list of links associated with the repository.
 * @see <a href="https://confluence.atlassian.com/bitbucket/downloads-resource-812221853.html#downloadsResource-GETalistofdownloads">downloads Resource - Atlassian Documentation</a>
 */
@AutoValue public abstract class DownloadableItemsRequest implements BitbucketApiCall<DownloadableItems> {

	abstract String owner();
	abstract String repoSlug();

	public static DownloadableItemsRequest create(String owner, String repoSlug) {
		return new AutoValue_DownloadableItemsRequest(owner, repoSlug);
	}

	@Override
	public Class<DownloadableItems> responseType() {
		return DownloadableItems.class;
	}

	@Override
	public Call<DownloadableItems> call(BitbucketService service) {
		return service.download(owner(), repoSlug());
	}
}
