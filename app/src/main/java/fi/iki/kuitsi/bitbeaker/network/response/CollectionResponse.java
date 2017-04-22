package fi.iki.kuitsi.bitbeaker.network.response;

import java.util.ArrayList;

import fi.iki.kuitsi.bitbeaker.util.StringUtils;

/**
 * Wrapper structure for endpoints that return object collections and support pagination. (API 2.0)
 *
 * @param <T> Response object type
 */
public abstract class CollectionResponse<T> {
	/**
	 * Total number of objects in the response.
	 */
	private int size;
	/**
	 * Page number of the current results.
	 */
	private int page;
	/**
	 * Current number of objects on the existing page.
	 */
	private int pagesize;
	/**
	 * Link to the next page.
	 */
	private String next;
	/**
	 * The list of objects. This contains at most {@link #pagesize} objects.
	 */
	private ArrayList<T> values;

	public int getCurrentPage() {
		return page;
	}

	public String getNextPageLink() {
		return next;
	}

	public boolean hasNextPage() {
		return StringUtils.isNotBlank(next);
	}

	public ArrayList<T> getValues() {
		return values;
	}
}
