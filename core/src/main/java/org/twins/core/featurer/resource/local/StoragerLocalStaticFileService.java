package org.twins.core.featurer.resource.local;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2902,
        name = "StoragerLocalStaticFileService",
        description = "Service to save files in local file system and return them them as nginx static resource after that")
@Slf4j
public class StoragerLocalStaticFileService extends StoragerAbstractLocalFileService {

    @Override
    public String getFileControllerUri(HashMap<String, String> params) throws ServiceException {
        String businessDomain = getDomainId().map(UUID::toString).orElse("defaultDomain");
        String businessAccount = getBusinessAccountId().map(UUID::toString).orElse("defaultDomain");
        Properties properties = extractProperties(params, false);
        String relativePath = relativeFileUri.extract(properties);
        String urlDomain = addSlashAtTheEndIfNeeded(selfHostDomainBaseUri.extract(properties));
        return urlDomain + businessDomain + businessAccount + relativePath;
    }


}
