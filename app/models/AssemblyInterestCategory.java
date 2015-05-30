// TODO to be used only if the connection between assemblies and categories will have some special
// properties
//package models;
//
//import play.db.ebean.Model;
//
//import javax.persistence.CascadeType;
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.ManyToMany;
//import javax.persistence.ManyToOne;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//
//import models.services.ServiceIssue;
//
//import java.util.Date;
//import java.util.List;
//import java.util.ArrayList;
//
//@Entity
//public class AssemblyInterestCategory extends AppCivistBaseModel {
//
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = -6886590347197026138L;
//	@Id
//    private Long interestCategoryId;
//    
//	@ManyToOne(cascade=CascadeType.ALL)
//	private Category category;
//
//	@JsonIgnore
//	@ManyToOne(cascade=CascadeType.ALL)
//	private Assembly assembly;
//
//	/**
//	 * The find property is an static property that facilitates database query creation
//	 */
//    public static Model.Finder<Long, AssemblyInterestCategory> find = new Model.Finder<Long, AssemblyInterestCategory>(
//            Long.class, AssemblyInterestCategory.class);
//
//    
//    
//    
//    public Long getInterestCategoryId() {
//        return interestCategoryId;
//    }
//
//    public void setInterestCategoryId(Long id) {
//        this.interestCategoryId = id;
//    }
//
//    
//    
//	/*
//	 * Basic Data operations
//	 */
//    public static AssemblyInterestCategory read(Long themeId) {
//        return find.ref(themeId);
//    }
//
//    public static List<AssemblyInterestCategory> findAll() {
//        return find.all();
//    }
//
//    public static AssemblyInterestCategory create(AssemblyInterestCategory theme) {
//        theme.save();
//        theme.refresh();
//        return theme;
//    }
//
//    public static AssemblyInterestCategory createObject(AssemblyInterestCategory theme) {
//        theme.save();
//        return theme;
//    }
//
//    public static void delete(Long id) {
//        find.ref(id).delete();
//    }
//
//    public static void update(Long id) {
//        find.ref(id).update();
//    }
//}
