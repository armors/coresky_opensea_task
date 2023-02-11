package com.coresky.web;

import com.coresky.web.task.impl.OpenSeaStream;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.URISyntaxException;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@MapperScan("com.coresky.web.mapper")
@MapperScan("mapper")
public class WebApplication {

	public static void main(String[] args) throws URISyntaxException {
		ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(WebApplication.class, args);
		OpenSeaStream openSeaStream = configurableApplicationContext.getBean(OpenSeaStream.class);
		openSeaStream.startTask();
	}
}
