package fi.iki.kuitsi.bitbeaker.data.remote;

import com.octo.android.robospice.SpiceManager;

import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;

public final class BitbucketApiCallCompiler extends ApiCallCompiler<BitbucketService> {
	private BitbucketApiCallCompiler(SpiceManager spiceManager) {
		super(spiceManager);
	}

	public static ApiCallCompilerStages.RApiCall<BitbucketService> using(SpiceManager spiceManager) {
		return new BitbucketApiCallCompiler(spiceManager);
	}
}
