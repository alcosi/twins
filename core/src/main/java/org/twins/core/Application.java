package org.twins.core;

import org.cambium.common.exception.ServiceException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.twins.core.service.notification.NotificationService;

@SpringBootApplication(scanBasePackages = {"org.twins", "org.cambium"},
        exclude = {SecurityAutoConfiguration.class })
@EnableJpaRepositories({"org.twins.core.dao", "org.cambium.featurer.dao", "org.cambium.i18n.dao", "org.twins.face.dao"})
@EntityScan({"org.twins.core.dao", "org.cambium.featurer.dao", "org.cambium.i18n.dao", "org.twins.face.dao"})
@EnableCaching
@EnableScheduling
public class Application {
    public static void main(String[] args) throws ServiceException {
        var c = SpringApplication.run(Application.class, args);
        NotificationService bean = c.getBean(NotificationService.class);
        bean.collect();
    }

}
