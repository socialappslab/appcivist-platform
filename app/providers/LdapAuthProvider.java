package providers;

import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.wwwauth.basic.BasicAuthProvider;
import com.feth.play.module.pa.user.AuthUser;
import play.Application;
import play.data.Form;
import play.mvc.Http;
import play.mvc.Result;
import service.PlayAuthenticateLocal;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static play.data.Form.form;

public class LdapAuthProvider extends BasicAuthProvider {

    private static final String PROVIDER_KEY = "ldap";
    public static final Form<LdapLogin> LOGIN_FORM = form(LdapLogin.class);

    @Inject
    public LdapAuthProvider(Application app) {
        super(app);
    }

    public static Result handleLogin(final Http.Context ctx, LdapConfig ldapConfig) {
        return PlayAuthenticateLocal.handleAuthentication(PROVIDER_KEY, ctx, ldapConfig);
    }

    @Override
    protected AuthUser authenticateUser(String username, String password) {
        return null;
    }

    public static class LdapLogin {
        String username;
        String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Override
    public Object authenticate(Http.Context context, Object payload) throws AuthException {

        Http.Context.current.set(context);
        final Form<LdapLogin> filledForm = LOGIN_FORM.bindFromRequest();
        LdapLogin ldapLogin = filledForm.get();
        LdapConfig ldapConfig = (LdapConfig) payload;
        //String base = "dc=example,dc=com";
        String base = ldapConfig.getDc();
        String dn = "uid=" + ldapLogin.getUsername() + "," + base;
        //String ldapURL = "ldap://ldap.forumsys.com:389";
        String ldapURL = ldapConfig.getUrl() + ":" + ldapConfig.getPort();
        // Setup environment for authenticating
        Hashtable<String, String> environment =
                new Hashtable<String, String>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, ldapURL);
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        environment.put(Context.SECURITY_PRINCIPAL, dn);
        environment.put(Context.SECURITY_CREDENTIALS, ldapLogin.getPassword());
        boolean auth;
        try
        {
            DirContext authContext =
                    new InitialDirContext(environment);
            auth = true;

            // user is authenticated

        } catch (NamingException ex)
        {
            throw new AuthException("Wrong Password or Username");
        }

        if (auth) {
            return new AuthUser() {
                private static final long serialVersionUID = 1L;

                @Override
                public String getId() {
                    return "1";
                }

                @Override
                public String getProvider() {
                    return "ldap";
                }
            };
        }
        return null;
    }

    public static class LdapConfig {
        private String url;
        private int port;
        private String dc;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getDc() {
            return dc;
        }

        public void setDc(String dc) {
            this.dc = dc;
        }
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }

}
