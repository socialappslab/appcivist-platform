package providers;

import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.wwwauth.basic.BasicAuthProvider;
import com.feth.play.module.pa.user.AuthUser;
import models.Assembly;
import models.Config;
import models.Membership;
import models.User;
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
import java.util.Enumeration;
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

    public static List<LdapAuthUser> getMemberLdapUsers(Assembly assembly, String cnsearch) throws AuthException, NamingException {
        LdapConfig ldapConfig = new LdapConfig();
        ldapConfig.setAssembly(assembly);
        setConfig(ldapConfig, true);
        return getLdapUsers(ldapConfig, cnsearch);

    }

    private static List<LdapAuthUser> getLdapUsers(LdapConfig ldapConfig, String cnserach) throws NamingException {
        String ldapURL = ldapConfig.getUrl() + ":" + ldapConfig.getPort();
        Hashtable<String, String> environment =
                new Hashtable<String, String>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, ldapURL);
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        environment.put(Context.SECURITY_PRINCIPAL, ldapConfig.getAdminDN());
        environment.put(Context.SECURITY_CREDENTIALS, ldapConfig.getAdminpass());
        DirContext context =
                new InitialDirContext(environment);
        SearchControls searchCtrls = new SearchControls();
        searchCtrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtrls.setCountLimit(50);
        String[] attributes = { "cn", "mail" };
        searchCtrls.setReturningAttributes(attributes);
        String filter = "&(objectClass=*)(accountstatus=active)";
        if(cnserach != null) {
            filter = "("+filter+"(cn=*"+cnserach+"*))";
        } else {
            filter = "("+filter+")";
        }
        NamingEnumeration values = context.search(ldapConfig.getDc(),filter,searchCtrls);
        List<LdapAuthUser>  aRet = new ArrayList<>();
        while (values.hasMoreElements())
        {
            LdapAuthUser user = new LdapAuthUser();

            SearchResult result = (SearchResult) values.next();
            Attributes attribs = result.getAttributes();

            if (null != attribs)
            {
                Attribute cn =  attribs.get("cn");
                if (cn == null) {
                   user.setCn(null);
                } else {
                    user.setCn(String.valueOf(cn.getAll().next()));
                }
                Attribute mail = attribs.get("mail");
                if (mail == null) {
                    user.setMail(null);
                } else {
                    String email = String.valueOf(mail.getAll().next());
                    user.setMail(email);

                    // add user information if this user is already a member of the assembly
                    try {
                        User appcivistUser = User.findByEmail(email);
                        if(appcivistUser!=null) {
                            user.setId(appcivistUser.getUserId()+"");
                        }
                    } catch (Exception e) {
                        Logger.info("Ignoring error while searching ldap user email: "+e.getMessage());
                    }
                }
            }
            aRet.add(user);
        }
        Logger.info(aRet.size() + " users found");
        context.close();
        return aRet;

    }


    private static void setConfig(LdapConfig ldapConfig, Boolean loadAdmin) throws AuthException {
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
            if(loadAdmin) {
                if (config.getKey().equals(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_ADMIN_DN)) {
                    ldapConfig.setAdminDN(config.getValue());
                }
                if (config.getKey().equals(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_ADMIN_PASS)) {
                    ldapConfig.setAdminpass(config.getValue());
                }
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
        setConfig(ldapConfig, false);
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
            ldapAuthUser.setUser(ldapLogin.getUsername());
            if(ldapAuthUser.getId() == null) {
                ldapAuthUser.setId(ldapLogin.getUsername()+"@ldap.com");
            }
            ldapAuthUser.setAssembly(ldapConfig.getAssembly());
            Logger.info("User CN " + ldapAuthUser.getCn());
            return ldapAuthUser;
        } catch (NamingException ex)
        {
            Logger.error("Ldap auth error "  + ex);

            if (ex.getRootCause()!=null) {
                if (ex.getRootCause().getMessage() == "Operation timed out") {
                    throw new AuthException("LDAP Server did not respond");
                } else {
                    throw new AuthException(ex.getRootCause().getMessage());
                }
            } else {
                throw new AuthException("Wrong Password or Username");
            }
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

    public static class LdapAuthUser extends AuthUser {

        private String id;
        private String user;
        private String mail;
        private String cn;
        private Assembly assembly;
        final static long SESSION_TIMEOUT = 24 * 14 * 3600;
        private long expiration;

        LdapAuthUser() {
            expiration = System.currentTimeMillis() + 1000 * SESSION_TIMEOUT;
        }

        @Override
        public long expires() {
            return expiration;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

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
        private String adminpass;
        private String adminDN;


        public String getAdminpass() {
            return adminpass;
        }

        public void setAdminpass(String adminpass) {
            this.adminpass = adminpass;
        }

        public String getAdminDN() {
            return adminDN;
        }

        public void setAdminDN(String adminDN) {
            this.adminDN = adminDN;
        }

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
