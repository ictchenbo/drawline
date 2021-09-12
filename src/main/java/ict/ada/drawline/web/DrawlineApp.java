package ict.ada.drawline.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "ict.ada.drawline.web")
@SpringBootApplication
public class DrawlineApp {
    public static void main(String[] args) {
        SpringApplication.run(DrawlineApp.class, args);
    }
}

