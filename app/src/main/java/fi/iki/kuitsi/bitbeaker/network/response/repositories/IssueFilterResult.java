package fi.iki.kuitsi.bitbeaker.network.response.repositories;

import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;

public class IssueFilterResult {

	private int count;
	private String search;
	private Issue.List issues;

	public int getCount() {
		return count;
	}

	public String getSearch() {
		return search;
	}

	public Issue.List getIssues() {
		return issues;
	}


}
