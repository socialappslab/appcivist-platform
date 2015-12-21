package models.misc;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import play.Logger;
import play.Play;

import com.avaje.ebean.Model;

import modules.S3ComponentImpl;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@Entity
public class S3File extends Model {

    @Id
    public UUID id;

    private String bucket;

    public String name;

    @Transient
    public File file;
    
    public static Finder<UUID, S3File> find = new Finder<>(S3File.class);
    
    public URL getUrl() throws MalformedURLException {
        return new URL(S3ComponentImpl.getS3Endpoint() + bucket + "/" + getActualFileName());
    }

    private String getActualFileName() {
        return id + "/" + name;
    }

    @Override
    public void save() {
    	S3ComponentImpl s3 = new S3ComponentImpl();
    	
        if (s3.getAmazonS3() == null) {
            Logger.error("Could not save because amazonS3 was null");
            throw new RuntimeException("Could not save");
        }
        else {
            this.bucket = s3.getS3Bucket();

            super.save(); // assigns an id

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, getActualFileName(), file);
            putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // public for all
            s3.getAmazonS3().putObject(putObjectRequest); // upload file
        }
    }

    @Override
    public void delete() {
    	S3ComponentImpl s3 = new S3ComponentImpl();
        if (s3.getAmazonS3() == null) {
            Logger.error("Could not delete because amazonS3 was null");
            throw new RuntimeException("Could not delete");
        }
        else {
        	s3.getAmazonS3().deleteObject(bucket, getActualFileName());
            super.delete();
        }
    }

	public static List<S3File> findAll() {
		return find.all();
	}
}