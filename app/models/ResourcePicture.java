package models;

import java.net.URL;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("PICTURE")
public class ResourcePicture extends Resource {
	
	public ResourcePicture(User creator, URL url) {
		super(creator,url);
	}
	
	public ResourcePicture(User creator, URL url, URL large, URL medium, URL thumbnail) {
		super(creator,url);
		
		this.urlLarge = large; 
		this.urlMedium = medium; 
		this.urlThumbnail = thumbnail;
	}
	
	private URL urlLarge; 
	private URL urlMedium;
	private URL urlThumbnail;
	
	public URL getUrlLarge() {
		return urlLarge;
	}

	public void setUrlLarge(URL urlLarge) {
		this.urlLarge = urlLarge;
	}

	public URL getUrlMedium() {
		return urlMedium;
	}

	public void setUrlMedium(URL urlMedium) {
		this.urlMedium = urlMedium;
	}

	public URL getUrlThumbnail() {
		return urlThumbnail;
	}

	public void setUrlThumbnail(URL urlThumbnail) {
		this.urlThumbnail = urlThumbnail;
	}	
}
