package fi.iki.kuitsi.bitbeaker.util;

import android.content.Context;
import android.content.res.Resources;

import java.util.Locale;

public final class ResourceUtils {

	private ResourceUtils() { }

	/**
	 * Use this instead of Locale.getDefault() as we might use other language than device's default.
	 *
	 * @return Locale currently in use
	 */
	public static Locale getCurrentLocale(Context context) {
		return context.getResources().getConfiguration().locale;
	}

	/**
	 * Retrieve a string resource identified by its key.
	 * @param context context
	 * @param key The name of the desired string resource.
	 * @return The string data associated with the key, or null if no such string resource was
	 * found.
	 */
	public static String getResourceStringValue(Context context, String key) {
		// Retrieve the resource id
		String packageName = context.getPackageName();
		Resources resources = context.getResources();
		int stringId = resources.getIdentifier(key, "string", packageName);
		if (stringId == 0) {
			return null;
		}
		// Return the string value based on the res id
		return resources.getString(stringId);
	}

}
