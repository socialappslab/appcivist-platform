package models;

import io.swagger.annotations.ApiModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

//import models.AppCivistBaseModel.ConfigOption;
import enums.ConfigTargets;

import java.util.List;
import java.util.UUID;

@Entity
//@UniqueConstraint(columnNames = { "key" })
@ApiModel(value="ConfigDefinition", description="Definition of a configuration value")
public class ConfigDefinition extends AppCivistBaseModel {
	
	@Id
    private UUID uuid;
	private String key;
    private String valueType;
	@Column(name="description", columnDefinition="text")
    private String description;
    private String defaultValue;
    private ConfigTargets configTarget = ConfigTargets.ASSEMBLY;
    
//    private String uiType; // type of element to use in the UI to render this configuration option
//    private List<ConfigOption> options; // list of possible values for this configuration
//    @Transient private ConfigOption optionValue; // currently selected option value
//	private String dependsOfKey;

    public ConfigDefinition(String key, String valueType, String description, String defaultValue, ConfigTargets configTarget) {
    	super();
    	this.uuid = UUID.randomUUID();
    	this.key = key;
        this.valueType = valueType;
        this.description = description;
        this.defaultValue = defaultValue;
        this.configTarget = configTarget;
    }
    
    public ConfigDefinition(){
        super();
    	this.uuid = UUID.randomUUID();
    }

    public static Finder<UUID, ConfigDefinition> find = new Finder<>(ConfigDefinition.class);

    public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID configDefinitionId) {
		this.uuid = configDefinitionId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
	/*
	 * Basic Data operations
	 */
	
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public ConfigTargets getConfigTarget() {
		return configTarget;
	}

	public void setConfigTarget(ConfigTargets configTarget) {
		this.configTarget = configTarget;
	}

//	public String getUiType() {
//		return uiType;
//	}
//
//	public void setUiType(String uiType) {
//		this.uiType = uiType;
//	}

	public static ConfigDefinition read(UUID uuid) {
        return find.ref(uuid);
    }

    public static List<ConfigDefinition> findAll() {
        return find.all();
    }

    public static ConfigDefinition create(ConfigDefinition config) {
        config.save();
        config.refresh();
        return config;
    }

    public static ConfigDefinition createObject(ConfigDefinition config) {
        config.save();
        return config;
    }

    public static void delete(UUID id) {
        find.ref(id).delete();
    }

    public static void update(UUID id) {
        find.ref(id).update();
    }

	public static ConfigDefinition findByKey(String key2) {
		return find.where().eq("key", key2).findUnique();
	}
}
