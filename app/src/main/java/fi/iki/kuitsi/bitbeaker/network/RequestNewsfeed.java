package fi.iki.kuitsi.bitbeaker.network;

import android.net.Uri;

import com.octo.android.robospice.request.SpiceRequest;

import org.xml.sax.InputSource;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import zeroone.rss.Channel;
import zeroone.rss.RssParser;

/**
 * bitbucket.org feed.
 */
public final class RequestNewsfeed extends SpiceRequest<Channel> {

	private final String url;

	public RequestNewsfeed(String owner, String token) {
		super(Channel.class);
		url = new Uri.Builder()
				.scheme("https")
				.authority("bitbucket.org")
				.appendPath(owner)
				.appendPath("rss")
				.appendPath("feed")
				.appendQueryParameter("token", token).toString();
	}

	@Override
	public Channel loadDataFromNetwork() throws Exception {
		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) new URL(url).openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();
			InputStream inputStream = urlConnection.getInputStream();
			return RssParser.parseFeed(new InputSource(inputStream));
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
	}
}
