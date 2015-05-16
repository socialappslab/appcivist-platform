package models.Location;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Geo extends Model {

    @Id
    private Long locationId;
    private String type = "Feature";

    @OneToMany(cascade = CascadeType.ALL, mappedBy="geo")
    private List<Geometry> geometries = new ArrayList<Geometry>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy="geo")
    private List<Properties> propertieses = new ArrayList<Properties>();

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Geometry> getGeometries() {
        return geometries;
    }

    public void setGeometries(List<Geometry> geometries) {
        this.geometries = geometries;
    }

    public List<Properties> getPropertieses() {
        return propertieses;
    }

    public void setPropertieses(List<Properties> propertieses) {
        this.propertieses = propertieses;
    }
}
