package fi.iki.kuitsi.bitbeaker.network.response.repositories;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;

/**
 * Response of GET a list of tags using `tags` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/repository+Resource+1.0#repositoryResource1.0-GETalistofthetags">repository Resource 1.0 - Bitbucket - Atlassian Documentation</a>
 */
public final class Tags extends TreeMap<String, Changeset> {

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Tags{");
		for (Entry<String, Changeset> entry : entrySet()) {
			sb.append(entry.getValue().getRawNode());
			sb.append("->");
			sb.append(entry.getKey());
			sb.append(',');
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * List tag names for specific changeset.
	 *
	 * @param rawNode Full commit hash of a changeset
	 * @return List of tag names
	 */
	@NonNull
	public ArrayList<String> getTagsForChangeset(String rawNode) {
		ArrayList<String> tagList = new ArrayList<>();
		if (rawNode != null) {
			for (Entry<String, Changeset> entry : entrySet()) {
				if (rawNode.equals(entry.getValue().getRawNode())) {
					tagList.add(entry.getKey());
				}
			}
		}
		Collections.sort(tagList, String.CASE_INSENSITIVE_ORDER);
		return tagList;
	}
}
