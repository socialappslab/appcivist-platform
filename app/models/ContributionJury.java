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

	@Column(name = "username")
	private String username;


	public static Finder<Long, ContributionJury> find = new Finder<>(ContributionJury.class);

	public ContributionJury() {
		super();
	}

	public ContributionJury(String username, Contribution contribution) {
		this.contribution = contribution;
		this.username = username;
	}

	public static boolean isContributionAndUser(Contribution contribution, User user) {
		return find.where().eq("contribution", contribution)
				.eq("jury", user).findUnique() != null ||
				isContributionAndUsername(contribution, user.getUsername());
	}

	public static boolean isContributionAndUsername(Contribution contribution, String username) {
		if (username.contains("@")) {
			username = username.substring(0, username.indexOf("@"));
		}
		return !(find.where().eq("contribution", contribution)
				.ilike("username", "%"+username+"%").findList().isEmpty());
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

