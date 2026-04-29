package com.resume.airesume;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync  // 新增：启用异步功能
public class AiResumeApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiResumeApplication.class, args);
	}

}
