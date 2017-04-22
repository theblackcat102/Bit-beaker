package fi.iki.kuitsi.bitbeaker.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.octo.android.robospice.networkstate.NetworkStateChecker;

/**
 * Check network connectivity and connection type. This class assumes that both
 * {@link android.Manifest.permission#ACCESS_NETWORK_STATE} and
 * {@link android.Manifest.permission#INTERNET} are granted.
 *
 * <a href="https://gist.github.com/emil2k/5130324">Android utility class for checking device's network connectivity and speed.</a>
 */
public class ConnectivityChecker implements NetworkStateChecker {

	/**
	 * Transfer rate enumeration.
	 */
	public enum TransferRate {
		TRANSFER_RATE_SLOW,
		TRANSFER_RATE_MEDIUM,
		TRANSFER_RATE_FAST,
		TRANSFER_RATE_UNKNOWN
	}

	@Override
	public boolean isNetworkAvailable(final Context context) {
		return ConnectivityChecker.isConnected(context);
	}

	@Override
	public void checkPermissions(final Context context) {

	}

	/**
	 * Get the network info from {@link android.net.ConnectivityManager}.
	 * @param context context from which network state is accessed
	 * @return a {@link NetworkInfo} object for the current default network
	 * or {@code null} if no network default network is currently active
	 */
	public static NetworkInfo getNetworkInfo(final Context context) {
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
				Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo();
	}

	/**
	 * Indicates whether connectivity exists.
	 * @param context context from which network state is accessed
	 * @return {@code true} if network connectivity exists, {@code false} otherwise
	 */
	public static boolean isConnected(final Context context) {
		final NetworkInfo info = ConnectivityChecker.getNetworkInfo(context);
		return (info != null && info.isConnectedOrConnecting());
	}

	/**
	 * Returns the transfer rate of network connectivity.
	 * @param context context from which network state is accessed
	 * @return transfer rate
	 * @see ConnectivityChecker.TransferRate
	 */
	public static TransferRate getNetworkTransferRate(final Context context) {
		final NetworkInfo info = ConnectivityChecker.getNetworkInfo(context);
		if (info != null && info.isConnected()) {
			return ConnectivityChecker.classifyTransferRate(info);
		} else {
			return TransferRate.TRANSFER_RATE_UNKNOWN;
		}
	}

	/**
	 * Classify transfer rate of network connectivity.
	 * @param info Network info
	 * @return transfer rate
	 */
	private static TransferRate classifyTransferRate(final NetworkInfo info) {
		final int type = info.getType();
		if (type == ConnectivityManager.TYPE_ETHERNET
			|| type == ConnectivityManager.TYPE_WIFI) {
			return TransferRate.TRANSFER_RATE_MEDIUM;
		} else if (type == ConnectivityManager.TYPE_MOBILE) {
			switch (info.getSubtype()) {
				case TelephonyManager.NETWORK_TYPE_GPRS:    // ~ 56–114 kbps
				case TelephonyManager.NETWORK_TYPE_EDGE:    // ~ 50-100 kbps
				case TelephonyManager.NETWORK_TYPE_CDMA:    // ~ 14-64 kbps
				case TelephonyManager.NETWORK_TYPE_1xRTT:   // ~ 50-100 kbps
				case TelephonyManager.NETWORK_TYPE_IDEN:    // ~ 25 kbps
					return TransferRate.TRANSFER_RATE_SLOW;
				case TelephonyManager.NETWORK_TYPE_UMTS:    // ~ 400-7000 kbps
				case TelephonyManager.NETWORK_TYPE_EVDO_0:  // ~ 400-1000 kbps
				case TelephonyManager.NETWORK_TYPE_EVDO_A:  // ~ 600-1400 kbps
				case TelephonyManager.NETWORK_TYPE_HSDPA:   // ~ 2-14 Mbps
				case TelephonyManager.NETWORK_TYPE_HSUPA:   // ~ 1-23 Mbps
				case TelephonyManager.NETWORK_TYPE_HSPA:    // ~ 700-1700 kbps
				case TelephonyManager.NETWORK_TYPE_EHRPD:   // ~ 1-2 Mbps, API level 11
					return TransferRate.TRANSFER_RATE_MEDIUM;
				case TelephonyManager.NETWORK_TYPE_EVDO_B:  // ~ 5 Mbps, API level 9
				case TelephonyManager.NETWORK_TYPE_LTE:     // ~ 10+ Mbps, API level 11
				case TelephonyManager.NETWORK_TYPE_HSPAP:   // ~ 10-20 Mbps, API level 13
					return TransferRate.TRANSFER_RATE_FAST;
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				default:
					return TransferRate.TRANSFER_RATE_UNKNOWN;
			}
		} else {
			return TransferRate.TRANSFER_RATE_UNKNOWN;
		}
	}
}
