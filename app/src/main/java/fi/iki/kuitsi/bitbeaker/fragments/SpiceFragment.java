package fi.iki.kuitsi.bitbeaker.fragments;

import android.support.v4.app.Fragment;

import com.octo.android.robospice.SpiceManager;
import fi.iki.kuitsi.bitbeaker.network.RestService;

/**
 * Base class that can be used to implement a fragment that use the
 * {@link fi.iki.kuitsi.bitbeaker.network.RestService}.
 */
abstract class SpiceFragment extends Fragment {

	/**
	 * The {@link SpiceManager} allow to access the
	 * {@link fi.iki.kuitsi.bitbeaker.network.RestService}.
	 */
	private final SpiceManager spiceManager = new SpiceManager(RestService.class);

	@Override
	public void onStart() {
		super.onStart();
		spiceManager.start(getActivity());
	}

	@Override
	public void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}

	/**
	 * Return the SpiceManager for this fragment.
	 */
	SpiceManager getSpiceManager() {
		return spiceManager;
	}
}
