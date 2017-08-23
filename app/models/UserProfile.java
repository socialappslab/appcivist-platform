package models;

import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import models.location.Geometry;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="UserProfile", description="Profile of Users")
public class UserProfile extends AppCivistBaseModel {
	@Id
	@GeneratedValue
	private Long profileId;
	@JsonIgnore
	private UUID uuid;
	@Transient 
    private String uuidAsString;
	private String name;
	private String middleName;
	private String lastName;
	private Date birthdate;
	@Column(name="address", columnDefinition="text")
	private String address;
	@Column(name="note", columnDefinition="text")
	private String note;
	@Column(name="phone")
	private String phone;
	@Column(name="gender")
	private String gender;


	// TODO add contact information
	@JsonIgnore
	@OneToOne
	private User user;

	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private List<Config> configs;

	public UserProfile(User creator, UUID uuid, String name, String middleName, String lastName,
					   Date birthdate, String address) {
		this.uuid = uuid;
		this.user = creator;
		this.name = name;
		this.middleName = middleName;
		this.lastName = lastName;
		this.birthdate = birthdate;
		this.address = address;
	}

	public List<Config> getConfigs() {
		return configs;
	}

	public void setConfigs(List<Config> configs) {
		this.configs = configs;
	}

	public static Finder<Long, UserProfile> getFind() {
		return find;
	}

	public static void setFind(Finder<Long, UserProfile> find) {
		UserProfile.find = find;
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
		this.setUuid(owner.getUuid());
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
	
	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getUuidAsString() {
		return uuid.toString();
	}

	public void setUuidAsString(String uuidAsString) {
		this.uuid = UUID.fromString(uuidAsString);
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public static UserProfile readByUserId(Long id) {
		return find.where().eq("user.userId", id).findUnique();
	}
}
