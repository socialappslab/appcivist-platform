package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import play.db.ebean.Model;

@Entity
public class PhaseDefinition extends AppCivistBaseModel {

	private static final long serialVersionUID = -1114088974682512287L;

	@Id
	@GeneratedValue
	private Long phaseDefinitionId;
	
	private String name; 
	
	@OneToMany(mappedBy = "phaseDefinition", cascade=CascadeType.ALL)
	@JsonManagedReference
	private List<RequiredPhaseConfiguration> requiredConfigurations = new ArrayList<RequiredPhaseConfiguration>();
	
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Model.Finder<Long, PhaseDefinition> find = new Model.Finder<Long, PhaseDefinition>(
			Long.class, PhaseDefinition.class);

	public PhaseDefinition() {
		super();
	}
	
	public PhaseDefinition(String name) {
		super();
		this.name = name;
	}

	public Long getPhaseDefinitionId() {
		return phaseDefinitionId;
	}

	public void setPhaseDefinitionId(Long phaseDefinitionId) {
		this.phaseDefinitionId = phaseDefinitionId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<RequiredPhaseConfiguration> getRequiredConfigurations() {
		return requiredConfigurations;
	}

	public void setRequiredConfigurations(
			List<RequiredPhaseConfiguration> requiredConfigurations) {
		this.requiredConfigurations = requiredConfigurations;
	}
	
	/*
	 * Basic Data operations
	 */
	
	public static PhaseDefinition read(Long id) {
        return find.ref(id);
    }

    public static List<PhaseDefinition> findAll() {
        return find.all();
    }

    public static PhaseDefinition create(PhaseDefinition object) {
        object.save();
        object.refresh();
        return object;
    }

    public static PhaseDefinition createObject(PhaseDefinition object) {
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
