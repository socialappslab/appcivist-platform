package modules;

import be.objectify.deadbolt.java.cache.HandlerCache;
import com.feth.play.module.pa.providers.oauth2.facebook.FacebookAuthProvider;
import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthProvider;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;
import security.MyHandlerCache;
import service.facebook.FacebookAuthProviderLocal;

import javax.inject.Singleton;

public class CustomDeadboltHook extends Module
{
    @Override
    public Seq<Binding<?>> bindings(Environment environment,
                                    Configuration configuration)
    {
        return seq(
                    bind(HandlerCache.class).to(MyHandlerCache.class).in(Singleton.class),
                    bind(GoogleAuthProvider.class).toSelf().eagerly(),
                    bind(FacebookAuthProviderLocal.class).toSelf().eagerly()
                );

    }
}