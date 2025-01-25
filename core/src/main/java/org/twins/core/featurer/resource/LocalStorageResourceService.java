package org.twins.core.featurer.resource;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.twins.core.featurer.FeaturerTwins;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;

@Featurer(id = FeaturerTwins.ID_2901,
        name = "LocalStorageResourceService",
        description = "Service to save resources (files) in local file system")
@Slf4j
public class LocalStorageResourceService extends StorageResourceService  {


    @Override
    InputStream getResourceAsStream(String resourceKey, HashMap<String, String> params) {
        return null;
    }

    @Override
    URI getResourceUri(String resourceKey, HashMap<String, String> params) throws ServiceException {
        return null;
    }

    @Override
    void deleteResource(String resourceKey, HashMap<String, String> params) throws ServiceException {

    }
}
