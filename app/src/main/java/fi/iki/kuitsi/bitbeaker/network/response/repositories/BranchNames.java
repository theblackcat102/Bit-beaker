package fi.iki.kuitsi.bitbeaker.network.response.repositories;

import com.google.gson.annotations.JsonAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Stores the a list of branches associated with a repository. Branch names extracted from the
 * response by {@link BranchNamesJsonAdapter}.
 * <p/>
 * {@code List<String>} delegator.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/repository+Resource#repositoryResource-GETlistofbranches">repository Resource - Bitbucket - Atlassian Documentation</a>
 */
@JsonAdapter(BranchNamesJsonAdapter.class)
public class BranchNames implements Iterable<String> {

	/**
	 * List of branch names, the "delegate".
	 */
	private final List<String> names;

	/**
	 * Constructs an empty branch name list.
	 */
	public BranchNames() {
		names = new ArrayList<>();
	}

	/**
	 * Construct an branch name list from the specified array.
	 */
	public BranchNames(String... branches) {
		names = Arrays.asList(branches);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BranchNames)) return false;

		BranchNames that = (BranchNames) o;

		return names.equals(that.names);
	}

	@Override
	public int hashCode() {
		return names.hashCode();
	}

	@Override
	public String toString() {
		return "BranchNames { " + names.toString() + " }";
	}

	/**
	 * Appends the specified name to the end of this list.
	 *
	 * @param name Name to be appended to this list
	 */
	public void add(String name) {
		names.add(name);
	}

	/**
	 * Returns the number of elements in this list.
	 *
	 * @return The number of elements in this list
	 */
	public int size() {
		return names.size();
	}

	/**
	 * Returns the element at the specified position in this list.
	 *
	 * @param location Index of the element to return
	 * @return The element at the specified position in this list
	 */
	public String get(int location) {
		return names.get(location);
	}

	/**
	 * Returns a string array containing all branch names contained in this {@code BranchNames}.
	 * @return a string array of the branch names from this {@code BranchNames}.
	 */
	public String[] toArray() {
		String[] result = new String[size()];
		return toArray(result);
	}

	/**
	 * Returns a string array containing all branch names contained in this {@code BranchNames}.
	 * If the specified array is large enough to hold the names, the specified array is used,
	 * otherwise an array of the same type is created.
	 *
	 * @param array the string array.
	 * @return a string array of the branch names from this {@code BranchNames}.
	 */
	public String[] toArray(String[] array) {
		names.toArray(array);
		return array;
	}

	/**
	 * Returns an iterator over the branch names.
	 *
	 * @return An iterator.
	 */
	@Override
	public Iterator<String> iterator() {
		return names.iterator();
	}
}
