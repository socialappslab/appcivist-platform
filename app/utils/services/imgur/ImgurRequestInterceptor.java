package utils.services.imgur;

import java.io.IOException;

import play.Play;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * This interceptor compresses the HTTP request body. Many webservers can't
 * handle this!
 */
public class ImgurRequestInterceptor implements Interceptor {
	private static String MY_IMGUR_CLIENT_ID = "";
	private static String MY_IMGUR_CLIENT_SECRET = "";
	static {
		MY_IMGUR_CLIENT_ID = Play.application().configuration()
				.getString("appcivist.services.imgur.default.clientId");
		MY_IMGUR_CLIENT_SECRET = Play.application().configuration()
				.getString("appcivist.services.imgur.default.clientSecret");
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request originalRequest = chain.request();
		Request compressedRequest = originalRequest.newBuilder()
				.header("Authorization", "Client-ID " + MY_IMGUR_CLIENT_ID)
				.build();
		return chain.proceed(compressedRequest);
	}

	public String getClientId() {
		return MY_IMGUR_CLIENT_ID;
	}
	
	public String getClientSecret() {
		return MY_IMGUR_CLIENT_SECRET;
	}
}
