package service;

import java.net.MalformedURLException;

import javax.inject.Inject;

import models.User;
import play.Application;
import utils.security.HashGenerationException;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.service.UserServicePlugin;

import exceptions.TokenNotValidException;

public class MyUserServicePlugin extends UserServicePlugin {

//	public MyUserServicePlugin() {
//		super(null);
//	}

	@Inject
	public MyUserServicePlugin(final Application app) {
		super(app);
	}

	@Override
	public Object save(final AuthUser authUser) {
		final boolean isLinked = User.existsByAuthUserIdentity(authUser);
		if (!isLinked) {
			try {
				return User.createFromAuthUser(authUser).getUserId();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (HashGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (TokenNotValidException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			// we have this user already, so return null
			return null;
		}
	}

	@Override
	public Object getLocalIdentity(final AuthUserIdentity identity) {
		// For production: Caching might be a good idea here...
		// ...and dont forget to sync the cache when users get deactivated/deleted
		final User u = User.findByAuthUserIdentity(identity);
		if(u != null) {
			return u.getUserId();
		} else {
			return null;
		}
	}

	@Override
	public AuthUser merge(final AuthUser newUser, final AuthUser oldUser) {
		if (!oldUser.equals(newUser)) {
			User.merge(oldUser, newUser);
		}
		return oldUser;
	}

	@Override
	public AuthUser link(final AuthUser oldUser, final AuthUser newUser) {
		User.addLinkedAccount(oldUser, newUser);
		return null;
	}

}
