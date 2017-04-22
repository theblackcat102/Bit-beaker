package fi.iki.kuitsi.bitbeaker;

import android.accounts.Account;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;

import de.greenrobot.event.EventBus;
import fi.iki.kuitsi.bitbeaker.preferences.PreferencesModule;
import fi.iki.kuitsi.bitbeaker.preferences.StringPreference;
import fi.iki.kuitsi.bitbeaker.util.LocaleUtils;
import roboguice.util.temp.Ln;

/**
 * The Bitbeaker application. It's used to maintain the global application state.
 */
public class Bitbeaker extends Application {

	public static final String REPO_OWNER = "bitbeaker-dev-team";
	public static final String REPO_SLUG = "bitbeaker";

	private static final String USER_AGENT = "Bitbeaker" + '/' + BuildConfig.VERSION_NAME
			+ " (Android " + android.os.Build.VERSION.RELEASE + ")";

	private AppComponent appComponent;
	private StringPreference localeOverride;

	@Override
	public void onCreate() {
		super.onCreate();
		Helper.setContext(getApplicationContext());
		localeOverride = PreferencesModule.provideLocalOverridePreference(this);
		initLogging();
		appComponent = DaggerAppComponent.builder()
				.appModule(new AppModule(this))
				.build();
	}

	@Override
	public Object getSystemService(@NonNull String name) {
		if (AppComponentService.matchesService(name)) {
			return appComponent;
		}
		return super.getSystemService(name);
	}

	private void initLogging() {
		Ln.getConfig().setLoggingLevel(Log.ERROR);
	}

	public Account getAccount() {
		return appComponent.userManager().getAccount();
	}

	@Deprecated
	public String getUsername() {
		Account account = getAccount();
		if (account != null) {
			return account.name;
		}
		return "";
	}

	/**
	 * Updates the language based on Preferences.
	 *
	 * @see #updateLanguage(android.content.Context, String)
	 */
	public void updateLanguage() {
		String lang = localeOverride.get();
		updateLanguage(this, lang);
	}

	/**
	 * Updates the language if needed.
	 *
	 * @param ctx ApplicationContext
	 * @param lang String representation of a Locale, like "en" or "en_US"
	 */
	private static void updateLanguage(Context ctx, String lang) {
		Configuration cfg = ctx.getResources().getConfiguration();
		String oldLocale = cfg.locale.toString();
		if (!oldLocale.equals(lang)) {
			if (!TextUtils.isEmpty(lang)) {
				cfg.locale = LocaleUtils.toLocale(lang);
			} else {
				cfg.locale = Locale.getDefault();
			}
			ctx.getResources().updateConfiguration(cfg, ctx.getResources().getDisplayMetrics());
			Helper.setContext(ctx);// translate Strings used in API queries
			System.out.println("Updated configuration from " + oldLocale + " to "
					+ cfg.locale.toString());
		}
	}

	/**
	 * Returns application instance from any context.
	 */
	public static Bitbeaker get(Context context) {
		return (Bitbeaker) context.getApplicationContext();
	}

	/**
	 * Returns the {@link EventBus} instance.
	 */
	public static EventBus getEventBus() {
		//CHECKSTYLE.OFF: RegexpSinglelineJava - The sole exception
		return EventBus.getDefault();
		//CHECKSTYLE:ON: RegexpSinglelineJava
	}

	/**
	 * Returns unified User Agent String for various WebViews.
	 *
	 * @see android.webkit.WebView#getSettings()
	 * @see android.webkit.WebSettings#setUserAgentString(String)
	 */
	public static String getUserAgentString() {
		return USER_AGENT;
	}
}
