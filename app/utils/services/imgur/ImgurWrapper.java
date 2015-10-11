package utils.services.imgur;

import retrofit.Retrofit;
import retrofit.GsonConverterFactory;
import retrofit.RxJavaCallAdapterFactory;
import utils.services.imgur.response.ImageResponse;
import utils.services.imgur.service.ImgurAPI;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;

/**
 * Implementation of the Wrapper for the IMGUR API based on the client developed by 
 * https://github.com/Rabrg/imgur-api
 * 
 * @author cdparra
 *
 */
public class ImgurWrapper {
	
	public static final boolean LOGGING = false;

	/*
	 * Redirect URL for android.
	 */
	public static final String MY_IMGUR_REDIRECT_URL = "http://localhost";
    private static final ImgurRequestInterceptor interceptor;
    
	private final Retrofit restAdapter;
    private final ImgurAPI imgurAPI;
    
// TODO: Adapt specific services to cover all API ENDPOINTS in imgur
//	private final ImageService imageService;
//  private final AlbumService albumService;
//  private final AccountService accountService;

    static {	
		interceptor = new ImgurRequestInterceptor();
	}

    /**
     * Constructs a new ImgurApi with the specified clientId.
     * @param clientId The client id.
     */
    public ImgurWrapper() {
		OkHttpClient okHttpClient = new OkHttpClient();
		okHttpClient.interceptors().add(interceptor);
		restAdapter = new Retrofit.Builder()
        		.baseUrl("https://api.imgur.com/3")
        		.addConverterFactory(GsonConverterFactory.create())
        		.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        		.client(okHttpClient)
        		.build();

        imgurAPI = restAdapter.create(ImgurAPI.class);
// TODO: once all interfaces are updated, instantiate a service for each in the adapter
//        imageService = restAdapter.create(ImageService.class);
//        albumService = restAdapter.create(AlbumService.class);
//        accountService = restAdapter.create(AccountService.class);        
    }    
	
	public ImgurRequestInterceptor getInterceptor() {
		return interceptor;
	}

	/*
	 * Client Auth
	 */
	public String getClientAuth() {
		return "Client-ID " + interceptor.getClientId();
	}
	
	public ImageResponse postImage(String title, String description, String albumId, String username, RequestBody file) {
		return imgurAPI.postImage(title, description, albumId, username, file);
	}
	
	/** 
	 * TODO verifty models and service interfaces for the methods below, to make them 
	 * compliant with the latest imgur api
	 */
	
// 	/**
//     * Gets information about an image.
//     * @param id The id of the image.
//     * @return A response containing information about the image.
//     */
//    public Response<Image> getImage(final String id) {
//        return imageService.getImage(id);
//    }
//
//    /**
//     * Uploads a new image.
//     * @param image A binary file, base64 data, or a URL for the image being uploaded.
//     * @param album The id of the album you want to add the image to. For anonymous albums, {album} should be the deletehash that is returned at creation.
//     * @param type The type of the file that's being sent; file, base64 or URL
//     * @param name The name of the file, this is automatically detected if uploading a file with a POST and multipart / form-data
//     * @param title The title of the image.
//     * @param description The description of the image.
//     * @return A basic response.
//     */
//    public Response<Basic> uploadImage(final String image, final String album, final String type, final String name, final String title, final String description) {
//        return imageService.uploadImage(image, album, type, name, title, description);
//    }
//
//    /**
//     * Deletes an image. For an anonymous image, {id} must be the image's deletehash. If the image belongs to your account then passing the ID of the image is sufficient.
//     * @param id The id of the image. For anonymous images, id should be the deletehash that is returned at creation.
//     * @return A basic response.
//     */
//    public Response<Basic> deleteImage(final String id) {
//        return imageService.deleteImage(id);
//    }
//
//    /**
//     * Updates the title or description of an image. You can only update an image you own and is associated with your account. For an anonymous image, id must be the image's deletehash.
//     * @param id The id of the image. For anonymous images, id should be the deletehash that is returned at creation.
//     * @param title The title of the image.
//     * @param description The description of the image.
//     * @return A basic response.
//     */
//    public Response<Basic> updateImage(final String id, final String title, final String description) {
//        return imageService.updateImage(id, title, description);
//    }
//
//    /**
//     * Favorite an image with the given ID. The user is required to be logged in to favorite the image.
//     * @param id The id of the image.
//     * @return A basic response.
//     */
//    public Response<Basic> favoriteImage(final String id) {
//        return imageService.favoriteImage(id);
//    }
//
//    /**
//     * Get information about a specific album.
//     * @param id The id of the album.
//     * @return A response containing information about the album.
//     */
//    public Response<Album> getAlbum(final String id) {
//        return albumService.getAlbum(id);
//    }
//
//    /**
//     * Get information about an image in an album.
//     * @param albumId The id of the album.
//     * @param imageId The id of the image.
//     * @return  A response containing information about the image.
//     */
//    public Response<Image> getAlbumImage(final String albumId, final String imageId) {
//        return albumService.getAlbumImage(albumId, imageId);
//    }
//
//    /**
//     * Get standard user information.
//     * @param username The username of the account.
//     * @return A response containing information about the account.
//     */
//    public Response<Account> getAccount(final String username) {
//        return accountService.getAccount(username);
//    }
}
