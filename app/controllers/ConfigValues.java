package controllers;

import io.swagger.annotations.ApiModel;

import java.util.HashMap;


@ApiModel(value="ConfigValues")
public class ConfigValues {

    public ConfigValues() {
    }

    public HashMap<String,String> configs = new HashMap<>();

    public ConfigValues(HashMap<String, String> configs) {
        this.configs = configs;
    }

    public HashMap<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(HashMap<String, String> configs) {
        this.configs = configs;
    }
}
