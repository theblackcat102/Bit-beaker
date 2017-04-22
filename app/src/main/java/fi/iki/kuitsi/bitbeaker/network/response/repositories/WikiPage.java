package fi.iki.kuitsi.bitbeaker.network.response.repositories;

/**
 * Response of GET the raw content of a Wiki page
 * using `wiki` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/wiki+Resources#wikiResources-GETtherawcontentofaWikipage">wiki Resources - Bitbucket - Atlassian Documentation</a>
 */
public class WikiPage {
	public String data;
	public String markup;
	public String rev;
}
