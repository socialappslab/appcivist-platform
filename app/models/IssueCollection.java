package models;

import java.util.List;

import play.db.ebean.*;

public class IssueCollection extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2917884641256561757L;
	private List<Issue> issues;

	public List<Issue> getIssues() {
		return issues;
	}

	public void setIssues(List<Issue> issues) {
		this.issues = issues;
	}
}
