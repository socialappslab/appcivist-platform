package models;

import java.net.URL;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("PAD")
public class ResourcePad extends Resource {

	private String padId;	
	public ResourcePad(User creator, URL url) {
		super(creator,url);
	}
	
	public ResourcePad(User creator, URL url, String padId) {
		super(creator,url);
		this.setPadId(padId);
	}
	
	public String getPadId() {
		return padId;
	}

	public void setPadId(String padId) {
		this.padId = padId;
	}	
}
