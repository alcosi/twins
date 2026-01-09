package org.twins.core.service.scheduler;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class SchedulerStarter implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        event.getApplicationContext().getBean(SchedulerService.class).init();
    }
}
