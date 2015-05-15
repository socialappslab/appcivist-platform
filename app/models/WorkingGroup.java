package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.*;
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

    @Id
    private Long groupId;
    private String name;
    private String text;
    private Date expiration;


    @ManyToMany(cascade = CascadeType.ALL)
    private List<Resource> resources = new ArrayList<Resource>();

    @JsonIgnore
    @ManyToOne
    private Role role;
}
