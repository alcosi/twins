package org.twins.core.featurer.storager.filehandler;

import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.io.TikaInputStream;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamMap;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.twins.core.dto.rest.featurer.storager.filehandler.AttachmentModification;
import org.twins.core.dto.rest.featurer.storager.filehandler.FHSyncProcessRsDTO;
import org.twins.core.dto.rest.featurer.storager.filehandler.FileHandlerDeleteRqDTO;
import org.twins.core.enums.featurer.storager.StorageType;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.storager.AddedFileKey;
import org.twins.core.featurer.storager.StoragerAbstractChecked;

import javax.naming.LimitExceededException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.cambium.common.util.UrlUtils.toURI;

@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_2907,
        name = "StoragerAlcosiFileHandlerV2",
        description = "Service to save or delete files via file-handler service (api with multipart)"
)
@Slf4j
public class StoragerAlcosiFileHandlerV2 extends StoragerAbstractChecked {

    @FeaturerParam(
            name = "fileHandlerUri", description = "URI of file handler",
            optional = true,
            defaultValue = "http://192.168.7.212:8011",
            exampleValues = {"http://192.168.7.212:8011", "http://file-handler:8011"}
    )
    public static final FeaturerParamString fileHandlerUri = new FeaturerParamString("fileHandlerUri");

    @FeaturerParam(
            name = "fileHandlerUploadPath", description = "Upload endpoint",
            optional = true,
            defaultValue = "/api/resize/save/synced",
            exampleValues = {"/api/resize/save/synced", "/api/resize/async/save/synced"}
    )
    public static final FeaturerParamString fileHandlerUploadPath = new FeaturerParamString("fileHandlerUploadPath");


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

    // NOTE: all deployments use the DEFAULT value "/{businessAccountId}/{fileId}". The parsers in this
    // class (generateFileKey, addFileInternal, extractDirsToDelete) rely on that exact template shape
    @FeaturerParam(
            name = "relativePath",
            description = "Prefix for file keys.\nPlaceholders {domainId}, {businessAccountId} and {fileId} can be used to make domain/account relevant path.",
            optional = true,
            defaultValue = "/{businessAccountId}/{fileId}",
            exampleValues = {"/twins-resources/{domainId}/{businessAccountId}", "/{domainId}/{businessAccountId}", "/files"}
    )
    public static final FeaturerParamString relativePath = new FeaturerParamString("relativePath");

    @FeaturerParam(
            name = "imagePipeline",
            description = "Tasks for image pipeline (save orig + save resized)",
            optional = false
    )
    public static final FeaturerParamString imagePipeline = new FeaturerParamString("imagePipeline");

    @FeaturerParam(
            name = "defaultPipeline",
            description = "Tasks for default pipeline (save the original file)",
            optional = false
    )
    public static final FeaturerParamString defaultPipeline = new FeaturerParamString("defaultPipeline");

