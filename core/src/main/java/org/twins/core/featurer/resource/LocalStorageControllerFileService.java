package org.twins.core.featurer.resource;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

@Featurer(id = FeaturerTwins.ID_2901,
        name = "LocalStorageControllerFileService",
        description = "Service to save files in local file system")
@Slf4j
public class LocalStorageControllerFileService extends AbstractLocalStorageFileService {

    @Override
    protected String getFileControllerUri(HashMap<String, String> params,HashMap<String, Object> context) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params, context);
        String relativePath = addSlashAtTheEndIfNeeded(relativeFileUri.extract(properties));
        String urlDomain = addSlashAtTheEndIfNeeded(selfHostDomainBaseUri.extract(properties));
        return urlDomain + relativePath;
    }

    @Override
    public String generateFileKey(UUID fileId, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params, context);
        String baseLocalPathString = addSlashAtTheEndIfNeeded(baseLocalPath.extract(properties));
        String businessDomain=addSlashAtTheEndIfNeeded(context.containsKey("domain")?context.get("domain").toString():"defaultDomain");
        return baseLocalPathString+businessDomain+fileId;
    }
}
