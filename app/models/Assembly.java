package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import models.Location.Geo;
import play.db.ebean.Model;
import utils.GlobalData;
import enums.Visibility;

@Entity
public class Assembly extends AppCivistBaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 128881028915968230L;

	@Id
	@GeneratedValue
	private Long assemblyId;

	/**
	 * Properties specific to the Assembly
	 */

	private User creator;
	private String name; // TODO limit to no more than what a title should be (150 chars maybe?)
	private String description;
	private String city;
	private String state;
	private String country;
	private String icon = GlobalData.APPCIVIST_ASSEMBLY_DEFAULT_ICON;
	private String url; 
	private Visibility visibiliy = Visibility.MEMBERSONLY; // only members by default

	// Relationships
	@ManyToMany(cascade = CascadeType.ALL)
	private List<Category> interestCategories = new ArrayList<Category>();

	@OneToMany(mappedBy = "assembly", cascade = CascadeType.ALL)
	private List<Campaign> campaigns = new ArrayList<Campaign>();

	@OneToMany(cascade = CascadeType.ALL)
	private List<Config> assemblyConfigs = new ArrayList<Config>();

	@ManyToMany(cascade = CascadeType.ALL)
	private List<Hashtag> hashtags = new ArrayList<Hashtag>();

	// Even if in the original design, we will have only working groups
	// belonging to
	// one assembly, let's make it manytomany in case we need it for the future
	@ManyToMany(cascade = CascadeType.ALL)
	private List<WorkingGroup> workingGroups = new ArrayList<WorkingGroup>();

// 	AssemblyConnections and Messages are managed in different entity
// 	TODO check that this works
//	@OneToMany(mappedBy = "targetAssembly", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	private List<AssemblyConnection> connections = new ArrayList<AssemblyConnection>();
//
//	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	private List<Message> messages = new ArrayList<Message>();


	/**
	 * Experimental properties (things we might want to have)
	 */
	// A location specification that is more precise, based on the GeoJSON
	// standard
	// Basically, with this location object, we can specify whether an entity is
	// located in a specific geo point, route/line or area.
	@OneToOne
	private Geo location;

	// If assemblies decide to collaborate on campaigns, then we might want to
	// have
	// the notion of "shared campaigns
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Campaign> sharedCampaigns = new ArrayList<Campaign>();
	
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Model.Finder<Long, Assembly> find = new Model.Finder<Long, Assembly>(
			Long.class, Assembly.class);

	/**
	 * Empty constructor
	 */
	public Assembly() {
		super();
	}

	/**
	 * Basic assembly constructor (with the most basic elements of an assembly)
	 * @param assemblyTitle
	 * @param assemblyDescription
	 * @param assemblyCity
	 */
	public Assembly(String assemblyTitle, String assemblyDescription,
			String assemblyCity) {
		super();
		this.name = assemblyTitle;
		this.description = assemblyDescription;
		this.city = assemblyCity;
	}

	public Assembly(User creator, String name, String description, String city,
			String state, String country, String icon, Visibility visibility) {
		super();
		this.creator = creator;
		this.name = name;
		this.description = description;
		this.city = city;
		this.state = state;
		this.country = country;
		this.icon = icon;
		this.visibiliy = visibility;
	}

	/** 
	 * Assembly constructor, including the basic lists of categories (interests), hashtags
	 * and configuration key/value pairs
	 * @param lang 
	 * @param creator
	 * @param name
	 * @param description
	 * @param city
	 * @param state
	 * @param country
	 * @param icon
	 * @param interests
	 * @param hashtags
	 * @param configurations
	 */
	public Assembly(String lang, User creator, String name, String description, String city,
			String state, String country, String icon, 
			Visibility visibility,
			List<Category> interests,
			List<Hashtag> hashtags,
			List<Config> assemblyConfigs) {
		super(lang);
		this.creator = creator;
		this.name = name;
		this.description = description;
		this.city = city;
		this.state = state;
		this.country = country;
		this.icon = icon;
		this.visibiliy = visibility;
		this.interestCategories = interests;
		this.hashtags = hashtags;
		this.assemblyConfigs = assemblyConfigs;
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

	/*
	 * Getters and Setters
	 */
	
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Visibility getVisibiliy() {
		return visibiliy;
	}

	public void setVisibiliy(Visibility visibiliy) {
		this.visibiliy = visibiliy;
	}

	public List<Category> getInterestCategories() {
		return interestCategories;
	}

	public void setInterestCategories(List<Category> interests) {
		this.interestCategories = interests;
	}

	public List<Campaign> getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(List<Campaign> campaigns) {
		this.campaigns = campaigns;
	}

	public List<Config> getAssemblyConfigs() {
		return assemblyConfigs;
	}

	public void setAssemblyConfigs(List<Config> assemblyConfigs) {
		this.assemblyConfigs = assemblyConfigs;
	}

	public List<Hashtag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(List<Hashtag> hashtags) {
		this.hashtags = hashtags;
	}

	public List<WorkingGroup> getWorkingGroups() {
		return workingGroups;
	}

	public void setWorkingGroups(List<WorkingGroup> workingGroups) {
		this.workingGroups = workingGroups;
	}

	public Geo getLocation() {
		return location;
	}

	public void setLocation(Geo location) {
		this.location = location;
	}

	public List<Campaign> getSharedCampaigns() {
		return sharedCampaigns;
	}

	public void setSharedCampaigns(List<Campaign> sharedCampaigns) {
		this.sharedCampaigns = sharedCampaigns;
	}

	/*
	 * Basic Data Queries
	 */
	
	/**
	 * Returns all the assemblies in our system
	 * @return
	 */
	public static AssemblyCollection findAll() {
		List<Assembly> assemblies = find.all();
		AssemblyCollection assemblyCollection = new AssemblyCollection();
		assemblyCollection.setAssemblies(assemblies);
		return assemblyCollection;
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

	public static void update(Long id) {
		find.ref(id).update();
	}

	/*
	 * Other Queries
	 * 
	 * TODO: 
	 * [done] Create an assembly
		Create an assembly with a name, a description and a assembly logo/icon. 
		Provide initial configuration
		Establish if the assembly is public/private
		Establish if members can create new issues
		Establish if members can join by request or only by invitation
		Associate hashtags
		Invite users to become members of the assembly
		Configure an assembly
			can members create new issues?
			update hashtags
		Publish the assembly (make assembly public to all users) or keep private
			update proposal template
		Export issues from one assembly into another 
		Export proposals from one assembly into an issue of another assembly
		Import issues from another assembly
		Import proposals from another assembly into an issue [what working group will be assigned to them?]
		Import proposal template from another assembly		
		Subscribe to another assembly
		Publish issues (if assembly is public, all issues are automatically published, i.e., public)	
		Publish proposals (if assembly is public, all proposals are automatically published unless the working group unpublishes the proposal)
	 */
	

}
