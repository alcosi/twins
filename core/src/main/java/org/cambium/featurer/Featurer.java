package org.cambium.featurer;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
public abstract class Featurer {
    @Autowired
    public FeaturerService featurerService;

    @PostConstruct
    private void postConstruct() {
        //check and update feature components in database
    }

}
