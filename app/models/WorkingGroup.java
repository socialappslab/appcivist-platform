package models;

import play.db.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class WorkingGroup extends Model{

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Resource> resources = new ArrayList<Resource>();
}
