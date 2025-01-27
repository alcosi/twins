package org.twins.core.featurer.resource.local;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.twins.core.featurer.resource.AbstractStorageFileService;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
    protected void addFileInternal(String fileKey, InputStream fileStream, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        String filePath = getLocalPath(params) + fileKey;
        try {
            Path path = Paths.get(filePath);
            if (path.getParent()!=null) {
                Files.createDirectories(path.getParent()); // Ensure parent directories exist
            }
            Files.copy(fileStream, path); // Write the content to the file
            log.info("Successfully created and saved file at: {}", filePath);
        } catch (Exception e) {
            log.error("Error while creating and saving file: {}", filePath, e);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to create or save file");
        }
    }

    @Override
    public InputStream getFileAsStream(String fileKey, HashMap<String, String> params,HashMap<String,Object> context) throws ServiceException {
        String filePath=getLocalPath(params)+ fileKey;
        try {
            return Files.newInputStream(Paths.get(filePath));
        } catch (Exception e) {
            log.error("Error while retrieving file as stream: {}", filePath, e);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to get resource");
        }
    }
  

    @Override
    public void deleteFile(String fileKey, HashMap<String, String> params,HashMap<String,Object> context) throws ServiceException {
        String resourcePath=getLocalPath(params)+ fileKey;
        try {
            if (Files.deleteIfExists(Paths.get(resourcePath))) {
                log.info("Successfully deleted resource: {}", resourcePath);
            } else {
                log.warn("File does not exist or already deleted: {}", resourcePath);
            }
        } catch (Exception e) {
            log.error("Error while deleting file: {}", resourcePath, e);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to delete file");
        }
    }
    protected String getLocalPath(HashMap<String, String> params) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params, new HashMap<>());
        String localPath= baseLocalPath.extract(properties);
        return addSlashAtTheEndIfNeeded(localPath);
    }


}
