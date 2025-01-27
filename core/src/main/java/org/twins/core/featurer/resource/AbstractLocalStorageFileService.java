package org.twins.core.featurer.resource;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;


@Slf4j
abstract class AbstractLocalStorageFileService extends AbstractStorageFileService {
    @FeaturerParam(name = "baseLocalPath", description = "Base local path of directory where to save files")
    public static final FeaturerParamString baseLocalPath = new FeaturerParamString("baseLocalPath");
    @FeaturerParam(name = "relativeFileUri", description = "Relative uri of controller to provide files")
    public static final FeaturerParamString relativeFileUri = new FeaturerParamString("relativeFileUri");
    @Override
    public InputStream getFileAsStream(String fileKey, HashMap<String, String> params) throws ServiceException {
        String resourcePath=getLocalPath(params)+ fileKey;
        try {
            return Files.newInputStream(Paths.get(resourcePath));
        } catch (Exception e) {
            log.error("Error while retrieving resource as stream: {}", resourcePath, e);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to get resource");
        }
    }
  

    @Override
    public void deleteFile(String fileKey, HashMap<String, String> params) throws ServiceException {
        String resourcePath=getLocalPath(params)+ fileKey;
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
        return addSlashAtTheEndIfNeeded(localPath);
    }

    @NotNull
    protected String addSlashAtTheEndIfNeeded(String path) {
        if (!path.endsWith("/")){
            path = path +"/";
        }
        return path;
    }
}
