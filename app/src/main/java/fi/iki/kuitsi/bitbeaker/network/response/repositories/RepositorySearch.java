package fi.iki.kuitsi.bitbeaker.network.response.repositories;

import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;

public class RepositorySearch {
	Integer count;
	String query;
	Repository.List repositories;

	public Repository.List getRepositories() {
		return repositories;
	}
}
