package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import enums.ResourceSpaceAssociationTypes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.misc.Views;
import play.data.validation.Constraints.Required;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="ResourceSpaceAssociationHistory", description="Model reprensenting ResourceSpace Associations History")
public class ResourceSpaceAssociationHistory extends Model {
	@Id
	@GeneratedValue
	private Long id;

	@OneToOne(cascade=CascadeType.ALL)
	@JsonInclude(Include.NON_EMPTY)
	@JsonIgnore
	private ResourceSpace resourceSpace;

	@Column(name="entity_id")
	private Long entityId;

	@Column(name="entity_type")
	@Enumerated(EnumType.STRING)
	private ResourceSpaceAssociationTypes entityType;

	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	@ApiModelProperty(name="creation", value="Date in which this resource was created", notes="By default set to NOW")
	private Date creation = new Date();


	public static Finder<Long, ResourceSpaceAssociationHistory> find = new Finder<>(ResourceSpaceAssociationHistory.class);

	public ResourceSpaceAssociationHistory() {
		super();
	}

	public static ResourceSpaceAssociationHistory read(Long id) {
		return find.ref(id);
	}


	public static List<ResourceSpaceAssociationHistory> findAll() {
		return find.all();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ResourceSpace getResourceSpace() {
		return resourceSpace;
	}

	public void setResourceSpace(ResourceSpace resourceSpace) {
		this.resourceSpace = resourceSpace;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public ResourceSpaceAssociationTypes getEntityType() {
		return entityType;
	}

	public void setEntityType(ResourceSpaceAssociationTypes entityType) {
		this.entityType = entityType;
	}

	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

/*
     * Basic Data operations
     */

	public static ResourceSpaceAssociationHistory create(ResourceSpaceAssociationHistory associationHistory) {
		associationHistory.save();
		associationHistory.refresh();
		return associationHistory;
	}

	public static ResourceSpaceAssociationHistory createObject(ResourceSpaceAssociationHistory associationHistory) {
		associationHistory.save();
		return associationHistory;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}


	public static void createAssociationHistory(ResourceSpace resourceSpace, ResourceSpaceAssociationTypes type, Long entityId) {
		ResourceSpaceAssociationHistory associationHistory = new ResourceSpaceAssociationHistory();
		associationHistory.setResourceSpace(resourceSpace);
		associationHistory.setCreation(new Date());
		associationHistory.setEntityType(type);
		associationHistory.setEntityId(entityId);
		associationHistory.save();
	}
}
