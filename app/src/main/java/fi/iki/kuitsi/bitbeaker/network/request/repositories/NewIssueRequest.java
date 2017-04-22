package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates a new issue in a repository using `issues` resource of `repositories` endpoint.
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/issues+Resource#issuesResource-POSTanewissue">issues Resource - Bitbucket - Atlassian Documentation</a>
 */
public class NewIssueRequest extends BaseRepositoriesRequest<Issue> {

	protected final Map<String, String> fields;

	protected NewIssueRequest(Init<?> init) {
		super(Issue.class, init.accountname, init.slug);
		this.fields = Collections.unmodifiableMap(new HashMap<>(init.fields));
	}

	@Override
	public Issue loadDataFromNetwork() throws Exception {
		return getService().newIssue(accountname, slug, fields).loadDataFromNetwork();
	}

	protected abstract static class Init<T extends Init<T>> {

		private static final String FIELD_TITLE = "title";
		private static final String FIELD_CONTENT = "content";
		private static final String FIELD_ASSIGNEE = "responsible";
		private static final String FIELD_KIND = "kind";
		private static final String FIELD_PRIORITY = "priority";
		private static final String FIELD_STATUS = "status";
		private static final String FIELD_MILESTONE = "milestone";
		private static final String FIELD_VERSION = "version";
		private static final String FIELD_COMPONENT = "component";

		final String accountname;
		final String slug;
		final Map<String, String> fields;

		protected Init(String accountname, String slug) {
			this.accountname = accountname;
			this.slug = slug;
			this.fields = new HashMap<>();
		}

		protected abstract T self();

		/** Set issue title. <strong>Required.</strong> */
		public final T setTitle(String title) {
			addField(FIELD_TITLE, title);
			return self();
		}

		/** Set issue description. */
		public final T setContent(String content) {
			addField(FIELD_CONTENT, content);
			return self();
		}

		/** Set issue assignee. */
		public final T setAssignee(String assignee) {
			addField(FIELD_ASSIGNEE, assignee);
			return self();
		}

		/**
		 * Set issue type.
		 */
		public final T setKind(Issue.Kind kind) {
			addField(FIELD_KIND, kind.toString());
			return self();
		}

		/**
		 * Set issue priority.
		 */
		public final T setPriority(Issue.Priority priority) {
			addField(FIELD_PRIORITY, priority.toString());
			return self();
		}

		/**
		 * Set issue status.
		 */
		public final T setStatus(Issue.Status status) {
			addField(FIELD_STATUS, status.toString());
			return self();
		}

		/** Set milestone. */
		public final T setMilestone(String milestone) {
			addField(FIELD_MILESTONE, milestone);
			return self();
		}

		/** Set version. */
		public final T setVersion(String version) {
			addField(FIELD_VERSION, version);
			return self();
		}

		/** Set component. */
		public final T setComponent(String component) {
			addField(FIELD_COMPONENT, component);
			return self();
		}

		private void addField(String name, String value) {
			fields.put(name, value);
		}
	}

	public static final class Builder extends Init<Builder> {

		@Override
		protected Builder self() {
			return this;
		}

		public Builder(String accountname, String slug) {
			super(accountname, slug);
		}

		public NewIssueRequest build() {
			return new NewIssueRequest(this);
		}
	}
}
