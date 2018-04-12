package providers;

import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.wwwauth.basic.BasicAuthProvider;
import com.feth.play.module.pa.user.AuthUser;
import models.Assembly;
import models.Config;
import models.transfer.TransferResponseStatus;
import play.Application;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import service.PlayAuthenticateLocal;
import utils.GlobalDataConfigKeys;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
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


    private void setConfig(LdapConfig ldapConfig) throws AuthException {
        for(Config config: ldapConfig.getAssembly().getConfigs()) {
            Logger.info(config.getKey());
            if (config.getKey().equals(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_SERVER)) {
                ldapConfig.setUrl(config.getValue());
            }
            if (config.getKey().equals(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_PORT)) {
                ldapConfig.setPort(Integer.valueOf(config.getValue()));
            }
            if (config.getKey().equals(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_DN)) {
                ldapConfig.setDc(config.getValue());
            }
        }
        if (ldapConfig.getPort() == 0 || ldapConfig.getUrl() == null) {
            throw new AuthException("No ldap configuration found in the assembly");
        }

    }

    @Override
    public Object authenticate(Http.Context context, Object payload) throws AuthException {

        Http.Context.current.set(context);
        final Form<LdapLogin> filledForm = LOGIN_FORM.bindFromRequest();
        LdapLogin ldapLogin = filledForm.get();
        LdapConfig ldapConfig = (LdapConfig) payload;
        setConfig(ldapConfig);
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
        try
        {
            DirContext authContext =
                    new InitialDirContext(environment);
            LdapAuthUser ldapAuthUser = new LdapAuthUser();
            getCnAndMail(authContext, ldapLogin.getUsername(), base, ldapAuthUser);
            ldapAuthUser.setId(ldapAuthUser.getMail());
            ldapAuthUser.setAssembly(ldapConfig.getAssembly());
            Logger.info("User CN " + ldapAuthUser.getCn());
            return ldapAuthUser;
        } catch (NamingException ex)
        {
            Logger.error("Ldap auth error "  + ex);
            throw new AuthException("Wrong Password or Username");
        }

    }

    private void getCnAndMail(DirContext ctx, String username, String base, LdapAuthUser user) throws NamingException {
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration results = ctx.search(base, "(uid="+username+")", constraints);
        String MY_ATTRS[] = { "cn", "mail" };
        while (results != null && results.hasMore()) {
            SearchResult sr = (SearchResult) results.next();
            String dn = sr.getName() + ", " + base;
            Logger.info("Distinguished Name is " + dn);
            Attributes ar = ctx.getAttributes(dn, MY_ATTRS);
            Attribute cn = ar.get("cn");
            if (cn == null) {
                user.setCn(null);
            } else {
                user.setCn(String.valueOf(cn.getAll().nextElement()));
            }
            Attribute mail = ar.get("mail");
            if (mail == null) {
                user.setMail(null);
            } else {
                user.setMail(String.valueOf(mail.getAll().nextElement()));
            }

        }
    }

    public class LdapAuthUser extends AuthUser {

        private String id;
        private String mail;
        private String cn;
        private Assembly assembly;

        public Assembly getAssembly() {
            return assembly;
        }

        public void setAssembly(Assembly assembly) {
            this.assembly = assembly;
        }

        public String getMail() {
            return mail;
        }

        public void setMail(String mail) {
            this.mail = mail;
        }

        public String getCn() {
            return cn;
        }

        public void setCn(String cn) {
            this.cn = cn;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getProvider() {
            return PROVIDER_KEY;
        }

        public void setId(String id){
            this.id = id;
        }


    }

    public static class LdapConfig {
        private String url;
        private int port;
        private String dc;
        private Assembly assembly;


        public Assembly getAssembly() {
            return assembly;
        }

        public void setAssembly(Assembly assembly) {
            this.assembly = assembly;
        }

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
