package models.transfer;

import io.swagger.annotations.ApiModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Created by javierppf on 05/01/17.
 */
public class FrontEndError {

    private String user;
    private String path;
    private String message;

    public FrontEndError() {
    }

    public FrontEndError(String message) {
        this.user = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "FrontEndError{" +
                "user='" + user + '\'' +
                ", path='" + path + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}