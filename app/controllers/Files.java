package controllers;

import http.Headers;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.With;
import utils.services.imgur.ImgurWrapper;
import utils.services.imgur.response.ImageResponse;
import utils.services.imgur.response.Upload;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "/file")
@With(Headers.class)
public class Files extends Controller {

	/*
	 * Allowed types of files JPEG, PNG, GIF, APNG, TIFF, BMP, PDF, XCF (GIMP).
	 * Please note that TIFF, BMP, PDF and XCF (GIMP) will be converted to PNG
	 * on upload. PNGs over 756KB are automatically converted to JPG.
	 */

	@ApiOperation(produces="text/html", value="Upload a picture to IMGUR", httpMethod="POST", consumes="multipart/form-data")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "title", value = "Title of image", dataType = "String", paramType = "query"),
		@ApiImplicitParam(name = "description", value = "Description of image", dataType = "String", paramType = "query"),
		@ApiImplicitParam(name = "albumId", value = "ID for album (if the user is adding this image to an album)", dataType = "String", paramType = "query"),
		@ApiImplicitParam(name = "image", value = "image", dataType = "play.mvc.Http.MultipartFormData.FilePart", paramType = "form"),
		@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
	})
//	@Restrict({ @Group(GlobalData.USER_ROLE) })
	public static Result uploadPictureToImgur(String title, String description, String albumId) {
		// 1. Get the image from the body
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart image = body.getFile("image") == null ? body.getFile("image[]") : body.getFile("image");
		
		Upload upload = new Upload(image.getFile(), title, description, albumId);

		// 2. Send the image to Imgur
		if (image != null) {
				ImgurWrapper imgur = new ImgurWrapper();
				ImageResponse response = imgur.postImage(
						upload.title,
						upload.description,
						upload.albumId,
						null,
						RequestBody.create(MediaType.parse("image/*"), upload.image));
				return ok(Json.toJson(response));
		} else {
			return badRequest(Json.toJson("No Image Data"));	
		}
	}
	
	// TODO add a more general upload file to a cloud server like Amazon S3 or heroku
}
