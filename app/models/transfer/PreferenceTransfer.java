package models.transfer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ggaona on 22/8/17.
 */
public class PreferenceTransfer {
    String userId;
    String defaultService;
    List<AutoSuscriptionTransfer> autoSusbcriptions = new ArrayList<AutoSuscriptionTransfer>();

    public PreferenceTransfer(String uuid){
        super();
        this.userId = uuid;
        this.autoSusbcriptions = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDefaultService() {
        return defaultService;
    }

    public void setDefaultService(String defaultService) {
        this.defaultService = defaultService;
    }

    public List<AutoSuscriptionTransfer> getAutoSusbcriptions() {
        return autoSusbcriptions;
    }

    public void setAutoSusbcriptions(List<AutoSuscriptionTransfer> autoSusbcriptions) {
        this.autoSusbcriptions = autoSusbcriptions;
    }


}
