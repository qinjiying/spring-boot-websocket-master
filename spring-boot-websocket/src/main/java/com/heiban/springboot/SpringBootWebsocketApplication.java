package com.heiban.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
public class SpringBootWebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebsocketApplication.class, args);
    }

}
