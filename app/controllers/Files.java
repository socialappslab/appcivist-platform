package controllers;

import http.Headers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.List;

import models.misc.S3File;
import models.transfer.TransferResponseStatus;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
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
			@ApiImplicitParam(name = "file", value = "File to upload", dataType = "play.mvc.Http.MultipartFormData", paramType = "body"),
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
}
