package fi.iki.kuitsi.bitbeaker;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.support.v4.util.SimpleArrayMap;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fi.iki.kuitsi.bitbeaker.util.ResourceUtils;
import fi.iki.kuitsi.bitbeaker.util.StringUtils;

public class Helper {

	private Helper() { }

	private static Context context;

	public static void setContext(Context context) {
		Helper.context = context;
		initTranslatedApiStrings();
	}

	/**
	 * Use this if you want to display String used in API queries to user.
	 *
	 * @param apiString String which can be used directly in API queries
	 * @return Translated version of that String
	 * @see #initTranslatedApiStrings for supported Strings
	 */
	public static String translateApiString(String apiString) {
		if (translatedApiStrings.containsKey(apiString)) {
			return translatedApiStrings.get(apiString);
		}
		return "ERROR: value not found";
	}

	/**
	 * Contains mappings to display translated versions of String used in API calls and vice versa.
	 * Language is selected by Android from strings.xml resources available.
	 *
	 * @param Key: API String
	 * @param Value: Translated String
	 * @see #initTranslatedApiStrings
	 */
	private static SimpleArrayMap<String, String> translatedApiStrings;

	/**
	 * Context MUST be set with setContext(Context) before you can call this!
	 *
	 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/Use+the+Bitbucket+REST+APIs">Use the Bitbucket REST APIs</a> for possible values
	 */
	private static void initTranslatedApiStrings() {
		translatedApiStrings = new SimpleArrayMap<>();
		translatedApiStrings.put("new", context.getString(R.string.api_status_new));
		translatedApiStrings.put("open", context.getString(R.string.api_status_open));
		translatedApiStrings.put("resolved", context.getString(R.string.api_status_resolved));
		translatedApiStrings.put("on hold", context.getString(R.string.api_status_on_hold));
		translatedApiStrings.put("invalid", context.getString(R.string.api_status_invalid));
		translatedApiStrings.put("duplicate", context.getString(R.string.api_status_duplicate));
		translatedApiStrings.put("wontfix", context.getString(R.string.api_status_wontfix));
		translatedApiStrings.put("closed", context.getString(R.string.api_status_closed));
		translatedApiStrings.put("trivial", context.getString(R.string.api_priority_trivial));
		translatedApiStrings.put("minor", context.getString(R.string.api_priority_minor));
		translatedApiStrings.put("major", context.getString(R.string.api_priority_major));
		translatedApiStrings.put("critical", context.getString(R.string.api_priority_critical));
		translatedApiStrings.put("blocker", context.getString(R.string.api_priority_blocker));
		translatedApiStrings.put("bug", context.getString(R.string.api_kind_bug));
		translatedApiStrings.put("enhancement", context.getString(R.string.api_kind_enhancement));
		translatedApiStrings.put("proposal", context.getString(R.string.api_kind_proposal));
		translatedApiStrings.put("task", context.getString(R.string.api_kind_task));
		translatedApiStrings.put("added", context.getString(R.string.api_changeset_files_type_added));
		translatedApiStrings.put("modified", context.getString(R.string.api_changeset_files_type_modified));
		translatedApiStrings.put("removed", context.getString(R.string.api_changeset_files_type_removed));
	}

	/**
	 * Check if file is an image based on it's extension.
	 *
	 * @see <a href="http://developer.android.com/guide/appendix/media-formats.html">Supported Media Formats</a>
	 */
	public static boolean isImage(String filename) {
		if (filename == null) return false;

		if (filename.toLowerCase().endsWith(".png")
				|| filename.toLowerCase().endsWith(".jpg")
				|| filename.toLowerCase().endsWith(".bmp")
				|| filename.toLowerCase().endsWith(".gif")) {
			return true;
		}
		return false;
	}

	/**
	 * Formats the given date into a nicer date + time string according to the current locale.
	 *
	 * @param date Date to format
	 * @return Localized, nice version of the given timestamp
	 */
	public static String formatDate(Date date) {
		if (date == null) return "";
		DateFormat targetDateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
		DateFormat targetTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
		return targetDateFormat.format(date) + " " + targetTimeFormat.format(date);
	}

