package models;

import io.swagger.annotations.ApiModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@ApiModel(value="Log", description="Log record for registering events in the system")
public class Log extends Model {
	@Id
    @GeneratedValue
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)	
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss a z")
    private Date time = Calendar.getInstance().getTime();
    @Column(name="user_id")
    private String user;
    private String path;	
    private String action;
    private String resourceType;
    private String resourceUuid;
    
    public static Finder<Long,Log> find = new Finder<>(Log.class);
    
    public static void create(Log log){
        log.save();
    }
    
    public static Log createObject(Log log){
        log.save();
        return log;
    }
    
    public static void delete(Long id){
        find.ref(id).delete();
    }
    
    public static Log read(Long id){
        return find.byId(id);
    }
    
    public static List<Log> readByUser(String user) {
    	return find.where().eq("user", user).findList();
    }

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceUuid() {
		return resourceUuid;
	}

	public void setResourceUuid(String resourceUUID) {
		this.resourceUuid = resourceUUID;
	}
}