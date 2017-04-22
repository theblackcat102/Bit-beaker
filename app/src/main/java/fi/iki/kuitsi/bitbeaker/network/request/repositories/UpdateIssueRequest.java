package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;

/**
 * Updates an existing issue  using `issues` resource of `repositories` endpoint.
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/issues+Resource#issuesResource-Updateanexistingissue">issues Resource - Bitbucket - Atlassian Documentation</a>
 */
public final class UpdateIssueRequest extends NewIssueRequest {

	private final int issueId;

	UpdateIssueRequest(Init<?> init) {
		super(init);
		this.issueId = init.issueId;
	}

	@Override
	public Issue loadDataFromNetwork() throws Exception {
		return getService().updateIssue(accountname, slug, issueId, fields).loadDataFromNetwork();
	}

	protected abstract static class Init<T extends Init<T>> extends NewIssueRequest.Init<T> {

		final int issueId;

		protected Init(String accountname, String slug, int issueId) {
			super(accountname, slug);
			this.issueId = issueId;
		}

		public final UpdateIssueRequest build() {
			return new UpdateIssueRequest(this);
		}
	}

	public static final class Builder extends Init<Builder> {

		public Builder(String accountname, String slug, int issueId) {
			super(accountname, slug, issueId);
		}

		@Override
		protected Builder self() {
			return this;
		}
	}
}
