package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import models.location.Location;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import utils.GlobalData;

import com.avaje.ebean.Query;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ResourceSpaceTypes;

/**
 * An assembly represents the central repository of a group of people interested
 * in organizing themselves as a community for engaging in social activism 
 * 
 * @author cdparra
 */
@Entity
@JsonInclude(Include.NON_EMPTY)
public class Assembly extends AppCivistBaseModel {
	@Id 
	@GeneratedValue
	@Column(name="assembly_id")
	private Long assemblyId;

	/**
	 * Properties specific to the Assembly
	 */

	private UUID uuid;
	@Transient
	private String uuidAsString;
	@MaxLength(value = 200)
	@Required
	private String name; // name of the assembly
	@MaxLength(value = 120)
	/**
	 * Shortname to access the assembly by name (automatically generated from the name)
	 */
	private String shortname; 
	@Required
	private String description; // what's the assembly about
	private String url; // URL to the assembly, using its shortname
	private Boolean listed = true;
	
	@OneToOne(mappedBy = "assembly", cascade=CascadeType.ALL)
	@JsonIgnoreProperties({ "assemblyProfileId", "assembly" })
	@JsonInclude(Include.NON_EMPTY)
	private AssemblyProfile profile;

	@ManyToOne(cascade=CascadeType.ALL)
	private Location location;
	
	@Transient
	@JsonIgnore
	private List<Theme> themes;

	/**
	 * The assembly resource set is where all the campaign, configurations,
	 * themes and general contributions are stored. Other resource spaces will
	 * be added if needed under proper names
	 */
	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	@JsonIgnoreProperties({ "uuid" })
	@JsonInclude(Include.NON_EMPTY)
	private ResourceSpace resources;

	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	@JsonIgnoreProperties({ "uuid" })
	@JsonInclude(Include.NON_EMPTY)
	private ResourceSpace forum;

	/**
	 * The User who created the Assembly
	 */
	private User creator; // user who has created the assembly?
	
	// TODO:
	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, Assembly> find = new Finder<>(Assembly.class);

	/**
	 * Empty constructor
	 */
	public Assembly() {
		super();
		this.uuid = UUID.randomUUID();
		this.setDefaultValues();
	}

	/**
	 * Basic assembly constructor (with the most basic elements of an assembly)
	 * 
	 * @param The
	 *            name of the assembly
	 * @param assemblyDescription
	 * @param assemblyCity
	 */
	public Assembly(String assemblyName, String assemblyDescription,
			String assemblyCity) {
		super();
		this.uuid = UUID.randomUUID();
		this.name = assemblyName;
		this.description = assemblyDescription;
		this.location = new Location(null, assemblyCity, null, null, null);
		this.setDefaultValues();
	}

	public Assembly(User creator, String name, String description, String city,
			String state, String country, AssemblyProfile profile) {
		super();
		this.uuid = UUID.randomUUID();
		this.creator = creator;
		this.name = name;
		this.description = description;
		this.location = new Location(null, city, state, null, country);
		this.profile = profile;
		this.setDefaultValues();
	}

	public Assembly(User creator, String name, String shortname,
			String description, Location location, UUID uuid,
			AssemblyProfile profile, ResourceSpace resources) {
		super();
		this.uuid = uuid;
		this.creator = creator;
		this.name = name;
		this.shortname = shortname;
		this.description = description;
		this.location = location;
		this.profile = profile;
		this.resources = resources;
	}
	
	public Assembly(User creator, String name, String shortname,
			String description, Location location, String uuid,
			AssemblyProfile profile, ResourceSpace resources) {
		super();
		this.uuidAsString = uuid;
		this.uuid = UUID.fromString(this.uuidAsString);
		this.creator = creator;
		this.name = name;
		this.shortname = shortname;
		this.description = description;
		this.location = location;
		this.profile = profile;
		this.resources = resources;
	}

	/*
	 * Getters and Setters
	 */

	public Long getAssemblyId() {
		return assemblyId;
	}

