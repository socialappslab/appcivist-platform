package models;

import javafx.collections.transformation.SortedList;
import play.db.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Organization extends Model {

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    @Id
    private Long orgId;
    private String name;
    private String description;
    
    private SortedList<Phase> phases;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Theme> themes = new ArrayList<Theme>();

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Assembly> assemblies = new ArrayList<Assembly>();

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<Message>();

}
