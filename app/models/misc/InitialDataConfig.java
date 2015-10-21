package models.misc;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.avaje.ebean.Model;

@Entity
public class InitialDataConfig extends Model {
	@Id
	@GeneratedValue
	private Long dataFileId;
	private String dataFile;
	private Boolean loaded = true;

	public static Finder<Long, InitialDataConfig> find = new Finder<Long, InitialDataConfig>(InitialDataConfig.class);

	public InitialDataConfig(String dataFile, Boolean loaded) {
		super();
		this.dataFile = dataFile;
		this.loaded = loaded;
	}
	
	public Long getDataFileId() {
		return dataFileId;
	}

	public void setDataFileId(Long dataFileId) {
		this.dataFileId = dataFileId;
	}

	public String getDataFile() {
		return dataFile;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	public Boolean getLoaded() {
		return loaded;
	}

	public void setLoaded(Boolean loaded) {
		this.loaded = loaded;
	}


	public static InitialDataConfig read(Long dataFileId) {
		return find.ref(dataFileId);
	}

    public static List<InitialDataConfig> findAll() {
        return find.all();
    }

    public static InitialDataConfig create(InitialDataConfig config) {
        config.save();
        config.refresh();
        return config;
    }

    public static InitialDataConfig createObject(InitialDataConfig config) {
        config.save();
        return config;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }
    
    public static InitialDataConfig readByFileName(String dataFile) {
		return find.where().eq("dataFile", dataFile).findUnique();
	}
}
