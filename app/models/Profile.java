package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Profile extends Model{

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    @Id
    private Long profileId;
    private String name;
    private String middleName;
    private String lastName;
    private Date birthdate;
    private String address;

    public Profile(User creator, Date creation, Date removal, String lang, Long profileId, String name, String middleName, String lastName, Date birthdate, String address) {
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.profileId = profileId;
        this.name = name;
        this.middleName = middleName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.address = address;
    }

    public static Model.Finder<Long, Profile> find = new Model.Finder<Long, Profile>(
            Long.class, Profile.class);

    public static Profile read(Long profileId) {
        return find.ref(profileId);
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }

    public Date getRemoval() {
        return removal;
    }

    public void setRemoval(Date removal) {
        this.removal = removal;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
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
