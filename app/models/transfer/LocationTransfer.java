package models.transfer;

public class LocationTransfer {
	private String street; //: "1969 calle de alberto aguilera",
	private String city; //: "la coruña",
	private String state; //: "asturias",
	private String zip; //: "56298"
	private String country; //: "spain"
	private String geoJson;
	
	public LocationTransfer() {
		super();
	}
	
	public LocationTransfer(String street, String city, String state,
			String zip, String country) {
		super();
		this.street = street;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.country = country;
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

	public String getGeoJson() {
		return geoJson;
	}

	public void setGeoJson(String geoJson) {
		this.geoJson = geoJson;
	}	
}
