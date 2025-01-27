package org.twins.core.featurer.resource.s3;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2904,
        name = "S3StorageFileControllerService",
        description = "Service to save files to S3 and serve in controller")
@Slf4j
public class S3StorageFileControllerService extends AbstractS3StorageFileService {

    @Override
    public String getFileControllerUri(HashMap<String, String> params, HashMap<String, Object> context) throws
            ServiceException {
        Properties properties = featurerService.extractProperties(this, params, context);
        String relativePath = addSlashAtTheEndIfNeeded(relativeFileUri.extract(properties));
        String urlDomain = addSlashAtTheEndIfNeeded(selfHostDomainBaseUri.extract(properties));
        return urlDomain + relativePath;
    }

}
