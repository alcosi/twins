package org.twins.core.featurer.resource.local;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;
@Component
@Featurer(id = FeaturerTwins.ID_2902,
        name = "LocalStorageStaticFileService",
        description = "Service to save files in local file system and return them them as nginx static resource after that")
@Slf4j
public class LocalStorageStaticFileService extends AbstractLocalStorageFileService {


    @Override
    public String getFileControllerUri(HashMap<String, String> params,HashMap<String, Object> context) throws ServiceException {
        String businessAccount=addSlashAtTheEndIfNeeded(context.containsKey(CONTEXT_ATTRIBUTE_BUSINESS_ACCOUNT)?context.get(CONTEXT_ATTRIBUTE_BUSINESS_ACCOUNT).toString():"defaultBusinessAccount");
        String businessDomain=addSlashAtTheEndIfNeeded(context.containsKey(CONTEXT_ATTRIBUTE_BUSINESS_DOMAIN)?context.get(CONTEXT_ATTRIBUTE_BUSINESS_DOMAIN).toString():"defaultDomain");
        Properties properties = featurerService.extractProperties(this, params, context);
        String relativePath = addSlashAtTheEndIfNeeded(relativeFileUri.extract(properties));
        String urlDomain = addSlashAtTheEndIfNeeded(selfHostDomainBaseUri.extract(properties));
        return urlDomain +businessDomain+businessAccount+ relativePath;
    }


}
