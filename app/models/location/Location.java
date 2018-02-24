package models.location;

import java.util.List;
import java.util.Optional;

import javax.persistence.*;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import delegates.NotificationsDelegate;
import enums.NotificationEventName;
import enums.ResourceSpaceTypes;
import models.AppCivistBaseModel;
import models.WorkingGroup;
import models.misc.Views;
import play.Logger;
import play.Play;
import play.libs.F;
import utils.GlobalData;
import utils.services.MapBoxWrapper;

import com.avaje.ebean.Model.Finder;
import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import utils.services.NominatimWrapper;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class Location extends Model {

	@Id
	@GeneratedValue
	private Long locationId;
	@Column(name = "place_name", columnDefinition = "text")
	@JsonView(Views.Public.class)	
	private String placeName; // "1969 calle de alberto aguilera en la coruña"
	@JsonView(Views.Public.class)
	private String street; //: "1969 calle de alberto aguilera",
	@JsonView(Views.Public.class)
	private String city; //: "la coruña",
	@JsonView(Views.Public.class)
	private String state; //: "asturias",
	@JsonView(Views.Public.class)
	private String zip; //: "56298"
	@JsonView(Views.Public.class)
	private String country; //: "spain"
	@JsonIgnore
	@Index
	private String serializedLocation;

	@Column(columnDefinition="TEXT")
	@JsonView(Views.Public.class)
	private String geoJson;

	@Column
	@JsonView(Views.Public.class)
	private Integer bestCoordinates = 0;

	@JsonView(Views.Public.class)
	private String source;

	@JsonView(Views.Public.class)
	private Boolean markedForReview;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "working_group_location")
	@JsonIgnore
	public List<WorkingGroup> workingGroups;

	// Additional information from openstreetmap
	@Column(name="additional_info")
	public String additionInfo;

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

	@PostPersist
	public void postPersist() {
		this.serializedLocation = "";
		this.serializedLocation += this.placeName!=null && !this.placeName.isEmpty() ? this.placeName : "";
		this.serializedLocation += this.street!=null && !this.street.isEmpty() ? " " + this.street : "";
		this.serializedLocation += this.city!=null && !this.city.isEmpty() ? " " + this.city : "";
		this.serializedLocation += this.state!=null && !this.state.isEmpty() ? " " + this.state : "";
		this.serializedLocation += this.zip!=null && !this.zip.isEmpty() ? " " + this.zip: "";
		this.serializedLocation += this.country!=null && !this.country.isEmpty() ? " " + this.country : "";
		String query = this.serializedLocation;
		// only store geoJson if geocoding service is nominatim
		String geocodingService = Play.application().configuration().getString(GlobalData.GEOCODING_SERVICE);
        F.Promise.promise(() -> {
            if ((this.geoJson == null || this.geoJson.isEmpty()) && geocodingService.equals("nominatim")) {
                if (this.getPlaceName() != null) {
                    JsonNode resultLocation = NominatimWrapper.geoCode(this.getPlaceName());

                    ArrayNode geojsonArr;
                    ArrayNode additionalInfoArr;

                    if (resultLocation.isArray()) {
                        // split additional info and geojson for each result
                        ArrayNode arr = (ArrayNode) resultLocation;
                        geojsonArr = new ObjectMapper().createArrayNode();
                        additionalInfoArr = new ObjectMapper().createArrayNode();
                        for (int a = 0; a < arr.size(); a++) {
                            JsonNode json = arr.get(a);
                            geojsonArr.add(json.get("geojson"));
                            createAdditionalInfo(additionalInfoArr, json);
                        }

                    } else {
                        // split additional info and geojson
                        geojsonArr = new ObjectMapper().createArrayNode();
                        additionalInfoArr = new ObjectMapper().createArrayNode();
                        JsonNode json = resultLocation;

                        createAdditionalInfo(additionalInfoArr, json);
                    }
                    this.setGeoJson(geojsonArr.toString());
                    this.setAdditionInfo(additionalInfoArr.toString());
                    this.update();
                } else {                 
                  this.geoJson = NominatimWrapper.geoCode(query).toString();
                }
            }
            return Optional.ofNullable(null);
        });
	}

	public static void createAdditionalInfo(ArrayNode additionalInfoArr, JsonNode json) {
		ObjectNode additionalInfo = new ObjectMapper().createObjectNode();
		additionalInfo.put("place_id", jsonNodeAsText(json,"place_id"));
		additionalInfo.put("licence", jsonNodeAsText(json,"licence"));
		additionalInfo.put("osm_type", jsonNodeAsText(json,"osm_type"));
		additionalInfo.put("osm_id", jsonNodeAsText(json,"osm_id"));
		additionalInfo.put("boundingbox", jsonNodeAsText(json,"boundingbox"));
		additionalInfo.put("lat", jsonNodeAsText(json,"lat"));
		additionalInfo.put("lon", jsonNodeAsText(json,"lon"));
		additionalInfo.put("display_name", jsonNodeAsText(json,"display_name"));
		additionalInfo.put("class", jsonNodeAsText(json,"class"));
		additionalInfo.put("type", jsonNodeAsText(json,"type"));
		additionalInfo.put("importance", jsonNodeAsDouble(json,"importance"));
		additionalInfo.put("icon", jsonNodeAsText(json,"icon"));
		additionalInfoArr.add(additionalInfo);
	}

	private static String jsonNodeAsText(JsonNode json, String attribute) {
		JsonNode node = json.get(attribute);
		return node != null ? node.asText() : "";
	}

	private static Double jsonNodeAsDouble(JsonNode json, String attribute) {
		JsonNode node = json.get(attribute);
		return node != null ? node.asDouble() : null;
	}

	public static List<Location> findByQuery(String query) {
		return find.where().ilike("serializedLocation", query).findList();
	}

	public static List<Location> findMarkedForReview() {
		return find.where().eq("markedForReview", true).findList();
	}

	public List<WorkingGroup> getWorkingGroups() {
		return workingGroups;
	}

	public void setWorkingGroups(List<WorkingGroup> workingGroups) {
		this.workingGroups = workingGroups;
	}

	public String getAdditionInfo() {
		return additionInfo;
	}

	public void setAdditionInfo(String additionInfo) {
		this.additionInfo = additionInfo;
	}

	public Integer getBestCoordinates() {
		return bestCoordinates;
	}

	public void setBestCoordinates(Integer bestCoordinates) {
		this.bestCoordinates = bestCoordinates;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Boolean getMarkedForReview() {
		return markedForReview;
	}

	public void setMarkedForReview(Boolean markForReview) {
		this.markedForReview = markForReview;
	}
}
