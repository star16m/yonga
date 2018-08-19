package com.yonga;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;

@SpringBootApplication
@EnableScheduling
public class YongaApplication {
	public static void main(String[] args) {
		SpringApplication.run(YongaApplication.class, args);
	}

	@Bean
	public ScheduledExecutorFactoryBean scheduledExecutorService() {
		ScheduledExecutorFactoryBean bean = new ScheduledExecutorFactoryBean();
		bean.setPoolSize(1);
		return bean;
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
