package utils.services;

import com.fasterxml.jackson.databind.JsonNode;
import enums.ContributionStatus;
import enums.ResourceTypes;
import exceptions.PeerdocServerError;
import models.Contribution;
import models.NonMemberAuthor;
import models.Resource;
import models.User;
import play.Logger;
import play.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import utils.security.HashGenerationException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

import static delegates.ContributionsDelegate.createResourceAndUpdateContribution;

/**
 * Created by yohanna on 25/03/18.
 */
public class PeerDocWrapper {

    private String peerDocServerUrl = "";
    private String keyHex = "";
    private User user;
    // AES-GCM parameters
    private static final String ALGORITHM_NAME = "AES/GCM/NoPadding";
    private static final int NONCE_LENGTH = 22;
    private static final int IV_LENGTH = 12;
    private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";
    private static final int AUTH_TAG_LENGTH = 16;
    private static final long DEFAULT_TIMEOUT = 10000;

    /**
     * Communicates with a PeerDoc Server and creates a PAD, storing the main URL of the peerdoc document
     * as part of a contribution
     *
     * @param c
     * @param resourceSpaceConfigsUUID
     * @return
     * @throws NoSuchPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws MalformedURLException
     * @throws HashGenerationException
     * @throws PeerdocServerError
     */
    public Map<String, String> createPad(Contribution c, UUID resourceSpaceConfigsUUID) throws NoSuchPaddingException, UnsupportedEncodingException,
            InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, MalformedURLException, HashGenerationException, PeerdocServerError {

        String padId = UUID.randomUUID().toString();
        String url = getPeerDocUrl();
        Logger.info("Creating PEERDOC Resource ("+padId+") with URL =  con"+url);
        createResourceAndUpdateContribution(padId, null, url, resourceSpaceConfigsUUID, c,
                ResourceTypes.PEERDOC, false, null);
        Map<String, String> aRet = new HashMap<>();
        aRet.put("path", url);
        return aRet;
    }

    private String getPeerDocUrl() throws NoSuchPaddingException, UnsupportedEncodingException, InvalidKeyException,
            NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, HashGenerationException, PeerdocServerError {
        String userEncrypted = encrypt();
        WSRequest holder = getWSHolder("/document?user="+userEncrypted);
        Logger.info("NOTIFICATION: Getting document URL in PeerDoc: " + holder.getUrl());
        F.Promise<WSResponse> promise = wsSend(holder);
        WSResponse response = promise.get(DEFAULT_TIMEOUT);
        Logger.debug("PEERDOC: response from server => "+response.toString());
        com.fasterxml.jackson.databind.JsonNode jn = response.asJson();
        Logger.debug("Peerdoc Server response: "+ jn.toString());
        String peerDocUrl = getPeerDocServerUrl();
        Logger.debug("Reading path from Peerdoc response...");
        com.fasterxml.jackson.databind.JsonNode pathNode = jn.get("path");
        String path = pathNode.toString();
        path.trim();
        path = path.replace("\"","");
        Logger.debug("peerDocUrl = " + peerDocUrl);
        Logger.debug("Path = " + path);
        String peerdocPath = peerDocUrl + path;
        Logger.debug("Path = " + peerdocPath);
        if (path != null) {
            return peerDocUrl + path;
        } else {
            throw new PeerdocServerError("Response from PeerDoc was empty");
        }

    }

    public PeerDocWrapper(User user) {
        this.user = user;
        this.peerDocServerUrl = Play.application().configuration()
                .getString("appcivist.services.peerdoc.serverBaseUrl");
        this.keyHex =  Play.application().configuration()
                .getString("appcivist.services.peerdoc.keyHex");
    }

    public boolean publish(Resource resource) throws NoSuchPaddingException, UnsupportedEncodingException,
            InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, HashGenerationException {
        String documentId = resource.getUrlAsString().split("document/")[1];
        String userEncrypted = encrypt();
        WSRequest holder = getWSHolder("/document/publish/"+documentId+"?user="+userEncrypted);
        F.Promise<WSResponse> promise = wsSend(holder);
        WSResponse status = promise.get(DEFAULT_TIMEOUT);
        if(status.getStatus() == 200) {
            JsonNode response = status.asJson();
            if (response.get("status") != null && response.get("status").asText().equals("success")) {
                return true;
            } else {
                Logger.error("ERROR ON PEERDOC PUBLISH " + status.getStatus());
                Logger.error(status.asJson().toString());
                return false;
            }
        } else {
            Logger.error("ERROR ON PEERDOC PUBLISH " + status.getStatus());
            Logger.error(status.asJson().toString());
            return false;
        }
    }

