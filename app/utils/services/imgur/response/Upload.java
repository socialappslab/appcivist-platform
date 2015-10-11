package utils.services.imgur.response;

import java.io.File;

/**
 * Created by AKiniyalocts on 2/24/15.
 *
 * Basic object for upload.
 */
public class Upload {
	public File image;
	public String title;
	public String description;
	public String albumId;

	public Upload(File image, String title, String description, String albumId) {
		super();
		this.image = image;
		this.title = title;
		this.description = description;
		this.albumId = albumId;
	}
	
	public Upload(File image, String title, String description) {
		super();
		this.image = image;
		this.title = title;
		this.description = description;
	}
}
