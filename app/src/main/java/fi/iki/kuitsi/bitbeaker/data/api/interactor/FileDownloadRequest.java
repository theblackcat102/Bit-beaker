package fi.iki.kuitsi.bitbeaker.data.api.interactor;

import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import fi.iki.kuitsi.bitbeaker.data.api.model.repositories.DownloadableItem;
import okhttp3.ResponseBody;
import retrofit2.Call;

public final class FileDownloadRequest implements BitbucketApiCall<ResponseBody> {

	private final String link;

	public FileDownloadRequest(DownloadableItem item) {
		this.link = item.links.self();
	}

	@Override
	public Class<ResponseBody> responseType() {
		return ResponseBody.class;
	}

	@Override
	public Call<ResponseBody> call(BitbucketService service) {
		return service.downloadFile(link);
	}
}