    public JsonNode fork(Resource resource) throws NoSuchPaddingException, UnsupportedEncodingException,
            InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, HashGenerationException {

        String documentId = resource.getUrlAsString().split("document/")[1];
        String userEncrypted = encrypt();
        WSRequest holder = getWSHolder("/document/fork/"+documentId+"?user="+userEncrypted);
        F.Promise<WSResponse> promise = wsSend(holder);
        WSResponse status = promise.get(DEFAULT_TIMEOUT);
        if(status.getStatus() == 200) {
            JsonNode response = status.asJson();
            if (response.get("status") != null && response.get("status").asText().equals("success")) {
                return response;
            }
        }
        return null;

    }

    public String export(Resource resource) throws NoSuchPaddingException, UnsupportedEncodingException,
            InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, HashGenerationException {

        String documentId = resource.getUrlAsString().split("document/")[1];
        String userEncrypted = encrypt();
        WSRequest holder = getWSHolder("/document/export/"+documentId+"?user="+userEncrypted);
        F.Promise<WSResponse> promise = wsSend(holder);
        WSResponse status = promise.get(DEFAULT_TIMEOUT);
        if(status.getStatus() == 200) {
            Logger.info("PEERDOC EXPORT: 200 status ");
            JsonNode response = status.asJson();
            if (response.get("status") != null && response.get("status").asText().equals("success")) {
                return response.get("html").asText();
            }
        }
        return null;
    }



    public boolean merge(Contribution parent, Contribution children) throws NoSuchPaddingException, UnsupportedEncodingException,
            InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, HashGenerationException {

        Resource resource = getPeerdocByContribution(children);
        String documentId = resource.getUrlAsString().split("document/")[1];
        String userEncrypted = encrypt();
        WSRequest holder = getWSHolder("/document/merge/"+documentId+"?user="+userEncrypted);
        F.Promise<WSResponse> promise = wsSend(holder);
        WSResponse status = promise.get(DEFAULT_TIMEOUT*2);
        Logger.info("RESPONSE FROM PEERDOC " + status.getBody());
        Logger.info("RESPONSE STATUS FROM PEERDOC " + status.getStatus());
        return status.getStatus() == 200;

    }


    public void updatePeerdocPermissions(Contribution contribution) throws NoSuchPaddingException,
            UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
            HashGenerationException {
        Resource resource = getPeerdocByContribution(contribution);
        if(resource == null) {
            Logger.info("PEERDOC: Contribution "+ contribution.getContributionId()+" does not have a PEERDOC. Not updating permissions");
            return;
        }
        String documentId = resource.getUrlAsString().split("document/")[1];
        String userEncrypted = encrypt();
        WSRequest holder = getWSHolder("/document/share/"+documentId+"?user="+userEncrypted);
        Map<String, List<UserPeerDoc>> editObject = new HashMap<>();
        Map<String, Object> toSend = new HashMap<>();
        editObject.put("admin", new ArrayList<>());
        for(User user: contribution.getAuthors()) {
            editObject.get("admin").add(userToUserPeerdoc(user, null));
        }
        for(NonMemberAuthor nonMemberAuthor: contribution.getNonMemberAuthors()) {
            UserPeerDoc userPeerDoc = new UserPeerDoc();
            userPeerDoc.setEmail(nonMemberAuthor.getEmail());
            userPeerDoc.setId(nonMemberAuthor.getId());
            userPeerDoc.setLanguage(nonMemberAuthor.getLang());
        }
        toSend.put("token_users", editObject);
        holder.setBody(Json.toJson(toSend));
        F.Promise<WSResponse> promise = wsSend(holder);
        promise.get(DEFAULT_TIMEOUT);
    }

    private Resource getPeerdocByContribution(Contribution contribution) {
        Resource resource = null;
        if(contribution.getExtendedTextPad() != null && contribution.getExtendedTextPad().getResourceType().equals(ResourceTypes.PEERDOC)) {
            resource = contribution.getExtendedTextPad();
        }
        return resource;
    }

