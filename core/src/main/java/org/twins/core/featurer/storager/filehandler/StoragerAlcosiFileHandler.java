package org.twins.core.featurer.storager.filehandler;

import io.github.breninsul.io.service.stream.inputStream.CountedLimitedSizeInputStream;
import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamListOfMaps;
import org.cambium.featurer.params.FeaturerParamMap;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.twins.core.dto.rest.featurer.storager.filehandler.*;
import org.twins.core.enums.featurer.storager.Format;
import org.twins.core.enums.featurer.storager.StorageType;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.storager.AddedFileKey;
import org.twins.core.featurer.storager.StoragerAbstractChecked;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static org.cambium.common.util.UrlUtils.toURI;

@Deprecated
@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_2906,
        name = "StoragerAlcosiFileHandler",
        description = "Service to save or delete files via file-handler service"
)
@Slf4j
public class StoragerAlcosiFileHandler extends StoragerAbstractChecked {

    @FeaturerParam(
            name = "fileHandlerUri", description = "URI of file handler",
            optional = true,
            defaultValue = "http://192.168.7.212:8011",
            exampleValues = {"http://192.168.7.212:8011", "http://file-handler:8011"}
    )
    public static final FeaturerParamString fileHandlerUri = new FeaturerParamString("fileHandlerUri");

    @FeaturerParam(name = "downloadExternalFileConnectionTimeout",
            description = "If the File is added as external URI, it should be downloaded first.\nSo this params sets timout time in milliseconds for such download request.\n",
            optional = true,
            defaultValue = "60000",
            exampleValues = {"60000", "1000"}
    )
    public static final FeaturerParamInt downloadExternalFileConnectionTimeout = new FeaturerParamInt("downloadExternalFileConnectionTimeout");

    @FeaturerParam(
            name = "basePathReplaceMap",
            description = "Param to replace base path part in storage file key",
            optional = false
    )
    public static final FeaturerParamMap basePathReplaceMap = new FeaturerParamMap("basePathReplaceMap");

    @FeaturerParam(
            name = "relativePath",
            description = "Prefix for file keys.\nPlaceholders {domainId}, {businessAccountId} and {fileId} can be used to make domain/account relevant path.",
            optional = true,
            defaultValue = "/{businessAccountId}/{fileId}",
            exampleValues = {"/twins-resources/{domainId}/{businessAccountId}", "/{domainId}/{businessAccountId}", "/files"}
    )
    public static final FeaturerParamString relativePath = new FeaturerParamString("relativePath");

    @FeaturerParam(
            name = "resizeTasks",
            description = "Params for resize tasks to make images in specific sizes and formats.",
            optional = false
    )
    public static final FeaturerParamListOfMaps resizeTasks = new FeaturerParamListOfMaps("resizeTasks");

    private static final Set<String> RESIZABLE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/jpg");
    private static final String ORIGINAL_TYPE = "ORIGINAL";
    private final RestTemplate restTemplate;

    @Override
    protected Duration getDownloadExternalFileConnectionTimeout(HashMap<String, String> params) throws ServiceException {
        var properties = extractProperties(params, false);
        var extracted = downloadExternalFileConnectionTimeout.extract(properties);
        return Duration.ofMillis(extracted == null || extracted < 1 ? 60000 : extracted.longValue());
    }

    @Override
    public URI getFileUri(UUID fileId, String fileKey, HashMap<String, String> params) {
        return toURI(fileKey);
    }

    @Override
    public String getFileControllerUri(HashMap<String, String> params) {
        return "";
    }

