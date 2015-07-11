package models;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.annotation.JsonBackReference;

import enums.ConfigTargets;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import java.util.List;

@Entity
public class Config extends AppCivistBaseModel {

	@Id
    @GeneratedValue
    private Long configId;
    private String key;
    private String value;
    private ConfigTargets configTarget;
    private Long targetId;
    
    @OneToOne
    private ConfigDefinition definition;

    /* Relatiohships
     * 
     */
    @ManyToOne
	@JsonBackReference
	private Assembly assembly;
   
    public Config(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public Config(String key, String value, ConfigDefinition def) {
        this.key = key;
        this.value = value;
        this.definition = def;
    }
    
    
    public Config(){
        super();
    }

    public static Finder<Long, Config> find = new Finder<Long, Config>(
            Long.class, Config.class);

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
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
    
    public ConfigTargets getConfigTarget() {
		return configTarget;
	}

	public void setConfigTarget(ConfigTargets configTarget) {
		this.configTarget = configTarget;
	}

	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	public ConfigDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(ConfigDefinition definition) {
		this.definition = definition;
	}

	public Assembly getAssembly() {
		return assembly;
	}

	public void setAssembly(Assembly assembly) {
		this.assembly = assembly;
	}

	/*
	 * Basic Queries
	 */
    public static List<Config> findAll() {
        return find.all();
    }
    
    public static List<Config> findByAssembly(Long aid) {
        return find.where().eq("assembly.assemblyID", aid).findList();
    }
    
	public static Config read(Long configId) {
        return find.ref(configId);
    }

	public static Config read(Long aid, Long configId) {
        return find.where().eq("assembly.assemblyID", aid).eq("configId",configId).findUnique();
    }
	
    public static Integer readByKey(String key) {
        ExpressionList<Config> configs = find.where().eq("key", key);
        return configs.findList().size();
    }
    
    public static Config create(Config config) {
        config.save();
        config.refresh();
        return config;
    }

    public static Config createObject(Config config) {
        config.save();
        return config;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void delete(Long aid, Long id) {
        find.where().eq("assembly.assemblyID", aid).eq("configId", id).findUnique().delete();
    }

    public static Config update(Config config) {
    	config.update();
    	config.refresh();
    	return config;
    }
}
