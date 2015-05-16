package models.Location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Properties extends Model{

    @Id
    private Long propertiesId;
    private String key;
    private String value;

    @JsonIgnore
    @ManyToOne
    private Geo geo;

    public Geo getGeo() {
        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    public Long getPropertiesId() {

        return propertiesId;
    }

    public void setPropertiesId(Long propertiesId) {
        this.propertiesId = propertiesId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}