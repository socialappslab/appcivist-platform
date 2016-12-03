package controllers;

import http.Headers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.avaje.ebean.Ebean;

import models.misc.InitialDataConfig;
import models.misc.S3File;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.api.http.MediaType;
import play.libs.Json;
import play.libs.Yaml;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import utils.GlobalData;
import views.html.files;
import enums.ResponseStatus;

@Api(value = "07 files: Upload files")
@With(Headers.class)
public class Files extends Controller {
	@ApiOperation(hidden=true, httpMethod="GET", value="List uploaded files")
	public static Result index() {
		List<S3File> uploads = S3File.findAll();
		return ok(files.render(uploads));
	}

	@ApiOperation(hidden=true, httpMethod="GET", value="List uploaded files")
	public static Result list() {
		List<S3File> uploads = S3File.findAll();
		return ok(Json.toJson(uploads));
	}
	
	@ApiOperation(httpMethod = "POST", response = S3File.class, produces = "application/json", value = "Upload a file and get its URL")
	@ApiResponses(value = { @ApiResponse(code = BAD_REQUEST, message = "File upload error", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "file", value = "File to upload", dataType = "file", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	public static Result upload() {
		Http.MultipartFormData body = request().body().asMultipartFormData();
		Http.MultipartFormData.FilePart uploadFilePart = body.getFile("file");
		if (uploadFilePart != null) {
			S3File s3File = new S3File();
			s3File.name = uploadFilePart.getFilename();
			s3File.file = uploadFilePart.getFile();
			s3File.save();
			return ok(Json.toJson(s3File));
		} else {
			return badRequest(Json.toJson(new TransferResponseStatus(ResponseStatus.BADREQUEST,"File upload error, upload file part is null")));
		}
	}
	
//	public static Result uploadData() {
//		
//	}
	
	@ApiOperation(httpMethod = "POST", response = S3File.class, consumes="multipart/form-data", produces = "application/json", value = "Upload YML data")
	@ApiResponses(value = { @ApiResponse(code = BAD_REQUEST, message = "File upload error", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "file", value = "File to upload", dataType = "java.io.File", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Restrict({ @Group(GlobalData.ADMIN_ROLE) })
	public static Result uploadData() {
		Logger.info("Reading data file...");
		Http.MultipartFormData body = request().body().asMultipartFormData();
		Http.MultipartFormData.FilePart uploadFilePart = body.getFile("file");
		if (uploadFilePart != null) {
			String filename = uploadFilePart.getFilename();
			File file = uploadFilePart.getFile();
			Logger.info("Data file received: " + filename);
			Logger.info("Copying data into database...");
			try {
				loadDataFile(filename, file);
			} catch (Exception e) {
				Logger.info("---> AppCivist: A problem occurred while loading '"+ filename + "'...");
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				Logger.debug("Exception: "+e.getStackTrace().toString()+" | "+e.getMessage()+" | "+sw.toString());
				return badRequest(Json.toJson(new TransferResponseStatus(
						ResponseStatus.SERVERERROR, "Data upload error: "
								+ e.getStackTrace().toString() + " | "
								+ e.getMessage() + " | " + sw.toString())));
			}
			return ok(Json.toJson(new TransferResponseStatus(ResponseStatus.OK,"Data uploaded!")));
		} else {
			return badRequest(Json.toJson(new TransferResponseStatus(ResponseStatus.BADREQUEST,"File upload error, upload file part is null")));
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static void loadDataFile(String filename, File file) throws FileNotFoundException  {
		Logger.info("---> AppCivist: Loading '" + filename + "'...");
		FileInputStream is = new FileInputStream(file);
		List list = (List) Yaml.load(is, play.Play.application().classloader());
		Logger.info("---> AppCivist: '" + filename + "' will be loaded to database now...");
		Ebean.save(list);
		Logger.info("---> AppCivist: '" + filename + "' loaded successfully!");	
	}
}
