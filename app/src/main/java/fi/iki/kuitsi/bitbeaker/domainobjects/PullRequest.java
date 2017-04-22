package fi.iki.kuitsi.bitbeaker.domainobjects;

/**
 * Represents a pull request.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/pullrequests+Resource>
 * pullrequests Resource - Bitbucket - Atlassian Documentation</a>
 */
public class PullRequest {

	public enum State {
		OPEN, MERGED, DECLINED
	}

	/**
	 * The pull request's unique ID.
	 * Note that pull request IDs are only unique within their associated repository.
	 */
	private int id;
	/**
	 * Title of the pull request.
	 */
	private String title;
	/**
	 * Description field for the request.
	 */
	private String description;
	/**
	 * The pull request's current status.
	 */
	private State state;

	/**
	 * Return the pull request's unique ID.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Return the pull request's title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Return the description of the pull request.
	 */
	public String getDescription() {
		return description;
	}
}
