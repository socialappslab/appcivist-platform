package models;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.ConfigTargets;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Config extends AppCivistBaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3340337591058459420L;
	
	@Id
    @GeneratedValue
    private Long configId;
    private String key;
    private String value;
    private ConfigTargets configTarget;
    
    @OneToOne
    private ConfigDefinition definition;

    @JsonIgnore
    @ManyToOne
    private CampaignPhase campaignPhase;
    
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

    public static Model.Finder<Long, Config> find = new Model.Finder<Long, Config>(
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

	public ConfigDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(ConfigDefinition definition) {
		this.definition = definition;
	}

    public CampaignPhase getCampaignPhase() {
        return campaignPhase;
    }

    public void setCampaignPhase(CampaignPhase campaignPhase) {
        this.campaignPhase = campaignPhase;
    }

    public static Config read(Long configId) {
        return find.ref(configId);
    }

    public static Integer readByKey(String key) {
        ExpressionList<Config> configs = find.where().eq("key", key);
        return configs.findList().size();
    }


    public static List<Config> findAll() {
        return find.all();
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

    public static void update(Long id) {
        find.ref(id).update();
    }
}