	public void setAssemblyId(Long assemblyId) {
		this.assemblyId = assemblyId;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public AssemblyProfile getProfile() {
		return this.profile;
	}

	public void setProfile(AssemblyProfile p) {
		this.profile = p;
	}

	/*
	 * Basic Data Queries
	 */

	public ResourceSpace getResources() {
		return resources;
	}

	public void setResources(ResourceSpace resources) {
		this.resources = resources;
	}

	public ResourceSpace getForum() {
		return forum;
	}

	public void setForum(ResourceSpace forum) {
		this.forum = forum;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getUuidAsString() {
		return uuid.toString();
	}

	public void setUuidAsString(String uuidAsString) {
		this.uuidAsString = uuidAsString;
		this.uuid = UUID.fromString(uuidAsString);
	}

	public Boolean getListed() {
		return listed;
	}

	public void setListed(Boolean listed) {
		this.listed = listed;
	}

	public List<Theme> getThemes() {
		return resources != null ? resources.getThemes() : null;
	}

	public void setThemes(List<Theme> themes) {
		this.themes = themes;
	}

	/**
	 * Returns all the assemblies in our system
	 * 
	 * @return
	 */
	public static List<Assembly> findAll(Boolean onlyListed) {
		if(onlyListed) return find.where().eq("listed",true).findList();
		else return find.all();
	}

	public static void create(Assembly assembly) {
		if (assembly.getAssemblyId() != null
				&& (assembly.getUrl() == null || assembly.getUrl() == "")) {
			assembly.setUrl(GlobalData.APPCIVIST_ASSEMBLY_BASE_URL + "/"
					+ assembly.getAssemblyId());
		}

		assembly.save();
		assembly.refresh();

		if (assembly.getUrl() == null || assembly.getUrl() == "") {
			assembly.setUrl(GlobalData.APPCIVIST_ASSEMBLY_BASE_URL + "/"
					+ assembly.getAssemblyId());
		}
	}

	public static Assembly read(Long assemblyId) {
		return find.ref(assemblyId);
	}

	public static Assembly createObject(Assembly assembly) {
		assembly.save();
		return assembly;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static Assembly update(Assembly a) {
		a.update();
		a.refresh();
		return a;
	}

	public void setDefaultValues() {
		// 1. Set default shortname if not given
		if (this.shortname == null) {
			if (this.name != null) {
				this.shortname = name.replaceAll("[^\\w]", "")
						.replaceAll("[\\s]", "-").toLowerCase();
			}
		}

		this.resources = new ResourceSpace(ResourceSpaceTypes.ASSEMBLY);
		this.forum = new ResourceSpace(ResourceSpaceTypes.ASSEMBLY);
	}

	public static List<Assembly> findBySimilarName(String query, Boolean onlyListed) {
		Query<Assembly> q = find.where().ilike("name", "%" + query + "%").query();
		if(onlyListed) q = q.where().eq("listed",true).query();
		return q.findList();
		
	}

	public static List<Assembly> findFeaturedAssemblies(String query, Boolean onlyListed) {
		Query<Assembly> q = find.setMaxRows(6).orderBy("creation");
		q = addQueryCriteria("name",query, q);
		if(onlyListed) q = q.where().eq("listed",true).query();
		return q.findList();
	}

	public static List<Assembly> findRandomAssemblies(String query, Boolean onlyListed) {
		Query<Assembly> q = find.setMaxRows(6).orderBy("random()");
		q = addQueryCriteria("name",query, q);
		if(onlyListed) q = q.where().eq("listed",true).query();
		return q.findList();
	}

	private static Query<Assembly> addQueryCriteria(String property,  String query, Query<Assembly> q) {
		if(query!=null && !query.isEmpty()) {
			q = q.where().ilike(property, "%"+query+"%").query();
		}
		return q;
	}

	public static Assembly readByUUID(UUID assemblyUUID) {
		return find.where().eq("uuid", assemblyUUID).findUnique();
	}
}
