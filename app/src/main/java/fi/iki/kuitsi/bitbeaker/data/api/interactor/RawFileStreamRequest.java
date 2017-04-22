package fi.iki.kuitsi.bitbeaker.data.api.interactor;

import com.google.auto.value.AutoValue;

import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import okhttp3.ResponseBody;
import retrofit2.Call;

@AutoValue public abstract class RawFileStreamRequest implements BitbucketApiCall<ResponseBody> {

	abstract String accountName();
	abstract String repoSlug();
	abstract String revision();
	abstract String path();

	public static RawFileStreamRequest create(String accountName, String repoSlug, String revision, String path) {
		return new AutoValue_RawFileStreamRequest(accountName, repoSlug, revision, path);
	}

	@Override
	public Class<ResponseBody> responseType() {
		return ResponseBody.class;
	}

	@Override
	public Call<ResponseBody> call(BitbucketService service) {
		return service.fileContentStream(accountName(), repoSlug(), revision(), path());
	}
}
