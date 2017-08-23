package models.transfer;

/**
 * Created by ggaona on 22/8/17.
 */
public class AutoSuscriptionTransfer {
    String key;
    String enabled;
    String identity;

    public AutoSuscriptionTransfer(String key){
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    String frequency;
}
