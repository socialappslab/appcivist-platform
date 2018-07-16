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
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;

import models.misc.AppcivistFile;
import models.transfer.TransferResponseStatus;
import play.Logger;
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
		List<AppcivistFile> uploads = AppcivistFile.findAll();
		return ok(files.render(uploads));
	}

	@ApiOperation(hidden=true, httpMethod="GET", value="List uploaded files")
	public static Result list() {
		List<AppcivistFile> uploads = AppcivistFile.findAll();
		return ok(Json.toJson(uploads));
	}
	
	@ApiOperation(httpMethod = "POST", response = AppcivistFile.class, produces = "application/json", value = "Upload a file and get its URL")
	@ApiResponses(value = { @ApiResponse(code = BAD_REQUEST, message = "File upload error", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "file", value = "File to upload", dataType = "file", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	public static Result upload() {
		Http.MultipartFormData body = request().body().asMultipartFormData();
		Http.MultipartFormData.FilePart uploadFilePart = body.getFile("file");
		if (uploadFilePart != null) {
			AppcivistFile appcivistFile = new AppcivistFile();
			appcivistFile.name = uploadFilePart.getFilename();
			appcivistFile.file = uploadFilePart.getFile();
			appcivistFile.save();
			return ok(Json.toJson(appcivistFile));
		} else {
			return badRequest(Json.toJson(new TransferResponseStatus(ResponseStatus.BADREQUEST,"File upload error, upload file part is null")));
		}
	}
	
//	public static Result uploadData() {
//		
//	}
	
	@ApiOperation(httpMethod = "POST", response = AppcivistFile.class, consumes="multipart/form-data", produces = "application/json", value = "Upload YML data")
	@ApiResponses(value = { @ApiResponse(code = BAD_REQUEST, message = "File upload error", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
        	@ApiImplicitParam(name = "file", value = "File with Data to Upload", dataType = "file", paramType = "form"),
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

				// update sequences to match the inserted ids
				String sql = "SELECT setval_max('public');";
				SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
				List<SqlRow> list = sqlQuery.findList();
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
