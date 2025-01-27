package org.twins.core.featurer.resource.local;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;

@Featurer(id = FeaturerTwins.ID_2901,
        name = "LocalStorageControllerFileService",
        description = "Service to save files in local file system")
@Slf4j
public class LocalStorageControllerFileService extends AbstractLocalStorageFileService {
    @Override
    public String getFileControllerUri(HashMap<String, String> params,HashMap<String, Object> context) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params, context);
        String relativePath = addSlashAtTheEndIfNeeded(relativeFileUri.extract(properties));
        String urlDomain = addSlashAtTheEndIfNeeded(selfHostDomainBaseUri.extract(properties));
        return urlDomain + relativePath;
    }
}
