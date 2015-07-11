package models.location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.GeoTypes;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Geometry extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2184427152141444791L;
	@Id
    private Long geometryId;
    private GeoTypes type = GeoTypes.Point;
    private String coordinates;

    @JsonIgnore
    @ManyToOne()
    private Geo geo;

    public Long getGeometryId() {
        return geometryId;
    }

    public void setGeometryId(Long geometryId) {
        this.geometryId = geometryId;
    }

    public GeoTypes getType() {
        return type;
    }

    public void setType(GeoTypes type) {
        this.type = type;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public Geo getGeo() {
        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }
}
