package fi.iki.kuitsi.bitbeaker;

import android.app.Activity;

public final class ActivityComponentFactory {

	private ActivityComponentFactory() {
		throw new AssertionError("No instance");
	}

	public static ActivityComponent create(Activity activity) {
		AppComponent appComponent = AppComponentService.obtain(activity.getApplication());
		return appComponent.activityComponentBuilder().build();
	}

}
