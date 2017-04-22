package fi.iki.kuitsi.bitbeaker.viewmodel;

import fi.iki.kuitsi.bitbeaker.domainobjects.IssueContainer;

/**
 * Wrapper that delegates calls to an {@link IssueContainer}. Additionally it stores statistics
 * and provides progress information.
 */
public class IssueContainerStat {

	private final IssueContainer issueContainer;
	private int issueCount = -1;
	private int closedIssueCount = -1;

	private IssueContainerStat(IssueContainer issueContainer) {
		this.issueContainer = issueContainer;
	}

	public static IssueContainerStat wrap(IssueContainer issueContainer) {
		return new IssueContainerStat(issueContainer);
	}

	public String getName() {
		return issueContainer.getName();
	}
	
	public void issueCount(int issueCount) {
		this.issueCount = issueCount;
	}

	public void closedIssueCount(int closedIssueCount) {
		this.closedIssueCount = closedIssueCount;
	}

	public int getIssueCount() {
		return issueCount;
	}

	public int getClosedIssueCount() {
		return closedIssueCount;
	}

	public double getProgress() {
		if (getIssueCount() != 0) {
			return ((getClosedIssueCount() * 1.0) / (getIssueCount() * 1.0)) * 100.0;
		}
		return 100.0;
	}
}
