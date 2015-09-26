package models;

import java.net.MalformedURLException;
import java.net.URL;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@DiscriminatorValue("PICTURE")
@JsonInclude(Include.NON_EMPTY)
public class ResourcePicture extends Resource {
	
	public ResourcePicture(User creator, URL url) {
		super(creator,url);
	}
	
	public ResourcePicture(User creator, URL url, URL large, URL medium, URL thumbnail) {
		super(creator,url);
		
		this.urlLarge = large; 
		this.urlMedium = medium; 
		this.urlThumbnail = thumbnail;
		this.setUrl(urlLarge);
	}
	
	@JsonIgnore
	private URL urlLarge; 
	@JsonIgnore
	private URL urlMedium;
	@JsonIgnore
	private URL urlThumbnail;

	public URL getUrlLarge() {
		return urlLarge;
	}

	public void setUrlLarge(URL urlLarge) {
		this.urlLarge = urlLarge;
		if (this.getUrl()==null)
			this.setUrl(urlLarge);
	}

	public URL getUrlMedium() {
		return urlMedium;
	}

	public void setUrlMedium(URL urlMedium) {
		this.urlMedium = urlMedium;
		if (this.getUrl()==null && this.urlLarge==null)
			this.setUrl(urlMedium);
	}

	public URL getUrlThumbnail() {
		return urlThumbnail;
	}

	public void setUrlThumbnail(URL urlThumbnail) {
		this.urlThumbnail = urlThumbnail;
		if (this.getUrl()==null && this.urlLarge==null && this.urlMedium==null)
			this.setUrl(urlThumbnail);
	}	
	
	@Transient
	public String getUrlLargeString() {
		return urlLarge!=null ? urlLarge.toString() : null;
	}

	@Transient
	public void setUrlLargeString(String urlLargeString) throws MalformedURLException {
		this.urlLarge = new URL(urlLargeString);
	}

	@Transient
	public String getUrlMediumString() {
		return urlMedium!=null ? urlMedium.toString() : null;
	}

	@Transient
	public void setUrlMediumString(String urlMediumString) throws MalformedURLException {
		this.urlMedium = new URL(urlMediumString);
	}

	@Transient
	public String getUrlThumbnailString() {
		return urlThumbnail!=null ? urlThumbnail.toString() : null;
	}

	@Transient
	public void setUrlThumbnailString(String urlThumbnailString) throws MalformedURLException {
		this.urlThumbnail = new URL(urlThumbnailString);
	}
}
