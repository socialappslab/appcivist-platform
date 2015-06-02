package models.services;

import java.util.List;

import play.db.ebean.*;

public class ServiceIssueCollection extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2917884641256561757L;
	private List<ServiceIssue> issues;

	public List<ServiceIssue> getIssues() {
		return issues;
	}

	public void setIssues(List<ServiceIssue> issues) {
		this.issues = issues;
	}
}
