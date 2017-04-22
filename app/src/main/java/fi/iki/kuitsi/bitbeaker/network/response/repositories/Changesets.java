package fi.iki.kuitsi.bitbeaker.network.response.repositories;

import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;

/**
 * Response of GET a list of changesets
 * using `changesets` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/changesets+Resource#changesetsResource-GETalistofchangesets">changesets Resource - Bitbucket - Atlassian Documentation</a>
 */
public class Changesets {
	public int count;
	/**
	 * List of {@link Changeset}s.
	 */
	public Changeset.List changesets;
}
