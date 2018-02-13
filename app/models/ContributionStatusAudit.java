package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import enums.ContributionStatus;
import io.swagger.annotations.ApiModelProperty;
import models.misc.Views;
import play.Logger;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ContributionStatusAudit extends Model {

    @Id
    @GeneratedValue
    @JsonView({Views.Report.class})
    private Long id;

    @JsonView(Views.Public.class)
    @ManyToOne(cascade = CascadeType.ALL)
    private Contribution contribution;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
    private Date statusStartDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
    private Date statusEndDate;

    @JsonView({Views.Public.class, Views.Report.class})
    @Enumerated(EnumType.STRING)
    private ContributionStatus status;

    private static Finder<Long, ContributionStatusAudit> find = new Finder<>(ContributionStatusAudit.class);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Contribution getContribution() {
        return contribution;
    }

    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    public Date getStatusStartDate() {
        return statusStartDate;
    }

    public void setStatusStartDate(Date statusStartDate) {
        this.statusStartDate = statusStartDate;
    }

    public Date getStatusEndDate() {
        return statusEndDate;
    }

    public void setStatusEndDate(Date statusEndDate) {
        this.statusEndDate = statusEndDate;
    }

    public ContributionStatus getStatus() {
        return status;
    }

    public void setStatus(ContributionStatus status) {
        this.status = status;
    }

    public static void create(Contribution contribution) {
        Logger.info("Creating contributionStatusAudit ");
        contribution.refresh();
        ContributionStatusAudit contributionStatusAudit = new ContributionStatusAudit();
        contributionStatusAudit.setContribution(contribution);
        contributionStatusAudit.setStatusStartDate(new Date());
        contributionStatusAudit.setStatus(contribution.getStatus());
        contributionStatusAudit.save();
        contributionStatusAudit.refresh();
    }

    public static ContributionStatusAudit getLastByContribution(Contribution contribution) {

     List<ContributionStatusAudit> list = find.where().eq("contribution",
             contribution).orderBy("id desc").setMaxRows(1).findList();
     if(list.isEmpty()) {
         return null;
     } else {
         return list.get(0);
     }
    }

    public static void newStatus(Contribution contribution) {
        ContributionStatusAudit prev = getLastByContribution(contribution);
        if(prev != null) {
            prev.setStatusEndDate(new Date());
            prev.update();
        }
        create(contribution);
    }
}
