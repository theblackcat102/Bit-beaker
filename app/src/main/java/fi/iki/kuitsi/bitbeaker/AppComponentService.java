package fi.iki.kuitsi.bitbeaker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

public final class AppComponentService {

	private static final String SERVICE_NAME = AppComponentService.class.getCanonicalName();

	private AppComponentService() {
		throw new AssertionError("No instance");
	}

	@SuppressWarnings("ResourceType")
	public static AppComponent obtain(@NonNull Context context) {
		return (AppComponent) context.getSystemService(SERVICE_NAME);
	}

	public static boolean matchesService(String name) {
		return SERVICE_NAME.equals(name);
	}

	@VisibleForTesting
	public static String getServiceName() {
		return SERVICE_NAME;
	}
}
