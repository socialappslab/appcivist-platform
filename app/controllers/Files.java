package controllers;

import java.util.List;

import models.misc.S3File;
import http.Headers;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import com.wordnik.swagger.annotations.Api;
import views.html.*;

@Api(value = "/files")
@With(Headers.class)
public class Files extends Controller {

	
	public static Result index() {
		List<S3File> uploads = S3File.findAll();
		return ok(files.render(uploads));
	}

	public static Result upload() {
		Http.MultipartFormData body = request().body().asMultipartFormData();
		Http.MultipartFormData.FilePart uploadFilePart = body.getFile("upload");
		if (uploadFilePart != null) {
			S3File s3File = new S3File();
			s3File.name = uploadFilePart.getFilename();
			s3File.file = uploadFilePart.getFile();
			s3File.save();
			return ok(Json.toJson(s3File));
		} else {
			return badRequest("File upload error");
		}
	}
}