    public Boolean changeStatus(Contribution contribution, ContributionStatus status) throws NoSuchPaddingException, UnsupportedEncodingException,
            InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, HashGenerationException, PeerdocServerError{
        Resource resource = getPeerdocByContribution(contribution);

        if(resource == null) {
            Logger.info("PEERDOC: Contribution "+ contribution.getContributionId()+" does not have a PEERDOC. Changing status as usual.");
            return null;
        }
        ContributionStatus currentStatus = contribution.getStatus();

        String documentId = resource.getUrlAsString().split("document/")[1];
        String userEncrypted = encrypt();
        Logger.info("PEERDOC: preparing request to send...");
        WSRequest holder = getWSHolder("/document/share/"+documentId+"?user="+userEncrypted);
        Map<String, Boolean> peerDocVisibility = new HashMap<>();
        switch (status) {
            case PUBLIC_DRAFT:
            case FORKED_PUBLIC_DRAFT:
            case MERGED:
                if (!currentStatus.equals(ContributionStatus.PUBLISHED)) {
                    peerDocVisibility.put("visibility", true);
                } else {
                    throw new PeerdocServerError("Published proposals can go back to DRAFT statuses");
                }
                break;
            case DRAFT:
            case FORKED_PRIVATE_DRAFT:
                if (!currentStatus.equals(ContributionStatus.PUBLISHED)) {
                    peerDocVisibility.put("visibility", false);
                } else {
                    throw new PeerdocServerError("Published proposals can go back to DRAFT statuses");
                }
                break;
            default:
                holder =  getWSHolder("/document/publish/"+documentId+"?user="+userEncrypted);
                break;
        }

        Logger.info("PEERDOC: sending request with following data => "+peerDocVisibility.toString());
        Logger.info("PEERDOC url :" + holder.getUrl());
        if (!peerDocVisibility.isEmpty()) {
            holder.setBody(Json.toJson(peerDocVisibility));
        }

        F.Promise<WSResponse> promise = wsSend(holder);
        WSResponse pdStatus = promise.get(DEFAULT_TIMEOUT);
        if(pdStatus.getStatus() == 200) {
            JsonNode response = pdStatus.asJson();
            Logger.info("PEERDOC: STATUS 200 " + response.toString());
            if (response.get("status") != null && response.get("status").asText().equals("success")) {
                return true;
            }
        } else {
            Logger.error("ERROR ON PEERDOC PUBLISH " + pdStatus.getStatus());
            Logger.error(pdStatus.asJson().toString());
            return false;
        }
        return false;

    }

    public String encrypt() throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException,
            UnsupportedEncodingException, HashGenerationException {

        if(this.user == null) {
            return "";
        }

        byte [] key = hexStringToByteArray(getKeyHex());
        byte [] nonce = new byte[NONCE_LENGTH];
        byte [] iv = new byte[IV_LENGTH];

        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM).nextBytes(nonce);
        SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM).nextBytes(iv);
        String messagePlain = userToUserData(getUser(), nonce);
        byte [] message = messagePlain.getBytes("UTF-8");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH * Byte.SIZE, iv);
        Cipher c = Cipher.getInstance(ALGORITHM_NAME);
        c.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        byte [] cipherText = c.doFinal(message);
        int cipherLength = cipherText.length;
        byte [] authTag = Arrays.copyOfRange(cipherText, cipherLength - AUTH_TAG_LENGTH, cipherLength);
        cipherText = Arrays.copyOfRange(cipherText, 0, cipherLength - AUTH_TAG_LENGTH);
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherLength + authTag.length);
        byteBuffer.put(iv);
        byteBuffer.put(authTag);
        byteBuffer.put(cipherText);
        byte[] cipherMessage = byteBuffer.array();
        return (Base64.getEncoder().encodeToString(cipherMessage))
                .replaceAll("\\+","-")
                .replaceAll("/", "_")
                .replaceAll("=",".")
                .replaceAll("AAAAAAAAAAAAAAAAAAAAA", "");
    }

    private String userToUserData(User user, byte [] nonce) throws HashGenerationException {
        UserPeerDoc userPeerDoc = userToUserPeerdoc(user, nonce);
        return Json.toJson(userPeerDoc).toString();
    }

    private UserPeerDoc userToUserPeerdoc(User user, byte [] nonce) throws HashGenerationException {
        UserPeerDoc userPeerDoc = new UserPeerDoc();
        Logger.info(user.getEmail());
        if(user.getProfilePic() != null) {
            userPeerDoc.setAvatar(user.getProfilePic().getUrlAsString());
        } else {
            userPeerDoc.setAvatar(User.getDefaultProfilePictureURL(user.getEmail()));
        }
        userPeerDoc.setUsername(user.getUsername());
        userPeerDoc.setEmail(user.getEmail());
        userPeerDoc.setId(user.getUserId());
        if(nonce != null) {
            userPeerDoc.setNonce(Base64.getEncoder().encodeToString(nonce));
        }
        userPeerDoc.setLanguage(user.getLanguage());
        return userPeerDoc;
    }


    private class UserPeerDoc {
        String avatar;
        String username;
        String email;
        Long id;
        String nonce;
        String language;

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private WSRequest getWSHolder(String endpoint) {
        WSRequest holder = WS.url(getPeerDocServerUrl() + endpoint);
        holder.setMethod("POST");
        return holder;
    }

    private F.Promise<WSResponse> wsSend(WSRequest holder) {
        F.Promise<WSResponse> promise = holder.execute().map(
                new F.Function<WSResponse, WSResponse>() {
                    public WSResponse apply(WSResponse response) {
                        return response;
                    }
                });
        return promise;
    }

    public String getPeerDocServerUrl() {
        return peerDocServerUrl;
    }

    public void setPeerDocServerUrl(String peerDocServerUrl) {
        this.peerDocServerUrl = peerDocServerUrl;
    }

    public String getKeyHex() {
        return keyHex;
    }

    public void setKeyHex(String keyHex) {
        this.keyHex = keyHex;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
