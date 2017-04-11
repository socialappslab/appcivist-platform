package controllers;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;
import play.i18n.Messages;

@ApiModel(value="ResetConfig")
public class ResetConfig {

    @Constraints.Required
    @Constraints.Email
    public String email;

    public String configUrl;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getConfigUrl() {
        return configUrl;
    }

    public void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
    }
}
