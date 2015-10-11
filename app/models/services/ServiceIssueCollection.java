package models.services;

import java.util.List;
import com.avaje.ebean.Model;

public class ServiceIssueCollection extends Model {
	private List<ServiceIssue> issues;

	public List<ServiceIssue> getIssues() {
		return issues;
	}

	public void setIssues(List<ServiceIssue> issues) {
		this.issues = issues;
	}
}
