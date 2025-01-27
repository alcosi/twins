package org.twins.core.featurer.resource.external;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.http.HttpHeaders;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.resource.AbstractCheckedStorageFileService;
import org.twins.core.featurer.resource.AddedFileKey;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@Featurer(id = FeaturerTwins.ID_2903,
        name = "ExternalUriStorageFileService",
        description = "Service to keep and work with external uri")
@Slf4j
public class ExternalUriStorageFileService extends AbstractCheckedStorageFileService {

    @FeaturerParam(name = "connectionTimeout", description = "Connection timeout when getting file")
    public static final FeaturerParamInt connectionTimeout = new FeaturerParamInt("connectionTimeout");
    public static final String CONTEXT_ATTRIBUTE_FILE_URI = "fileUri";

    @Override
    public URI getFileUri(UUID fileId, String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        return URI.create(fileKey);
    }

    @Override
    public String getFileControllerUri(HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        return "";
    }

    @Override
    public InputStream getFileAsStream(String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        try {
            HttpResponse<InputStream> response = getInputStreamHttpResponse(fileKey, params, context);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Failed to retrieve the file: HTTP Status " + response.statusCode());
            }
            return response.body();
        } catch (Throwable t) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to get file");
        }
    }


    @Override
    public void deleteFile(String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        //External resource, no need to delete anything
    }

    @Override
    @SneakyThrows
    protected void addFileInternal(String fileKey, InputStream fileStream, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        if (fileStream != null) {
            int byteBufferSize = Short.MAX_VALUE;
            //Read all bytes by chunks till the end
            while (fileStream.read()>-1) {
                fileStream.readNBytes(byteBufferSize);
            }
        }
    }

    @Override
    protected AddedFileKey checkAndAddFileInternal(UUID fileId, InputStream fileStream, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        String fileKey = generateFileKey(fileId, params, context);
        try {
            HttpResponse<InputStream> response = getInputStreamHttpResponse(fileKey, params, context);
            fileStream = response.body();
            Integer fileSizeLimit = getFileSizeLimit(params,context);
            Optional<Long> contentLengthHeader = response.headers().firstValue(HttpHeaders.CONTENT_LENGTH).map(Long::valueOf);
            //Chunked response, have to check content length by downloading file =(
            if (contentLengthHeader.isEmpty()||contentLengthHeader.get()<0) {
                return super.checkAndAddFileInternal(fileId, fileStream, params, context);
            }
            if (fileSizeLimit!=null&&fileSizeLimit>-1&&contentLengthHeader.get()>fileSizeLimit){
                throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "File size limit " + fileSizeLimit + " exceeded (" + contentLengthHeader.get() + ")");

            }
            try (InputStream is = checkMimeTypeAndCacheStream(fileStream,params,context)){
                return new AddedFileKey(fileKey, contentLengthHeader.get());
            }
        }catch (ServiceException e){
            tryDeleteFile(fileKey,params,context);
            throw e;
        } catch (Throwable t) {
            tryDeleteFile(fileKey,params,context);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to add file");
        }
    }

    @Override
    public String generateFileKey(UUID fileId, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        String uri = context.get(CONTEXT_ATTRIBUTE_FILE_URI).toString();
        return uri;
    }

    protected HttpResponse<InputStream> getInputStreamHttpResponse(String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException, IOException, InterruptedException {
        Properties properties = featurerService.extractProperties(this, params, new HashMap<>());
        Integer timeout = connectionTimeout.extract(properties);
        Duration timeoutDuration = Duration.ofMillis(timeout == null ? 60000 : timeout);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(getFileUri(null, fileKey, params, new HashMap<>()))
                .GET()
                .timeout(timeoutDuration)
                .build();
        HttpClient httpClient = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .connectTimeout(timeoutDuration)
                .build();
        HttpResponse<InputStream> response = httpClient
                .send(request, HttpResponse.BodyHandlers.ofInputStream());
        return response;
    }
}
