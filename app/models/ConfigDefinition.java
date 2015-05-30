package models;


import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

@Entity
public class ConfigDefinition extends AppCivistBaseModel {


    /**
	 * 
	 */
	private static final long serialVersionUID = -5550514904277749816L;
	
	@Id
    @GeneratedValue
    private Long configDefinitionId;
    private String key;
    private String valueType;
    private String description;
    private String category;
    
    public ConfigDefinition(String key, String valueType, String description, String category) {
    	super();
    	this.key = key;
        this.valueType = valueType;
        this.description = description;
        this.category = category;
    }
    
    public ConfigDefinition(){
        super();
    }

    public static Model.Finder<Long, ConfigDefinition> find = new Model.Finder<Long, ConfigDefinition>(
            Long.class, ConfigDefinition.class);

    public Long getConfigDefinitionId() {
		return configDefinitionId;
	}

	public void setConfigDefinitionId(Long configDefinitionId) {
		this.configDefinitionId = configDefinitionId;
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
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public static ConfigDefinition read(Long configId) {
        return find.ref(configId);
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

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }
}
