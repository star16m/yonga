package com.yonga.auc.config;

import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;

import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@org.springframework.context.annotation.Configuration
@EnableCaching
@Profile("production")
public class CacheConfig {

	@Bean
	public JCacheManagerCustomizer cacheManagerCustomizer() {
		return cm -> {
			Configuration<Object, Object> cacheConfiguration = createCacheConfiguration();
			cm.createCache("category", cacheConfiguration);
			cm.createCache("maker", cacheConfiguration);
			cm.createCache("productType", cacheConfiguration);
			cm.createCache("workLog", cacheConfiguration);
			cm.createCache("config", cacheConfiguration);
		};
	}

	private Configuration<Object, Object> createCacheConfiguration() {
		return new MutableConfiguration<>().setStatisticsEnabled(true);
	}
}
