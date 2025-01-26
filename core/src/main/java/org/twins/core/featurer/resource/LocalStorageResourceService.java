package org.twins.core.featurer.resource;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.twins.core.featurer.FeaturerTwins;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

@Featurer(id = FeaturerTwins.ID_2901,
        name = "LocalStorageResourceService",
        description = "Service to save resources (files) in local file system")
@Slf4j
public class LocalStorageResourceService extends StorageResourceService  {
    @FeaturerParam(name = "baseLocalPath", description = "Base local path of directory where to save files")
    public static final FeaturerParamString baseLocalPath = new FeaturerParamString("baseLocalPath");

    @Override
    protected InputStream getResourceAsStream(String resourceKey, HashMap<String, String> params) throws ServiceException {
        String resourcePath=getLocalPath(params)+resourceKey;
        try {
            return Files.newInputStream(Paths.get(resourcePath));
        } catch (Exception e) {
            log.error("Error while retrieving resource as stream: {}", resourcePath, e);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to get resource");
        }
    }
  

    @Override
    protected void deleteResource(String resourceKey, HashMap<String, String> params) throws ServiceException {
        String resourcePath=getLocalPath(params)+resourceKey;
        try {
            if (Files.deleteIfExists(Paths.get(resourcePath))) {
                log.info("Successfully deleted resource: {}", resourcePath);
            } else {
                log.warn("Resource does not exist or already deleted: {}", resourcePath);
            }
        } catch (Exception e) {
            log.error("Error while deleting resource: {}", resourcePath, e);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to delete resource");
        }
    }
    protected String getLocalPath(HashMap<String, String> params) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params, new HashMap<>());
        String localPath= baseLocalPath.extract(properties);
        if (!localPath.endsWith("/")){
            localPath=localPath+"/";
        }
        return localPath;
    }
}
