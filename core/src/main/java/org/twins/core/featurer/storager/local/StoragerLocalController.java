package org.twins.core.featurer.storager.local;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2901,
        name = "StoragerLocalController",
        description = "Service to save files in local file system")
@Slf4j
public class StoragerLocalController extends StoragerAbstractLocal {
    @Override
    public String getFileControllerUri(HashMap<String, String> params) throws ServiceException {
        Properties properties = extractProperties(params, false);
        String relativePath = relativeFileUri.extract(properties);
        String urlDomain = addSlashAtTheEndIfNeeded(selfHostDomainBaseUri.extract(properties));
        return urlDomain + relativePath;
    }
}
