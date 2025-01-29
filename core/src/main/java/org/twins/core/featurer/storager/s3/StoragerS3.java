package org.twins.core.featurer.storager.s3;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

@Component
@Featurer(id = FeaturerTwins.ID_2904,
        name = "StoragerS3",
        description = "Service to save files to S3 and serve in controller")
@Slf4j
public class StoragerS3 extends StoragerAbstractS3 {


}
