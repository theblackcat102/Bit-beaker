package fi.iki.kuitsi.bitbeaker.comparators;

import java.util.Comparator;

import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;

public class RepositoryRelevanceComparator implements Comparator<Repository> {
	@Override
	public int compare(Repository a, Repository b) {
		if (a.isFork() == b.isFork()) {
			return -a.getLastUpdated().compareTo(b.getLastUpdated());
		}
		return a.isFork() ? 1 : -1;
	}
}
