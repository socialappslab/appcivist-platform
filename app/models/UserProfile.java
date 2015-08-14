package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import java.util.Date;
import java.util.UUID;

@Entity
public class UserProfile extends AppCivistBaseModel {
	@Id
	@GeneratedValue
	private Long profileId;
	private UUID uuid;
	private String name;
	private String middleName;
	private String lastName;
	private Date birthdate;
	private String address;
	// TODO add contact information
	@OneToOne
	private User user;

	public UserProfile(User creator, String name, String middleName, String lastName,
			Date birthdate, String address) {
		this.user = creator;
		this.name = name;
		this.middleName = middleName;
		this.lastName = lastName;
		this.birthdate = birthdate;
		this.address = address;
	}

	public static Finder<Long, UserProfile> find = new Finder<>(UserProfile.class);

	public static UserProfile read(Long profileId) {
		return find.ref(profileId);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User owner) {
		this.user = owner;
		this.uuid = owner.getUuid();
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
	
	/*
	 * Other queries
	 */
	
	public static UserProfile readByUserId(Long id) {
		return find.where().eq("user.userId", id).findUnique();
	}
}