    private static final Set<String> IMAGE_FORMATS = Set.of("jpg", "jpeg", "png", "webp", "tiff", "tif", "gif", "jp2");
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
            var url = STR."\{fileHandlerUri.extract(properties)}/api/storage/delete";
            var dirs = extractDirsToDelete(fileKey);
            var request = new HttpEntity<>(new FileHandlerDeleteRqDTO(List.of(dirs), StorageType.S3), new HttpHeaders());
            var resp = exchangeWithRetry(
                    "Delete " + dirs,
                    () -> restTemplate.exchange(url, HttpMethod.POST, request, Void.class)
            );

            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.info("Unable to delete files in file-handler service: {}", e.getMessage(), e);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to delete files in file-handler service");
        }
    }

    @Override
    public String generateFileKey(UUID fileId, HashMap<String, String> params) throws ServiceException {
        // default template renders fileKey as "{businessAccountId}/{fileId}/{fileId}":
        // storageDir "{businessAccountId}/{fileId}" + bare fileId UUID as the file name
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
            TikaInputStream tikaStream = TikaInputStream.get(fileStream);

            try (tikaStream) {
                var properties = extractProperties(params, false);
                // fileKey shape from generateFileKey: "{businessAccountId}/{fileId}/{fileId}" —
                // the file name is a bare fileId UUID, no extension or label
                var fileKeyElems = Arrays.stream(fileKey.split("/")).collect(Collectors.toList());
                var fileName = fileKeyElems.removeLast();
                var fileId = Arrays.stream(fileName.split("\\.")).toList().getFirst();
                var storageDir = String.join("/", fileKeyElems);
                var fileSize = getFileSize(tikaStream, fileSizeLimit);

                var detectedMime = tika.detect(tikaStream);
                var pipelineJson = preparePipelineJson(properties, fileId, storageDir, detectedMime);

                var resp = sendWithRetry(fileHandlerUri.extract(properties) + fileHandlerUploadPath.extract(properties), pipelineJson, tikaStream, fileName, FHSyncProcessRsDTO.class);

                if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                    log.error("RS STATUS CODE: {}\nRS BODY:{}", resp.getStatusCode(), resp.getBody());
                    throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID);
                }

                var body = resp.getBody();
                String originalUrl = null;
                var modifications = new ArrayList<AttachmentModification>();

                if (body.outputs() != null) {
                    for (var output : body.outputs()) {
                        if ("save".equals(output.taskType())) {
                            originalUrl = prepareObjectLink(output.url(), properties);
                        } else {
                            modifications.add(new AttachmentModification(
                                    body.fileId(),
                                    output.as(),
                                    prepareObjectLink(output.url(), properties)
                            ));
                        }
                    }
                }

                if (originalUrl == null) {
                    log.error("No 'save' output found in pipeline response for file: {}", fileName);
                    throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID);
                }

                return new AddedFileKey(originalUrl, fileSize, modifications);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.info("Unable to save file in file-handler service: {}", e.getMessage(), e);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to save file in file-handler service");
        }
    }

    private String preparePipelineJson(Properties properties, String fileId, String storageDir, String detectedMime) throws ServiceException {
        var pipelineJson = IMAGE_FORMATS.contains(getMimeSubType(detectedMime))
                ? imagePipeline.extract(properties)
                : defaultPipeline.extract(properties);
        return pipelineJson
                .replace("{fileId}", fileId)
                .replace("{storageDir}", storageDir);
    }

    private String getMimeSubType(String mimeType) {
        return mimeType.split("/")[1].toLowerCase();
    }

    private String prepareObjectLink(String objectLink, Properties properties) throws ServiceException {
        if (objectLink == null) {
            log.info("File wasn't saved on file-handler service, objectLink is null");
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID);
        }

        var replaceMap = basePathReplaceMap.extract(properties);
        var result = objectLink;

        for (var entry : replaceMap.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }

    private String extractDirsToDelete(String fileKey) {
        // fileKey is the URL stored on save; with the default relativePath it looks like
        // ".../{businessAccountId}/{fileId}/{fileId}-{as}.{ext}" — the handler renames the file
        // ("{fileId}-original.jpg" etc.) but keeps our dirs. Dir to delete = last two segments
        // before the file name: "{businessAccountId}/{fileId}" — label/extension noise goes away
        // together with the file name itself.
        var segments = List.of(fileKey.split("/"));

        return String.join("/", segments.subList(segments.size() - 3, segments.size() - 1));
    }

    private HttpEntity<MultiValueMap<String, Object>> prepareMultipartRq(Object rqData, Resource fileResource) {
        var fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        var body = new LinkedMultiValueMap<String, Object>();

        body.add("data", prepareDataPart(rqData));
        body.add("file", new HttpEntity<>(fileResource, fileHeaders));

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<?> prepareDataPart(Object rqData) {
        var dataHeaders = new HttpHeaders();
        dataHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(rqData, dataHeaders);
    }

    @SneakyThrows
    private long getFileSize(InputStream inputStream, Integer fileSizeLimit) {
        inputStream.mark(Integer.MAX_VALUE);
        long size = inputStream.transferTo(OutputStream.nullOutputStream());
        inputStream.reset();

        if (size > fileSizeLimit && fileSizeLimit != -1) {
            throw new LimitExceededException(STR."File size [\{size}] is exceeding the size limit [\{fileSizeLimit}]");
        }

        return size;
    }

    @SneakyThrows
    private <T> ResponseEntity<T> sendWithRetry(String url, Object rqData, InputStream tikaStream, String fileName, Class<T> clazz) {
        // RestTemplate closes the part stream after the first write, so the file is spooled to a temp
        // file and re-opened by FileSystemResource on every attempt — an InputStreamResource would be
        // dead after attempt one
        var tempFile = Files.createTempFile("file-handler-upload", null);
        Files.copy(tikaStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

        try {
            var fileResource = new FileSystemResource(tempFile) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };

            return exchangeWithRetry(
                    STR."Upload \{fileName}",
                    () -> restTemplate.exchange(url, HttpMethod.POST, prepareMultipartRq(rqData, fileResource), clazz)
            );
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private <T> ResponseEntity<T> exchangeWithRetry(String what, Supplier<ResponseEntity<T>> exchangeCall) {
        var maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return exchangeCall.get();
            } catch (HttpServerErrorException.NotImplemented e) {
                throw e;
            } catch (ResourceAccessException | HttpClientErrorException.TooManyRequests | HttpServerErrorException e) {
                log.warn("{}: attempt {}/{} failed: {}", what, attempt, maxRetries, e.getMessage());

                if (attempt == maxRetries) {
                    throw e;
                }

                try {
                    Thread.sleep(retryDelayMs(e, attempt));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }

        return null;
    }

    private long retryDelayMs(Exception e, int attempt) {
        if (e instanceof HttpClientErrorException.TooManyRequests) {
            return 2000L * attempt; // cumulative 12s outlasts the 10s FH rate window
        }

        return 500L * attempt; // network error / 5xx: 0.5s, 1s, 1.5s
    }
}
