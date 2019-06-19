package com.yonga.auc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.category.AuctionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ConfigService {

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public Config getConfig(String group, String key) {
        return this.configRepository.findConfig(group, key);
    }

    public List<Config> getConfig(String group) {
        return this.configRepository.getConfig(group);
    }

    @PostConstruct
    public void setConstants() {
        String title = getConfigValue("CONFIG", "TITLE");
        String welcome = getConfigValue("CONFIG", "WELCOME");
        String auctionInfo = getConfigValue("AUCTION", "INFO");
        ConfigConstants.APPLICATION_TITLE = title != null ? title : ConfigConstants.APPLICATION_TITLE;
        ConfigConstants.APPLICATION_WELCOME = welcome != null ? welcome : ConfigConstants.APPLICATION_WELCOME;
//        ConfigConstants.AUCTION_INFO = new AuctionInfo();
        if (YongaUtil.isNotEmpty(auctionInfo)) {
            try {
                ConfigConstants.AUCTION_INFO = objectMapper.readValue(auctionInfo, AuctionInfo.class);
            } catch (IOException e) {
                log.error("옥션 정보 초기화 중 에러가 발생하였습니다.", e);
            }
        }
    }
    @Cacheable("config")
    public String getConfigValue(String group, String key) {
        Config config = getConfig(group, key);
        if (config != null) {
            return config.getValue();
        }
        return null;
    }
//
//    @CacheEvict(value = "config", allEntries = true)
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
