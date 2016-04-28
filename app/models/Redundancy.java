package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import models.audit.AuditContribution;
import models.location.Location;
//newly added
import delegates.RedundanciesDelegate;
import controllers.Redundancies; 

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.annotation.Index;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import enums.AuditEventTypes;
import enums.ContributionTypes;
import enums.ResourceSpaceTypes;

@Entity @JsonInclude(Include.NON_EMPTY)
@Where(clause="removed=false")
public class Redundancy extends AppCivistBaseModel {

	private Long key;
	private Long value;

	public static Finder<Long, Contribution> find = new Finder<>(
			Contribution.class);

	public Redundancy (Long key, Long value) {
		super(); 
		this.key = key; 
		this.value = value; 
	}

	public Redundancy() {
		super();
	}
	public void add(Long id){

		List<Long> similars = RedundanciesDelegate.match_keywords(id);
		for (Long sim : similars) {
			Redundancy r = new Redundancy(id, sim); 
			save(); 
			update(); 
		}
	}
}







