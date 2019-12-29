package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;

import javax.persistence.*;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="ContributionJury", description="Jury associated to a contribution")
public class ContributionJury extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name="contribution_id")
	private Contribution contribution;

	@ManyToOne
	@JoinColumn(name="user_id")
	private User jury;


	public static Finder<Long, ContributionJury> find = new Finder<>(ContributionJury.class);

	public ContributionJury() {
		super();
	}

	public static boolean isContributionAndUser(Contribution contribution, User user) {
		return find.where().eq("contribution", contribution)
				.eq("jury", user).findUnique() != null;
	}


	public Long getId() {
		return id;
	}

	public Contribution getContribution() {
		return contribution;
	}

	public User getJury() {
		return jury;
	}
}

