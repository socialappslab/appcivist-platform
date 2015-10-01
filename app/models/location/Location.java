package models.location;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;

import utils.services.MapBoxWrapper;

import com.avaje.ebean.Model.Finder;
import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Location {
	
	@Id
	@GeneratedValue
	private Long locationId;
	private String placeName; // "1969 calle de alberto aguilera en la coruña"
	private String street; //: "1969 calle de alberto aguilera",
	private String city; //: "la coruña",
	private String state; //: "asturias",
	private String zip; //: "56298"
	private String country; //: "spain"
	@JsonIgnore
	@Index
	private String serializedLocation;
	@Column(columnDefinition="TEXT")
	private String geoJson; 

	/**
	 * The find property is an static property that facilitates database query creation
	 */
    public static Finder<Long, Location> find = new Finder<>(Location.class);

	
	// TODO: find a way for knowing if part of the location was changed before updating
	// @Transient
	//	@JsonIgnore
	//	Location oldLocation;
	
	public Location() {
		super();
	}
	
	public Location(String street, String city, String state, String zip,
			String country) {
		super();
		this.street = street;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.country = country;
	}

	public Long getLocationId() {
		return locationId;
	}


	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}


	public String getStreet() {
		return street;
	}


	public void setStreet(String street) {
		this.street = street;
	}


	public String getCity() {
		return city;
	}


	public void setCity(String city) {
		this.city = city;
	}


	public String getState() {
		return state;
	}


	public void setState(String state) {
		this.state = state;
	}


	public String getZip() {
		return zip;
	}


	public void setZip(String zip) {
		this.zip = zip;
	}


	public String getCountry() {
		return country;
	}


	public void setCountry(String country) {
		this.country = country;
	}

	public String getPlaceName() {
		return placeName;
	}

	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}

	public String getGeoJson() {
		return geoJson;
	}

	public void setGeoJson(String geoJson) {
		this.geoJson = geoJson;
	}
	
//	@PostLoad
//	public void afterRetrievingFromDB() {
//		this.oldLocation = 
//	}
	
	@PrePersist
	public void beforePersist() {
		this.serializedLocation = "";
		this.serializedLocation += this.placeName!=null && !this.placeName.isEmpty() ? this.placeName : "";
		this.serializedLocation += this.street!=null && !this.street.isEmpty() ? " " + this.street : "";
		this.serializedLocation += this.city!=null && !this.city.isEmpty() ? " " + this.city : "";
		this.serializedLocation += this.state!=null && !this.state.isEmpty() ? " " + this.state : "";
		this.serializedLocation += this.zip!=null && !this.zip.isEmpty() ? " " + this.zip: "";
		this.serializedLocation += this.country!=null && !this.country.isEmpty() ? " " + this.country : "";
		if (this.geoJson == null || this.geoJson.isEmpty()) {
			String query = this.serializedLocation;
			this.geoJson = MapBoxWrapper.geoCode(query);
		}
	}
	
	public static List<Location> findByQuery(String query) {
		return find.where().ilike("serializedLocation", query).findList();
	}
	
}
