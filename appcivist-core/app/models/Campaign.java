package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class Campaign extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3367429873420318943L;

	@Id
	private Long id;
	private String name;
	private String url;
	
	public static Model.Finder<Long, Campaign> find = new Model.Finder<Long, Campaign>(
			Long.class, Campaign.class);

	public static List<Campaign> findAll() {
		List<Campaign> campaigns = find.all();
		return campaigns;
	}

	public static void create(Campaign campaign) {
		campaign.save();
		campaign.refresh();
	}

	public static Campaign read(Long campaignId) {
		return find.ref(campaignId);
	}

	public static Campaign createObject(Campaign campaign) {
		campaign.save();
		return campaign;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
