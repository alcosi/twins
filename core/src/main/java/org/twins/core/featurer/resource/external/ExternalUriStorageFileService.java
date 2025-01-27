package org.twins.core.featurer.resource.external;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.resource.AbstractStorageFileService;

import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

@Featurer(id = FeaturerTwins.ID_2903,
        name = "ExternalUriStorageFileService",
        description = "Service to keep and work with external uri")
@Slf4j
public class ExternalUriStorageFileService extends AbstractStorageFileService {

    @FeaturerParam(name = "connectionTimeout", description = "Connection timeout when getting file")
    public static final FeaturerParamInt connectionTimeout = new FeaturerParamInt("connectionTimeout");
    @Override
    public URI getFileUri(UUID fileId,String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        return URI.create(fileKey);
    }

    @Override
    protected String getFileControllerUri(HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        return "";
    }

    @Override
    public InputStream getFileAsStream(String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        try {
            Properties properties = featurerService.extractProperties(this, params, new HashMap<>());
            Integer timeout = connectionTimeout.extract(properties);
            Duration timeoutDuration = Duration.ofMillis(timeout == null ? 60000 : timeout);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(getFileUri(null, fileKey, params, new HashMap<>()))
                    .GET()
                    .timeout(timeoutDuration)
                    .build();
            HttpResponse<InputStream> response = HttpClient
                    .newBuilder()
                    .proxy(ProxySelector.getDefault())
                    .connectTimeout(timeoutDuration)
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofInputStream());
            return response.body();
        } catch (Throwable t){
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to get resource");
        }
    }

    @Override
    public void deleteFile(String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        //External resource, no need to delete anything
    }

    @Override
    protected void addFileInternal(String fileKey, InputStream fileStream, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        //External resource, no need to save anything
    }

    @Override
    public String generateFileKey(UUID fileId, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        String uri=context.get("fileUri").toString();
        return uri;
    }
}
