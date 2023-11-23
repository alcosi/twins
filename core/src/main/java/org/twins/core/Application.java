package org.twins.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"org.twins", "org.cambium"},
        exclude = {SecurityAutoConfiguration.class })
@EnableJpaRepositories({"org.twins.core.dao", "org.cambium.featurer.dao", "org.cambium.i18n.dao"})
@EntityScan({"org.twins.core.dao", "org.cambium.featurer.dao", "org.cambium.i18n.dao"})
@EnableCaching
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
