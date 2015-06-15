package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.util.Date;

@Entity
public class UserProfile extends AppCivistBaseModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3661472286635903816L;

	@Id
	@GeneratedValue
	private Long profileId;
	private String name;
	private String middleName;
	private String lastName;
	private Date birthdate;
	private String address;
	private User creator;

	public UserProfile(User creator, String name, String middleName, String lastName,
			Date birthdate, String address) {
		this.creator = creator;
		this.name = name;
		this.middleName = middleName;
		this.lastName = lastName;
		this.birthdate = birthdate;
		this.address = address;
	}

	public static Model.Finder<Long, UserProfile> find = new Model.Finder<Long, UserProfile>(
			Long.class, UserProfile.class);

	public static UserProfile read(Long profileId) {
		return find.ref(profileId);
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Long getProfileId() {
		return profileId;
	}

	public void setProfileId(Long profileId) {
		this.profileId = profileId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}
