package utils.services;

import enums.ContributionStatus;
import enums.PeerDocStatus;
import enums.ResourceTypes;
import exceptions.PeerdocServerError;
import models.Contribution;
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
        Logger.debug("Peerdoc Server response: "+ response.asJson().toString());
        String peerDocUrl = getPeerDocServerUrl();
        if (response.asJson().get("path") != null) {
            String responsePath = response.asJson().get("path").asText();
            return peerDocUrl + responsePath;
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

    public void publish(Resource resource) throws NoSuchPaddingException, UnsupportedEncodingException,
            InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, HashGenerationException {
        String documentId = resource.getUrlAsString().split("document/")[1];
        String userEncrypted = encrypt();
        WSRequest holder = getWSHolder("/document/publish/"+documentId+"?user="+userEncrypted);
        F.Promise<WSResponse> promise = wsSend(holder);
        promise.get(DEFAULT_TIMEOUT);
    }

    public void changeStatus(Contribution contribution, ContributionStatus status) throws NoSuchPaddingException, UnsupportedEncodingException,
            InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, HashGenerationException, PeerdocServerError{
        Resource resource = null;
        if(contribution.getExtendedTextPad() != null && contribution.getExtendedTextPad().getResourceType().equals(ResourceTypes.PEERDOC)) {
            resource = contribution.getExtendedTextPad();
        }
        if(resource == null) {
            Logger.info("Contribution "+ contribution.getContributionId()+" does not have a PEERDOC. Changing status as usual.");
            return;
        }
        ContributionStatus currentStatus = contribution.getStatus();

        String documentId = resource.getUrlAsString().split("document/")[1];
        String userEncrypted = encrypt();
        WSRequest holder = getWSHolder("/document/share/"+documentId+"?user="+userEncrypted);
        Map<String, Boolean> peerDocVisibility = new HashMap<>();
        switch (status) {
            case PUBLIC_DRAFT:
                if (!currentStatus.equals(ContributionStatus.PUBLISHED)) {
                    peerDocVisibility.put("visibility", true);
                } else {
                    throw new PeerdocServerError("Published proposals can go back to DRAFT statuses");
                }
                break;
            case DRAFT:
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

        holder.setBody(Json.toJson(peerDocVisibility));
        F.Promise<WSResponse> promise = wsSend(holder);
        promise.get(DEFAULT_TIMEOUT);
    }

    public String encrypt() throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException,
            UnsupportedEncodingException, HashGenerationException {
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
        userPeerDoc.setNonce(Base64.getEncoder().encodeToString(nonce));
        return Json.toJson(userPeerDoc).toString();
    }

    private class UserPeerDoc {
        String avatar;
        String username;
        String email;
        Long id;
        String nonce;

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
