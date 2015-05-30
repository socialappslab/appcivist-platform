package models;

import java.util.List;

import play.db.ebean.Model;

public class ContributionCollection extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2917884641256561757L;
	private List<Contribution> contributions;

	public List<Contribution> getContributions() {
		return contributions;
	}

	public void setContributions(List<Contribution> contributions) {
		this.contributions = contributions;
	}
}
