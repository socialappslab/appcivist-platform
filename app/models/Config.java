package models;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ConfigTargets;
import models.misc.Views;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="Config", description="Generic model to store a configuration value")
public class Config extends AppCivistBaseModel {

	@Id
    @JsonView(Views.Public.class)
	private UUID uuid = UUID.randomUUID();
    @JsonView(Views.Public.class)
    private String key;
	@Column(name="value", columnDefinition="text")
    @JsonView(Views.Public.class)
    private String value;
    @JsonView(Views.Public.class)
    @Enumerated(EnumType.STRING)
    private ConfigTargets configTarget;
    @JsonView(Views.Public.class)
    @Column(name="target_uuid")
    private UUID targetUuid;
    @Transient
    private String targetUuidAsString;
    
    @ManyToOne(cascade=CascadeType.ALL)
    private ConfigDefinition definition;
   
    public Config(String key, String value) {
    	this.uuid = UUID.randomUUID();
        this.key = key;
        this.value = value;
    }
    
    public Config(String key, String value, ConfigTargets target, String targetUuidAsString) {
    	this.uuid = UUID.randomUUID();
    	this.targetUuidAsString = targetUuidAsString;
    	this.targetUuid = UUID.fromString(targetUuidAsString);
        this.key = key;
        this.value = value;
        this.configTarget = target;
    }
    
    public Config(String key, String value, ConfigDefinition def) {
    	this.uuid = UUID.randomUUID();
        this.key = key;
        this.value = value;
        this.definition = def;
    }
    
    public Config(){
        super();	
        this.uuid = UUID.randomUUID();
    }

    public static Finder<UUID, Config> find = new Finder<>(Config.class);

    public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
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

	public UUID getTargetUuid() {
		return targetUuid;
	}

	public void setTargetUuid(UUID targetUUID) {
		this.targetUuid = targetUUID;
	}

	public String getTargetUuidAsString() {
		return uuid.toString();
	}

	public void setTargetUuidAsString(String targetUuidAsString) {
		this.targetUuidAsString = targetUuidAsString;
		this.targetUuid = UUID.fromString(targetUuidAsString);
	}

	public ConfigDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(ConfigDefinition definition) {
		this.definition = definition;
	}

	/*
	 * Basic Queries
	 */
    public static List<Config> findAll() {
        return find.all();
    }
    
    public static List<Config> findByAssembly(UUID assemblyUUID) {
        return find.where().eq("targetUuid", assemblyUUID)
        		.eq("configTarget", ConfigTargets.ASSEMBLY)
        		.findList();
    }
    
	public static Config read(UUID configId) {
        return find.ref(configId);
    }

	public static Config read(UUID targetUuid, UUID uuid) {
        return find.where().eq("targetUuid", targetUuid).eq("configId",uuid).findUnique();
    }
	
    public static Integer readByKey(String key) {
        ExpressionList<Config> configs = find.where().eq("key", key);
        return configs.findList().size();
    }
    
    public static Config create(Config config) {
    	// check if there is a definition associated with thie configuration value, if not add it
		if (config.getDefinition()==null) {
			ConfigDefinition cd = ConfigDefinition.findByKey(config.getKey());
			if(cd != null) {
				config.setDefinition(cd);
			} else {
				return null;
			}
		}
        config.save();
        config.refresh();
        return config;
    }

    public static Config createObject(Config config) {
        config.save();
        return config;
    }

    public static void delete(UUID id) {
        find.ref(id).delete();
    }

    public static void delete(UUID targetUuid, UUID uuid) {
        find.where().eq("targetUuid", targetUuid).eq("uuid", uuid).findUnique().delete();
    }

    public static Config update(Config config) {
    	config.update();
    	config.refresh();
    	return config;
    }
}
