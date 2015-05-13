package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import java.util.Date;

@Entity
public class Module extends Model{

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;
}