	/**
	 * Tries to parse a Date from Bitbucket's scraped HTML source code.
	 *
	 * @param datetime contents of datetime attribute of time tag
	 *        For example 2014-06-06T22:08:03+03:00 or 2015-01-12T20:07:08.658593
	 * @return Date or null if parsing fails
	 */
	public static Date parseBitbucketDatetime(String datetime) {
		Date date;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		try {
			date = format.parse(datetime);
		} catch (ParseException e1) {
			format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
			try {
				date = format.parse(datetime);
			} catch (ParseException e2) {
				format = new SimpleDateFormat("yyyy-MM-dd");
				try {
					date = format.parse(datetime.substring(0, 10));
				} catch (ParseException | IndexOutOfBoundsException e3) {
					// give up and hope these will be provided via API in the future :)
					date = null;
				}
			}
		}
		return date;
	}

	/**
	 * Checks if URL needs Authentication by checking if the domain is bitbucket.org or
	 * api.bitbucket.org.
	 */
	public static Boolean urlNeedsAuthentication(URL url) {
		return "bitbucket.org".equals(url.getHost().toLowerCase())
				|| "api.bitbucket.org".equals(url.getHost().toLowerCase());
	}

	/**
	 * Counts an empty string, a null string or the string
	 * "null" (with any amount of leading or trailing whitespace) as empty.
	 *
	 * @param string The string to be examined
	 * @return True if the string is null, "null", empty or whitespace only
	 */
	public static boolean isJsonEmpty(final String string) {
		return StringUtils.isBlank(string) || "null".equals(string.trim());
	}

	/**
	 * Turns the given URL into an HTML link.
	 *
	 * @param url An URL, 'http://www.example.com/', for example.
	 * @return HTML link, e.g. '<a href="http://www.example.com/">http://www.example.com/</a>'.
	 */
	public static String getHtmlLink(String url) {
		return "<a href=\"" + url + "\">" + url + "</a>";
	}

	/**
	 * This method formats a TextView as if it was a hyperlink. This method
	 * is useful when it's not a real HTML link but there's an OnClickListener
	 * assigned to the TextView, taking you to some other action.
	 *
	 * @param t The TextView whose appearance is to be changed.
	 */
	public static TextView renderAsLink(TextView t) {
		t.setPaintFlags(t.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		t.setTextColor(Color.BLUE);
		return t;
	}

	public static String formatRelativeDate(Context dateContext, Date date) {
		return DateUtils.getRelativeDateTimeString(dateContext, date.getTime(), DateUtils.MINUTE_IN_MILLIS,
				DateUtils.DAY_IN_MILLIS * 2, 0).toString();
	}

	/**
	 * Use this instead of Locale.getDefault() as we might use other language than device's default.
	 *
	 * @return Locale currently in use
	 *
	 * @deprecated Use {@link ResourceUtils#getCurrentLocale(Context)} instead.
	 */
	@Deprecated
	public static Locale getCurrentLocale() {
		return ResourceUtils.getCurrentLocale(context);
	}

	/**
	 * Checks if external storage is available for read and write.
	 */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if external storage is available to at least read.
	 */
	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	/**
	 * Copies String to clipboard.
	 *
	 * @param text text to copy
	 * @return true on success
	 */
	public static boolean copyStringToClipboard(String text) {
		android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
				context.getSystemService(Context.CLIPBOARD_SERVICE);
		android.content.ClipData clip = android.content.ClipData.newPlainText(
				context.getString(R.string.app_name), text);
		clipboard.setPrimaryClip(clip);
		return true;
	}

	/**
	 * Protects against ActivityNotFoundException caused by faulty links in TextViews.
	 *
	 * Source: https://commonsware.com/blog/2013/10/23/linkify-autolink-need-custom-urlspan.html
	 */
	public static void fixURLSpans(TextView tv) {
		SpannableString current = (SpannableString) tv.getText();
		URLSpan[] spans = current.getSpans(0, current.length(), URLSpan.class);

		for (URLSpan span : spans) {
			int start = current.getSpanStart(span);
			int end = current.getSpanEnd(span);

			current.removeSpan(span);
			current.setSpan(new DefensiveURLSpan(span.getURL()), start, end, 0);
		}
	}

	/**
	 * Helper class for {@link #fixURLSpans(android.widget.TextView)}.
	 *
	 * Source: https://commonsware.com/blog/2013/10/23/linkify-autolink-need-custom-urlspan.html
	 */
	private static class DefensiveURLSpan extends URLSpan {
		public DefensiveURLSpan(String url) {
			super(url);
		}

		@Override
		public void onClick(View widget) {
			try {
				super.onClick(widget);
			} catch (ActivityNotFoundException e) {
				android.util.Log.d(getClass().getSimpleName(), "Faulty link detected!", e);
				Toast.makeText(widget.getContext(), R.string.open_link_failed,
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
