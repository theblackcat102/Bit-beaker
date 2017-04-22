package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.kuitsi.bitbeaker.domainobjects.IssueContainer;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.IssueFilterResult;
import fi.iki.kuitsi.bitbeaker.util.Objects;

/**
 * GET a list of issues in a repository's tracker using `issues` resource of `repositories`
 * endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/issues+Resource#issuesResource-GETalistofissuesinarepository%27stracker">issues Resource - Bitbucket - Atlassian Documentation</a>
 */
public final class IssuesRequest extends BaseRepositoriesRequest<IssueFilterResult> {

	public static final IssueStatusFilter RESOLVED = new IssueStatusFilter("resolved");

	/** An integer specifying the number of issues to return. */
	private final Integer limit;
	/** Offset to start at. */
	private final Integer start;
	/** A string to search for. */
	private final String search;
	/** Contains an is or ! ( is not) filter  to restrict the list of issues. */
	private final List<String> status;
	private final List<String> kind;
	private final Map<String, String> filter;


	private IssuesRequest(Builder builder) {
		super(IssueFilterResult.class, builder.accountName, builder.slug);
		this.limit = builder.limit;
		this.start = builder.start;
		this.search = builder.search;
		this.status = builder.status;
		this.kind = builder.kind;
		this.filter = builder.filter;
	}

	@Override
	public IssueFilterResult loadDataFromNetwork() throws Exception {
		return getService().issues(accountname, slug, limit, start, search, status, kind, filter).loadDataFromNetwork();
	}

	@Override
	public String getCacheKey() {
		return "issues" + String.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return 15 * DurationInMillis.ONE_MINUTE;
	}

	public int getStart() {
		return start;
	}

	public int getLimit() {
		return limit;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(accountname, slug, start, limit, search, status, kind, filter);
	}

	private abstract static class IssueFilter {
		List<String> filter = new ArrayList<>();

		IssueFilter() {
		}

		abstract String getParameterName();

		public void add(String filter) {
			this.filter.add(filter);
		}

		List<String> getQueries() {
			return filter;
		}
	}

	public static class IssueStatusFilter extends IssueFilter {

		public IssueStatusFilter(String ... s) {
			super();
			if (s != null) {
				filter = Arrays.asList(s);
			} else {
				filter = Collections.emptyList();
			}
		}

		public IssueStatusFilter(List<String> s) {
			super();
			filter = s;
		}

		@Override
		String getParameterName() {
			return "status";
		}
	}

	public static class IssueKindFilter extends IssueFilter {

		public IssueKindFilter(List<String> s) {
			super();
			filter = s;
		}

		@Override
		String getParameterName() {
			return "kind";
		}
	}

	/**
	 * Build a new {@link IssuesRequest}.
	 */
	public static class Builder {
		private String accountName;
		private String slug;
		private Integer limit;
		private Integer start;
		private String search;
		private List<String> status;
		private List<String> kind;
		private Map<String, String> filter;

		public Builder() {
			status = new ArrayList<>();
			kind = new ArrayList<>();
			filter = new HashMap<>();
		}

		public Builder setAccountName(String accountName) {
			if (accountName == null) {
				throw new NullPointerException("accountName may not be null");
			}
			this.accountName = accountName;
			return this;
		}

		public Builder setRepoSlug(String slug) {
			if (slug == null) {
				throw new NullPointerException("slug may not be null");
			}
			this.slug = slug;
			return this;
		}

		public Builder setLimit(Integer limit) {
			this.limit = limit;
			return this;
		}

		public Builder setStart(Integer start) {
			this.start = start;
			return this;
		}

		public Builder setSearch(String search) {
			this.search = search;
			return this;
		}

		public Builder addFilter(IssueFilter issueFilter) {
			if (issueFilter instanceof IssueStatusFilter) {
				status.addAll(issueFilter.getQueries());
			} else if (issueFilter instanceof IssueKindFilter) {
				kind.addAll(issueFilter.getQueries());
			} else {
				throw new UnsupportedOperationException("Unsupported issueFilter: "
						+ issueFilter.getClass().getSimpleName());
			}
			return this;
		}

		public Builder addFilter(@IssueContainer.Type String key, String value) {
			filter.put(key, value);
			return this;
		}

		public IssuesRequest build() {
			return new IssuesRequest(this);
		}
	}
}
