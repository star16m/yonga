package com.yonga.auc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigService {

    @Autowired
    private ConfigRepository configRepository;


    public Config getConfig(String group, String key) {
        return this.configRepository.findConfig(group, key);
    }

    public List<Config> getConfig(String group) {
        return this.configRepository.getConfig(group);
    }

    public String getConfigValue(String group, String key) {
        Config config = getConfig(group, key);
        if (config != null) {
            return config.getValue();
        }
        return null;
    }

    public void setConfigValue(String group, String key, String value) {
        Config config = getConfig(group, key);
        if (config == null) {
            config = new Config();
            config.setGroup(group);
            config.setKey(key);
        }
        config.setValue(value);
        this.configRepository.save(config);
    }

    public boolean hasConfig(String group) {
        Integer count = this.configRepository.countGroupConfigs(group);
        return count != null && count.intValue() > 0;
    }
}
