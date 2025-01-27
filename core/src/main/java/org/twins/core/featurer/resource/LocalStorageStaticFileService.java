package org.twins.core.featurer.resource;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;

@Featurer(id = FeaturerTwins.ID_2902,
        name = "LocalStorageStaticFileService",
        description = "Service to save files in local file system and return them them as nginx static resource after that")
@Slf4j
public class LocalStorageStaticFileService extends AbstractLocalStorageFileService {
    @Override
    protected String getFileControllerUri(HashMap<String, String> params,HashMap<String, Object> context) throws ServiceException {
        String businessAccount=addSlashAtTheEndIfNeeded(context.containsKey("businessAccount")?context.get("businessAccount").toString():"defaultBusinessAccount");
        String businessDomain=addSlashAtTheEndIfNeeded(context.containsKey("domain")?context.get("domain").toString():"defaultDomain");
        Properties properties = featurerService.extractProperties(this, params, context);
        String relativePath = addSlashAtTheEndIfNeeded(relativeFileUri.extract(properties));
        String urlDomain = addSlashAtTheEndIfNeeded(selfHostDomainBaseUri.extract(properties));
        return urlDomain +businessDomain+businessAccount+ relativePath;
    }
}
