package models;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import play.db.ebean.Model;
import enums.CampaignTypesEnum;

@Entity
public class CampaignType extends AppCivistBaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8987815561364000335L;

	@Id
	@GeneratedValue
	private Long campaignTypeId;
	
	private CampaignTypesEnum nameKey;
	private String name;
	
	@ManyToMany(cascade=CascadeType.ALL)
	private List<PhaseDefinition> defaultPhases = new LinkedList<PhaseDefinition>();
	
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Model.Finder<Long, CampaignType> find = new Model.Finder<Long, CampaignType>(
			Long.class, CampaignType.class);

	public CampaignType() {
		super();
	}

	public CampaignType(CampaignTypesEnum name, List<PhaseDefinition> defaultPhases) {
		super();
		this.nameKey = name;
		this.defaultPhases = defaultPhases;
	}

	public CampaignTypesEnum getNameKey() {
		return nameKey;
	}

	public void setNameKey(CampaignTypesEnum name) {
		this.nameKey = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String niceName) {
		this.name = niceName;
	}

	public List<PhaseDefinition> getDefaultPhases() {
		return defaultPhases;
	}

	public void setDefaultPhases(List<PhaseDefinition> defaultPhases) {
		this.defaultPhases = defaultPhases;
	}
	
	/*
	 * Basic Data operations
	 */
	
	public static CampaignType read(Long id) {
        return find.ref(id);
    }

    public static List<CampaignType> findAll() {
        return find.all();
    }

    public static CampaignType create(CampaignType object) {
        object.save();
        object.refresh();
        return object;
    }

    public static CampaignType createObject(CampaignType object) {
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
