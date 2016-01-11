package modules;

import javax.inject.Inject;
import javax.inject.Singleton;

import play.Application;
import play.Logger;
import play.Play;
import play.inject.ApplicationLifecycle;
import play.libs.F;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

//@Singleton
public class S3ComponentImpl { //implements S3Component {
	

	public static final String AWS_S3_BUCKET = "appcivist.services.aws.s3.bucket";
	public static final String AWS_ACCESS_KEY = "appcivist.services.aws.access.key";
	public static final String AWS_SECRET_KEY = "appcivist.services.aws.secret.key";
	public static final String AWS_S3_ENDPOINT = "appcivist.services.aws.s3.endpoint";
	
	public Application application = Play.application();
	public AmazonS3 amazonS3;
	public String s3Bucket;
	
//	{
//		String accessKey = application.configuration().getString(AWS_ACCESS_KEY);
//		String secretKey = application.configuration().getString(AWS_SECRET_KEY);
//		s3Bucket = application.configuration().getString(AWS_S3_BUCKET);
//
//		if ((accessKey != null) && (secretKey != null)) {
//			AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey,
//					secretKey);
//			amazonS3 = new AmazonS3Client(awsCredentials);
//			amazonS3.createBucket(s3Bucket);
//			Logger.info("Using S3 Bucket: " + s3Bucket);
//		}
//	}
	
//	@Inject
	public S3ComponentImpl() {
		String accessKey = application.configuration().getString(AWS_ACCESS_KEY);
		String secretKey = application.configuration().getString(AWS_SECRET_KEY);
		s3Bucket = application.configuration().getString(AWS_S3_BUCKET);

		if ((accessKey != null) && (secretKey != null)) {
			AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey,
					secretKey);
			amazonS3 = new AmazonS3Client(awsCredentials);
			//amazonS3.createBucket(s3Bucket);
			Logger.info("Using S3 Bucket: " + s3Bucket);
		}
	}

	public AmazonS3 getAmazonS3() {
		return amazonS3;
	}

	public String getS3Bucket() {
		return s3Bucket;
	}
	
	public static String getS3Endpoint() {
		return Play.application().configuration().getString(AWS_S3_ENDPOINT);
	}
}