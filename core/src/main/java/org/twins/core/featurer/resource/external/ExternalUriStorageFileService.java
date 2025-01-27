package org.twins.core.featurer.resource.external;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.resource.AbstractStorageFileService;
import org.twins.core.featurer.resource.AddedFileKey;

import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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
    public URI getFileUri(UUID fileId, String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
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
            HttpClient httpClient = HttpClient
                    .newBuilder()
                    .proxy(ProxySelector.getDefault())
                    .connectTimeout(timeoutDuration)
                    .build();
            HttpResponse<InputStream> response = httpClient
                    .send(request, HttpResponse.BodyHandlers.ofInputStream());
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
        //External resource, no need to save anything
    }

    @Override
    public String generateFileKey(UUID fileId, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        String uri = context.get("fileUri").toString();
        return uri;
    }

    /**
     * Adds a file to the service using the provided file ID, file URI, parameters, and context.
     *
     * @param fileId the unique identifier for the file
     * @param fileUri the URI of the file as a string
     * @param params a map of string-based parameters associated with the file
     * @param context a map of additional context-specific objects
     * @return an AddedFileKey object representing the added file's key
     * @throws ServiceException if an error occurs while adding the file
     */
    public AddedFileKey addFile(UUID fileId,String fileUri, HashMap<String, String> params,HashMap<String, Object> context) throws ServiceException {
        return addFile(fileId, fileUri.getBytes(StandardCharsets.UTF_8), params,context);
    }
    /**
     * Adds a file to the storage based on the provided file identifier, file content,
     * parameters, and context. Validates if the file size does not exceed the limit
     * specified within the parameters. If the file size exceeds the limit, a
     * {@link ServiceException} is thrown.
     *
     * @param fileId Unique identifier for the file being added.
     * @param fileUri Byte array representing the containing external URI
     * @param params A map of parameters that may include file-related metadata or configuration.
     * @param context A map containing context-specific information for the file operation.
     * @return An {@link AddedFileKey} object containing information about the stored file, such as its key and size.
     * @throws ServiceException If the file size exceeds the allowed limit or if any storage operation fails.
     */
    @Override
    public AddedFileKey addFile(UUID fileId, byte[] fileUri, HashMap<String, String> params,HashMap<String, Object> context) throws ServiceException {
        return super.addFile(fileId, fileUri, params,context);
    }
    /**
     * Adds a file using the provided file ID, input stream, parameters, and context information.
     *
     * @param fileId the unique identifier of the file
     * @param fileUri the input stream containing external URI
     * @param params a map of additional parameters required for adding the file
     * @param context a map containing contextual information for processing the file
     * @return an AddedFileKey object that represents the key of the added file
     * @throws ServiceException if an error occurs while adding the file
     */
    @Override
    public AddedFileKey addFile(UUID fileId, InputStream fileUri, HashMap<String, String> params,HashMap<String, Object> context) throws ServiceException {
        return super.addFile(fileId, fileUri, params,context);
    }
    /**
     * Adds a file internally to the system using the provided file information and returns a key representing the added file.
     *
     * @param fileId the unique identifier of the file to be added
     * @param fileUri the input stream containing external URI
     * @param params a map of string parameters associated with the file addition process
     * @param context a map of additional context objects to be used during the file addition process
     * @return an AddedFileKey object representing the key of the added file
     * @throws ServiceException if an error occurs while adding the file or processing its data
     */
    @Override
    protected AddedFileKey addFileInternal(UUID fileId, InputStream fileUri, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        try {
            String externalUri = new String(fileUri.readAllBytes(),StandardCharsets.UTF_8);
            return new AddedFileKey(externalUri,-1);
        } catch (Throwable t) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unable to get file");
        }
    }
}
