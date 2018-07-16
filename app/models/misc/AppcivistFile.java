package models.misc;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.apache.commons.io.FileUtils;
import play.Logger;

import com.avaje.ebean.Model;

import modules.S3ComponentImpl;
import play.Play;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@Entity(name = "appcivist_file")
public class AppcivistFile extends Model {

    @Id
    public UUID id = UUID.randomUUID();

    private String bucket;

    public String name;

    public String url;

    public String target;

    @Transient
    public File file;
    
    public static Finder<UUID, AppcivistFile> find = new Finder<>(AppcivistFile.class);

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private URL getS3Url() throws MalformedURLException {
        return new URL(S3ComponentImpl.getS3Endpoint() + bucket + "/" + getActualFileName());
    }

    private String getActualFileName() {
        return id + "/" + name;
    }


    private void saveS3() throws MalformedURLException {
        S3ComponentImpl s3 = new S3ComponentImpl();

        if (s3.getAmazonS3() == null) {
            Logger.error("Could not save because amazonS3 was null");
            throw new RuntimeException("Could not save");
        }
        else {
            this.bucket = s3.getS3Bucket();
            this.target = "s3";
            this.url = getS3Url().toString();
            super.save(); // assigns an id

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, getActualFileName(), file);
            putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // public for all
            s3.getAmazonS3().putObject(putObjectRequest); // upload file
        }
    }

    @Override
    public void save() {

        String fileServerConf = Play.application().configuration().getString("application.fileServer", "local");

        if("s3".equals(fileServerConf))  {
            try {
                saveS3();
            } catch (MalformedURLException e) {
                Logger.error("Error saving s3 file " + e.getMessage());
            }
        } else {
            String pathToSave = Play.application().configuration().getString("application.contributionFilesPath");

            if(pathToSave == null) {
                Logger.error("No path to save configured");
                return;
            }

            pathToSave = pathToSave + "uploads/";
            String fileName = this.id.toString() + this.getActualFileName();
            try {
                FileUtils.moveFile(this.file, new File(pathToSave, fileName));
            } catch (IOException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                Logger.error("FILESERVER: Error saving local file " + pathToSave+this.id.toString());
                Logger.debug("FILESERVER: Exception: "+e.getStackTrace().toString()+" | "+e.getMessage()+" | "+sw.toString());

            }
            this.url =  Play.application().configuration()
                    .getString("application.contributionFiles") + "uploads/" + fileName;
            this.target = "local";
            super.save();
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

	public static List<AppcivistFile> findAll() {
		return find.all();
	}
}