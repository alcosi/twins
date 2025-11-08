package org.twins.core.featurer.storager.filehandler;

import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamListOfMaps;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.twins.core.dto.rest.featurer.storager.FileHandlerDeleteDTO;
import org.twins.core.dto.rest.featurer.storager.FileHandlerResizeSaveDTO;
import org.twins.core.dto.rest.featurer.storager.FileHandlerSaveDTO;
import org.twins.core.dto.rest.featurer.storager.ResizeTaskDTO;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.storager.StoragerAbstractChecked;

import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_2906,
        name = "StoragerFileHandlerController",
        description = "Service to save or delete files via file-handler service"
)
@Slf4j
public class StoragerFileHandlerController extends StoragerAbstractChecked {

    // did this because of example with S3, but maybe it will be better to make this properties value
    @FeaturerParam(
            name = "fileHandlerUri", description = "URI of file handler",
            optional = true,
            defaultValue = "http://192.168.7.212:8011",
            exampleValues = {"http://192.168.7.212:8011", "http://file-handler:8011"}
    )
    public static final FeaturerParamString fileHandlerUri = new FeaturerParamString("fileHandlerUri");

    @FeaturerParam(
            name = "basePath",
            description = "Prefix for file keys.\nPlaceholders {domainId} and {businessAccountId} can be used to make domain/account relevant path.",
            optional = true,
            defaultValue = "/{businessAccountId}/{fileId}",
            exampleValues = {"/twins-resources/{domainId}/{businessAccountId}", "/{domainId}/{businessAccountId}", "/files"}
    )
    public static final FeaturerParamString basePath = new FeaturerParamString("basePath");

    @FeaturerParam(
            name = "resizeTasks",
            description = "Params for resize tasks to make images in specific sizes and formats.",
            optional = false
    )
    public static final FeaturerParamListOfMaps resizeTasks = new FeaturerParamListOfMaps("resizeTasks");

    private final RestTemplate restTemplate;
    private static final Set<String> RESIZABLE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/jpg");

    @Override
    protected Duration getDownloadExternalFileConnectionTimeout(HashMap<String, String> params) {
        return null;
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
            var dirs = Arrays.copyOf(fileKey.split("/"), fileKey.split("/").length - 1);
            var request = new HttpEntity<>(new FileHandlerDeleteDTO(dirs, StorageType.S3), new HttpHeaders());
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
        //after all file key will be smth like 111/222/222.jpeg
        var properties = extractProperties(params, false);
        var businessAccount = getBusinessAccountId().map(UUID::toString).orElse("defaultBusinessAccount");
        var baseLocalPathString = addSlashAtTheEndIfNeeded(basePath.extract(properties));
        var key = baseLocalPathString
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
    protected void addFileInternal(String fileKey, InputStream fileStream, String mimeType, HashMap<String, String> params) throws ServiceException {
        try {
            try (fileStream) {
                var properties = extractProperties(params, false);
                var baseUrl = fileHandlerUri.extract(properties);
                var fileKeyElems = Arrays.stream(fileKey.split("/")).collect(Collectors.toList());
                var fileName = fileKeyElems.removeLast();
                var fileId = Arrays.stream(fileName.split("\\.")).toList().getFirst();
                var storageDir = String.join("/", fileKeyElems);
                var fileBytes = fileStream.readAllBytes();

                if (shouldResize(mimeType)) {
                    var url = baseUrl + "/api/resize/save/synced";
                    var tasksParams = resizeTasks.extract(properties);
                    var tasks = new ArrayList<ResizeTaskDTO>();

                    try {
                        for (var taskParams : tasksParams) {
                            tasks.add(new ResizeTaskDTO(
                                    Integer.parseInt(taskParams.get("width")),
                                    Integer.parseInt(taskParams.get("height")),
                                    taskParams.get("type"),
                                    getMimeSubType(mimeType),
                                    fileId,
                                    Boolean.parseBoolean(taskParams.get("keepAspectRatio"))
                            ));
                        }
                    } catch (Throwable t) {
                        log.info("Unable to create resize tasks. Check tasks params: {}\n{}", tasksParams, t.getMessage(), t);
                        throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS);
                    }

                    var rqBody = new FileHandlerResizeSaveDTO(
                            fileId,
                            fileName,
                            fileBytes,
                            tasks,
                            StorageType.S3,
                            storageDir,
                            true
                    );
                    var req = new HttpEntity<>(rqBody);
                    var resp = restTemplate.exchange(url, HttpMethod.POST, req, Void.class);

                    if (!resp.getStatusCode().is2xxSuccessful()) {
                        throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID);
                    }
                } else {
                    var url = baseUrl + "/api/save/synced";
                    var rqBody = new FileHandlerSaveDTO(fileId, fileName, fileBytes, StorageType.S3, storageDir);
                    var req = new HttpEntity<>(rqBody);
                    var resp = restTemplate.exchange(url, HttpMethod.POST, req, Void.class);

                    if (!resp.getStatusCode().is2xxSuccessful()) {
                        throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID);
                    }
                }
            }
        } catch (Throwable t) {
            log.info("Unable to save file in file-handler service: {}", t.getMessage(), t);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable save file in file-handler service");
        }
    }

    private boolean shouldResize(String contentType) {
        return RESIZABLE_CONTENT_TYPES.contains(contentType);
    }

    private String getMimeSubType(String mimeType) {
        return mimeType.split("/")[1].toUpperCase();
    }

    public enum StorageType {
        S3
    }
}
