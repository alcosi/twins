package org.twins.core.featurer.storager.local;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

@Component
@Featurer(id = FeaturerTwins.ID_2901,
        name = "StoragerLocalController",
        description = "Service to save files in local file system")
@Slf4j
public class StoragerLocal extends StoragerAbstractLocal {

}
