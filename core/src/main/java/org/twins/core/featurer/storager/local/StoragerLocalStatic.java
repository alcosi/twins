package org.twins.core.featurer.storager.local;

import io.github.breninsul.springHttpMessageConverter.inputStream.ContentDispositionType;
import io.github.breninsul.springHttpMessageConverter.inputStream.ContentTypeResolvingInputStreamResponseKt;
import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponse;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.storager.AddedFileKey;
import org.twins.core.featurer.storager.StoragerAbstractChecked;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;


@Component
@Featurer(id = FeaturerTwins.ID_2901,
        name = "StoragerLocalStaticController",
        description = "Service to save files in local file system and return their URL as '$selfHostDomainBaseUri'+'public/resource/{id}/v1'")
@Slf4j
public class StoragerLocalStatic extends StoragerAbstractChecked {
    @Value("${server.servlet.context-path:}")
    protected String contextPath;

    @FeaturerParam(name = "downloadExternalFileConnectionTimeout",
            description = "If the File is added as external URI, it should be downloaded first.\nSo this params sets timout time in milliseconds for such download request.\n",
            optional = true,
            defaultValue = "60000",
            exampleValues = {"60000", "1000"}
    )
    public static final FeaturerParamInt downloadExternalFileConnectionTimeout = new FeaturerParamInt("downloadExternalFileConnectionTimeout");

    @FeaturerParam(name = "baseLocalPath", description = "Base local path of directory where files should be saved.\nPlaceholders {domainId} and {businessAccountId} can be used to make domain/account relevant path.",
            optional = true,
            defaultValue = "/opt/resources/{domainId}/{businessAccountId}",
            exampleValues = {"/opt/resource/", "/opt/resources/{domainId}/{businessAccountId}"}
    )
    public static final FeaturerParamString baseLocalPath = new FeaturerParamString("baseLocalPath");

    @Override
    protected Duration getDownloadExternalFileConnectionTimeout(HashMap<String, String> params) throws ServiceException {
        Properties properties = extractProperties(params, false);
        Integer extracted = downloadExternalFileConnectionTimeout.extract(properties);
        return Duration.ofMillis(extracted == null || extracted < 1 ? 60000 : extracted.longValue());
    }

    @Override
    public String getFileControllerUri(HashMap<String, String> params) throws ServiceException {
        String controllerPath = "public/resource/{id}/v1";
        Properties properties = extractProperties(params, false);
        String urlDomain = addSlashAtTheEndIfNeeded(selfHostDomainBaseUri.extract(properties));
        return urlDomain + removeDoubleSlashes(addSlashAtStartIfNeeded(contextPath) + addSlashAtStartIfNeeded(controllerPath));
    }

    @Override
    protected AddedFileKey addFileInternal(String fileKey, InputStream fileStream, String mimeType, HashMap<String, String> params) throws ServiceException {
        String filePath = fileKey;
        try {
            long fileSize;
            Path path = Paths.get(filePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent()); // Ensure parent directories exist
            }
            try (InputStream is = fileStream) {
                fileSize = Files.copy(is, path);
            } // Write the content to the file
            log.info("Successfully created and saved file at: {}", filePath);

            return new AddedFileKey(fileKey, fileSize, Collections.emptyList());
        } catch (Exception e) {
            log.error("Error while creating and saving file: {}", filePath, e);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to create or save file");
        }
    }

    @Override
    public InputStreamResponse getFileAsStream(String fileKey, HashMap<String, String> params) throws ServiceException {
        String filePath = fileKey;
        try {
            return ContentTypeResolvingInputStreamResponseKt.toFileResource(new File(filePath), null, true, ContentDispositionType.INLINE, true);
        } catch (Exception e) {
            log.error("Error while retrieving file as stream: {}", filePath, e);
            throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Unable to get resource");
        }
    }


    @Override
    public void deleteFile(String fileKey, HashMap<String, String> params) throws ServiceException {
        String resourcePath = fileKey;
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

    @Override
    public String generateFileKey(UUID fileId, HashMap<String, String> params) throws ServiceException {
        Properties properties = extractProperties(params, false);
        String domainId = addSlashAtTheEndIfNeeded(getDomainId().map(UUID::toString).orElse("defaultDomain"));
        String businessAccount = addSlashAtTheEndIfNeeded(getBusinessAccountId().map(UUID::toString).orElse("defaultBusinessAccount"));
        String baseLocalPathString = addSlashAtTheEndIfNeeded(baseLocalPath.extract(properties));
        String key = baseLocalPathString.replace("{domainId}", domainId).replace("{businessAccountId}", businessAccount) + fileId.toString();
        String removedDoubleSlashes = removeDoubleSlashes(key);
        return removedDoubleSlashes;
    }
}
