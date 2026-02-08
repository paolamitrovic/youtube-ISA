package com.example.videoconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VideoConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideoConsumerApplication.class, args);
		System.out.println("Video Consumer Application started!");
		System.out.println("Listening for video upload events...");
	}

}
