package models.Location;

import play.db.ebean.Model;

import javax.persistence.*;

import models.AppCivistBaseModel;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Geo extends AppCivistBaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4612546771988078421L;
	@Id
    @GeneratedValue
    private Long locationId;
    private String type = "Feature";

    @OneToMany(cascade = CascadeType.ALL, mappedBy="geo")
    private List<Geometry> geometries = new ArrayList<Geometry>();

    // Properties store human readable properties of the location like
    // place name, textual address, city, etc. 
    @OneToMany(cascade = CascadeType.ALL, mappedBy="geo")
    private List<Properties> properties = new ArrayList<Properties>();

	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Model.Finder<Long, Geo> find = new Model.Finder<Long, Geo>(
			Long.class, Geo.class);
    
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

    public List<Properties> getProperties() {
        return properties;
    }

    public void setProperties(List<Properties> propertieses) {
        this.properties = propertieses;
    }

	/*
	 * Basic Data operations
	 */
	
	public static Geo read(Long id) {
        return find.ref(id);
    }

    public static List<Geo> findAll() {
        return find.all();
    }

    public static Geo create(Geo object) {
        object.save();
        object.refresh();
        return object;
    }

    public static Geo createObject(Geo object) {
        object.save();
        return object;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }
}