    @Override
    public InputStreamResponse getFileAsStream(String fileKey, HashMap<String, String> params) throws ServiceException {
        throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "File handler service is not configured to store file bytes!");
    }

    @Override
    public void deleteFile(String fileKey, HashMap<String, String> params) throws ServiceException {
        try {
            var properties = extractProperties(params, false);
            var url = fileHandlerUri.extract(properties) + "/api/delete/synced";
            var dirs = extractDirsToDelete(fileKey, properties);
            var request = new HttpEntity<>(new FileHandlerDeleteRqDTO(List.of(dirs), StorageType.S3), new HttpHeaders());
            var resp = restTemplate.exchange(url, HttpMethod.POST, request, Void.class);

            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID);
            }
        } catch (Throwable t) {
            log.info("Unable to delete files in file-handler service: {}", t.getMessage(), t);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to delete files in file-handler service");
        }
    }

    @Override
    public String generateFileKey(UUID fileId, HashMap<String, String> params) throws ServiceException {
        var properties = extractProperties(params, false);
        var businessAccount = getBusinessAccountId().map(UUID::toString).orElse("defaultBusinessAccount");
        var relativePathString = addSlashAtTheEndIfNeeded(relativePath.extract(properties));
        var key = relativePathString
                .replace("{businessAccountId}", businessAccount)
                .replace("{fileId}", fileId.toString()) + fileId;
        var removedDoubleSlashes = removeDoubleSlashes(key);

        if (removedDoubleSlashes.startsWith("/")) {
            return removedDoubleSlashes.substring(1);
        } else {
            return removedDoubleSlashes;
        }
    }

    @Override
    protected AddedFileKey addFileInternal(String fileKey, InputStream fileStream, String mimeType, HashMap<String, String> params) throws ServiceException {
        try {
            Integer fileSizeLimit = getFileSizeLimit(params);
            CountedLimitedSizeInputStream sizeLimitedStream = new CountedLimitedSizeInputStream(fileStream, fileSizeLimit, 0);

            try (sizeLimitedStream) {
                var properties = extractProperties(params, false);
                var baseUrl = fileHandlerUri.extract(properties);
                var fileKeyElems = Arrays.stream(fileKey.split("/")).collect(Collectors.toList());
                var fileName = fileKeyElems.removeLast();
                var fileId = Arrays.stream(fileName.split("\\.")).toList().getFirst();
                var storageDir = String.join("/", fileKeyElems);
                var fileBytes = sizeLimitedStream.readAllBytes();

                if (shouldResize(mimeType)) {
                    var url = STR."\{baseUrl}/api/resize/save/synced";
                    var tasksParams = resizeTasks.extract(properties);
                    var tasks = new ArrayList<ResizeTaskDTO>();

                    try {
                        for (var taskParams : tasksParams) {
                            tasks.add(new ResizeTaskDTO(
                                    Integer.parseInt(taskParams.get("width")),
                                    Integer.parseInt(taskParams.get("height")),
                                    STR."Resize from \{getMimeSubType(mimeType)} to \{taskParams.get("format")}",
                                    Format.valueOf(taskParams.get("format").toUpperCase()),
                                    fileId,
                                    Boolean.parseBoolean(taskParams.get("keepAspectRatio"))
                            ));
                        }

                        // maybe delete this?
                        if (tasks.size() != tasks.stream().map(ResizeTaskDTO::type).distinct().count()) {
                            log.info("Type field in tasks params is not unique");
                            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS);
                        }
                    } catch (Throwable t) {
                        log.info("Unable to create resize tasks. Check tasks params: {}\n{}", tasksParams, t.getMessage(), t);
                        throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS);
                    }

                    var rqBody = new FileHandlerResizeSaveRqDTO(
                            fileId,
                            ORIGINAL_TYPE,
                            fileBytes,
                            tasks,
                            StorageType.S3,
                            storageDir,
                            true
                    );
                    var req = new HttpEntity<>(rqBody);
                    var resp = restTemplate.exchange(url, HttpMethod.POST, req,  new ParameterizedTypeReference<List<FileHandlerResizeSaveRsDTO>>() {});

                    if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                        log.error("RS STATUS CODE: {}\nRS BODY:{}", resp.getStatusCode(), resp.getBody());
                        throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID);
                    }

                    var modifications = new ArrayList<AttachmentModifications>();
                    var objectLink = "";

                    for (var modification : resp.getBody()) {
                        if (modification.type().equals(ORIGINAL_TYPE)) {
                            objectLink = modification.objectLink();
                        } else {
                            modifications.add(new AttachmentModifications(
                                    modification.id(),
                                    modification.type(),
                                    prepareObjectLink(modification.objectLink(), properties)
                            ));
                        }
                    }

                    return new AddedFileKey(prepareObjectLink(objectLink, properties), sizeLimitedStream.bytesRead(), modifications);
                } else {
                    var url = STR."\{baseUrl}/api/save/synced";
                    var rqBody = new FileHandlerSaveRqDTO(fileId, ORIGINAL_TYPE, fileBytes, StorageType.S3, storageDir);
                    var req = new HttpEntity<>(rqBody);
                    var resp = restTemplate.exchange(url, HttpMethod.POST, req, FileHandlerResizeSaveRsDTO.class);

                    if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                        log.error("RS STATUS CODE: {}\nRS BODY:{}", resp.getStatusCode(), resp.getBody());
                        throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID);
                    }

                    return new AddedFileKey(prepareObjectLink(resp.getBody().objectLink(), properties), sizeLimitedStream.bytesRead(), Collections.emptyList());
                }
            }
        } catch (Throwable t) {
            log.info("Unable to save file in file-handler service: {}", t.getMessage(), t);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to save file in file-handler service");
        }
    }

    private boolean shouldResize(String contentType) {
        return RESIZABLE_CONTENT_TYPES.contains(contentType);
    }

    private String getMimeSubType(String mimeType) {
        return mimeType.split("/")[1].toUpperCase();
    }

    private String prepareObjectLink(String objectLink, Properties properties) throws ServiceException {
        if (objectLink == null) {
            log.info("File wasn't saved on file-handler service, objectLink is null");
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID);
        }

        var replaceMap = basePathReplaceMap.extract(properties);
        var result = "";

        for (var entry : replaceMap.entrySet()) {
            result = objectLink.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }

    private String extractDirsToDelete(String fileKey, Properties properties) throws ServiceException {
        //extracting only relative path (ex. {businessAccountId}/{fileId}/)

        var parts = new ArrayList<>(List.of(fileKey.split("/")));
        var fileName = parts.removeLast();
        var fileId = fileName.split("\\.")[0];
        var businessAccountId = getBusinessAccountId().map(UUID::toString).orElseThrow(() -> new ServiceException(ErrorCodeCommon.UUID_UNKNOWN));
        var domainId = getDomainId().map(UUID::toString).orElseThrow(() -> new ServiceException(ErrorCodeCommon.UUID_UNKNOWN));

        var dirs = relativePath.extract(properties)
                .replace("{domainId}", domainId)
                .replace("{businessAccountId}", businessAccountId)
                .replace("{fileId}", fileId);

        dirs = addSlashAtTheEndIfNeeded(dirs);

        if (dirs.startsWith("/")) {
            return dirs.substring(1);
        } else {
            return dirs;
        }
    }
}
