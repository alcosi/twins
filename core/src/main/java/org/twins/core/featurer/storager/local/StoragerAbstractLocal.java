package org.twins.core.featurer.storager.local;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.twins.core.featurer.storager.StoragerAbstractChecked;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;


@Slf4j
public abstract class StoragerAbstractLocal extends StoragerAbstractChecked {
    @FeaturerParam(name = "baseLocalPath", description = "Base local path of directory where to save files")
    public static final FeaturerParamString baseLocalPath = new FeaturerParamString("baseLocalPath");

    @Override
    public String generateFileKey(UUID fileId, HashMap<String, String> params) throws ServiceException {
        Properties properties = extractProperties(params, false);
        String baseLocalPathString = addSlashAtTheEndIfNeeded(baseLocalPath.extract(properties));
        String businessDomain = addSlashAtTheEndIfNeeded(getDomainId().map(UUID::toString).orElse("defaultDomain"));
        String businessAccount = addSlashAtTheEndIfNeeded(getBusinessAccountId().map(UUID::toString).orElse("defaultBusinessAccount"));
        return baseLocalPathString + businessDomain + businessAccount + fileId;
    }

    @Override
    protected void addFileInternal(String fileKey, InputStream fileStream, HashMap<String, String> params) throws ServiceException {
        String filePath = fileKey;
        try {
            Path path = Paths.get(filePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent()); // Ensure parent directories exist
            }
            try (InputStream is = fileStream) {
                Files.copy(is, path);
            } // Write the content to the file
            log.info("Successfully created and saved file at: {}", filePath);
        } catch (Exception e) {
            log.error("Error while creating and saving file: {}", filePath, e);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to create or save file");
        }
    }

    @Override
    public InputStream getFileAsStream(String fileKey, HashMap<String, String> params) throws ServiceException {
        String filePath = fileKey;
        try {
            return Files.newInputStream(Paths.get(filePath));
        } catch (Exception e) {
            log.error("Error while retrieving file as stream: {}", filePath, e);
            throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Unable to get resource");
        }
    }


    @Override
    public void deleteFile(String fileKey, HashMap<String, String> params) throws ServiceException {
        String resourcePath = getLocalPath(params) + fileKey;
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
        Properties properties = extractProperties(params, false);
        String localPath = baseLocalPath.extract(properties);
        return addSlashAtTheEndIfNeeded(localPath);
    }


}
