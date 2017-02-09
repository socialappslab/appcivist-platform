package controllers;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;
import play.i18n.Messages;

@ApiModel(value="PasswordChange")
public class PasswordChange {
    @Constraints.MinLength(5)
    @Constraints.Required
    public String password;

    @Constraints.MinLength(5)
    @Constraints.Required
    public String repeatPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }

    public String validate() {
        if (password == null || !password.equals(repeatPassword)) {
            return Messages
                    .get("playauthenticate.change_password.error.passwords_not_same");
        }
        return null;
    }
}
