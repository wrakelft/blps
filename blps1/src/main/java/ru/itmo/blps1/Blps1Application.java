package ru.itmo.blps1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.itmo.blps1.config.properties.Bitrix24Properties;

@SpringBootApplication
@EnableKafka
@EnableScheduling
@EnableConfigurationProperties(Bitrix24Properties.class)
public class Blps1Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Blps1Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Blps1Application.class);
    }
}