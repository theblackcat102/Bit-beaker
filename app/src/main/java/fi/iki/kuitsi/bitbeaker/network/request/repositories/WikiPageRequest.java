package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import android.net.Uri;
import android.text.TextUtils;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.WikiPage;
import fi.iki.kuitsi.bitbeaker.util.Objects;

import java.util.List;

/**
 * GET the raw content of a Wiki page using `wiki` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/wiki+Resources#wikiResources-GETtherawcontentofaWikipage">wiki Resources - Bitbucket - Atlassian Documentation</a>
 */
public class WikiPageRequest extends BaseRepositoriesRequest<WikiPage> {

	private String page;

	public WikiPageRequest(String accountname, String slug, String page) {
		super(WikiPage.class, accountname, slug);
		this.page = page;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public void setUrl(String url) {
		Uri uri = Uri.parse(url);
		List<String> pathSegments = uri.getPathSegments();
		int start = pathSegments.indexOf("wiki");
		if (start != -1) {
			List<String> pageSegments = pathSegments.subList(start + 1, pathSegments.size());
			String page = TextUtils.join("/", pageSegments);
			setPage(page);
		}
	}

	@Override
	public String getCacheKey() {
		return "wiki" + String.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ONE_HOUR;
	}

	@Override
	public WikiPage loadDataFromNetwork() throws Exception {
		return getService().wikiPage(accountname, slug, page).loadDataFromNetwork();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(accountname, slug, page);
	}
}
