package utils.services.imgur.service;

import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Query;
import utils.services.imgur.response.ImageResponse;

import com.squareup.okhttp.RequestBody;

/**
 * Created by AKiniyalocts on 2/23/15.
 * <p/>
 * This is our imgur API. It generates a rest API via Retrofit from Square inc.
 * <p/>
 * more here: http://square.github.io/retrofit/
 */
public interface ImgurAPI {
    String server = "https://api.imgur.com/3";


    /****************************************
     * Upload
     * Image upload API
     */

    /**
     * @param auth        #Type of authorization for upload
     * @param title       #Title of image
     * @param description #Description of image
     * @param albumId     #ID for album (if the user is adding this image to an album)
     * @param username    username for upload
     * @param file        image
     * @param cb          Callback used for success/failures
     */
    @Multipart
    @POST("/image")
    ImageResponse postImage(
            @Query("title") String title,
            @Query("description") String description,
            @Query("album") String albumId,
            @Query("account_url") String username,
            @Part("image") RequestBody file
    );
}
