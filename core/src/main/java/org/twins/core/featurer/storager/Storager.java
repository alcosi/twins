package org.twins.core.featurer.storager;

import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.params.FeaturerParamString;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.auth.AuthService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.cambium.common.util.UrlUtils.toURI;

/**
 * Provides services for handling file uploads and resource management.
 * Includes functionalities for saving, retrieving, and deleting files, as well as validating file size and MIME types.
 */
@FeaturerType(id = FeaturerTwins.TYPE_29,
        name = "Storager",
        description = "Services for file uploading")
@Slf4j
public abstract class Storager extends FeaturerTwins {
    @FeaturerParam(name = "selfHostDomainBaseUri",
            description = "URI where TWINS app is hosted and can be accessed externally. Can be relative '/' for most cases",
            optional = true,
            defaultValue = "/",
            exampleValues = {"/", "https://twins.app/api", "/proxied-api"}
    )
    public static final FeaturerParamString selfHostDomainBaseUri = new FeaturerParamString("selfHostDomainBaseUri");

    private static OkHttpClient httpClient;

    @Autowired
    protected AuthService authService;

    protected abstract Duration getDownloadExternalFileConnectionTimeout(HashMap<String, String> params) throws ServiceException;

    protected Optional<UUID> getDomainId() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (apiUser == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(apiUser.getDomainId());
    }

    protected Optional<UUID> getBusinessAccountId() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (apiUser == null || !apiUser.isBusinessAccountSpecified()) {
            return Optional.empty();
        }
        return Optional.ofNullable(apiUser.getBusinessAccountId());
    }

    protected Optional<UUID> getUserId() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (apiUser == null || !apiUser.isUserSpecified()) {
            return Optional.empty();
        }
        return Optional.ofNullable(apiUser.getUserId());
    }

    /**
     * Constructs and returns the URI for the file controller based on the provided parameters and context.
     *
     * @param params a map of string key-value pairs containing file-specific parameters.
     * @return the URI of the file controller as a string.
     * @throws ServiceException if an error occurs while constructing the URI.
     */
    abstract public String getFileControllerUri(HashMap<String, String> params) throws ServiceException;

    /**
     * Provides a mechanism to retrieve a file as an InputStream using the given file key and parameters.
     * This method allows fetching file content from respective storage or source
     * based on the implementation of the service.
     *
     * @param fileKey The unique key or identifier corresponding to the file.
     * @param params  A map containing additional parameters required to locate or fetch the file.
     * @return An InputStream representing the file's content.
     * @throws ServiceException If there is an error while retrieving the file or if the file cannot be found.
     */
    abstract public InputStreamResponse getFileAsStream(String fileKey, HashMap<String, String> params) throws ServiceException;

    /**
     * Retrieves the contents of a file as a byte array.
     *
     * @param fileKey The unique key or identifier of the file to retrieve.
     * @param params  A set of key-value pair parameters used for file retrieval,
     *                such as configurations or contextual information.
     * @return A byte array representing the content of the requested file.
     * @throws ServiceException If an error occurs while retrieving or processing the file.
     */
    @SneakyThrows
    public byte[] getFileBytes(String fileKey, HashMap<String, String> params) throws ServiceException {
        try (InputStream stream = getFileAsStream(fileKey, params).getContentStream()) {
            return stream.readAllBytes();
        }
    }

    /**
     * Retrieves the URI of a file based on the provided file identifier, file key, parameters,
     * and context. This method constructs a URI using the file controller's URI and the file identifier.
     *
     * @param fileId  the unique identifier of the file
     * @param fileKey the key associated with the file
     * @param params  a map of parameters used for file configuration and URI generation
     * @return the URI of the file
     * @throws ServiceException if any error occurs during the URI generation
     */
    public URI getFileUri(UUID fileId, String fileKey, HashMap<String, String> params) throws ServiceException {
        String domainId = getDomainId().map(UUID::toString).orElse("defaultDomain");
        String businessAccountId = getBusinessAccountId().map(UUID::toString).orElse("defaultBusinessAccount");
        return toURI(getFileControllerUri(params)
                .replace("{id}", fileId.toString())
                .replace("{key}", fileKey)
                .replace("{domainId}", domainId)
                .replace("{businessAccountId}", businessAccountId));
    }

    /**
     * Adds a file to the storage based on the provided file identifier, file content,
     * parameters, and context. Validates if the file size does not exceed the limit
     * specified within the parameters. If the file size exceeds the limit, a
     * {@link ServiceException} is thrown.
     *
     * @param fileId Unique identifier for the file being added.
     * @param file   Byte array representing the content of the file.
     * @param params A map of parameters that may include file-related metadata or configuration.
     * @return An {@link AddedFileKey} object containing information about the stored file, such as its key and size.
     * @throws ServiceException If the file size exceeds the allowed limit or if any storage operation fails.
     */
    public AddedFileKey addFile(UUID fileId, byte[] file, HashMap<String, String> params) throws ServiceException {
        return addFile(fileId, new ByteArrayInputStream(file), params);
    }

    /**
     * Adds an external URL as a file resource and associates it with the given file ID and parameters.
     *
     * @param fileId      the unique identifier of the file to which the external URL will be associated
     * @param externalUrl the URL of the external resource to be added
     * @param params      additional parameters to be used during the process of adding the external URL
     * @return the key and size of the added file resource
     * @throws ServiceException if an error occurs while adding the external URL
     */
    @SneakyThrows
    public AddedFileKey addExternalUrlFile(UUID fileId, String externalUrl, HashMap<String, String> params) throws ServiceException {
        return addFile(fileId, getInputStreamHttpResponse(toURI(externalUrl), params).body().byteStream(), params);
    }

    /**
     * Adds a file to the storage service with the specified file identifier, file stream, parameters, and context.
     * Returns an {@code AddedFileKey} containing the unique key and size of the added file.
     *
     * @param fileId     the unique identifier of the file to be added.
     * @param fileStream an InputStream representing the content of the file to be added.
     * @param params     a map containing file-specific parameters required for the operation.
     * @return an {@code AddedFileKey} object containing the unique file key and file size.
     * @throws ServiceException if there is an error during the file addition process.
     */
    abstract public AddedFileKey addFile(UUID fileId, InputStream fileStream, HashMap<String, String> params) throws ServiceException;

    /**
     * Attempts to delete a file using the provided file key, parameters, and context.
     * If the deletion fails method will throw error.
     *
     * @param fileKey the unique identifier of the file to be deleted
     * @param params  a map containing parameters related to the file deletion
     */
    public void tryDeleteFile(String fileKey, HashMap<String, String> params) throws ServiceException {
        deleteFile(fileKey, params);
    }

    /**
     * Deletes the specified file based on the provided file key, parameters, and context.
     * This method is intended to handle the deletion of a file in implementations of storage services.
     *
     * @param fileKey A unique identifier for the file to be deleted.
     * @param params  A HashMap containing service-specific parameters required for file deletion.
     * @throws ServiceException If there is an issue during the deletion process.
     */
    abstract public void deleteFile(String fileKey, HashMap<String, String> params) throws ServiceException;

    /**
     * Generates a unique file key based on the provided file identifier, parameters, and contextual data.
     * This method is abstract and must be implemented by subclasses to define specific key generation logic.
     *
     * @param fileId the unique identifier of the file for which the key is being generated
     * @param params a map containing parameters required for generating the file key
     * @return a string representing the generated file key
     * @throws ServiceException if an error occurs during the key generation process
     */
    abstract protected String generateFileKey(UUID fileId, HashMap<String, String> params) throws ServiceException;

    /**
     * Sends an HTTP GET request to the specified URI with the provided parameters and returns the response as an InputStream.
     *
     * @param uri    the target URI for the HTTP request
     * @param params a HashMap containing additional parameters necessary for the request
     * @return a Response object containing the InputStream of the response body
     * @throws ServiceException     if there is an error during property extraction
     * @throws IOException          if an I/O error occurs while sending or receiving the HTTP request
     */
    protected Response getInputStreamHttpResponse(URI uri, HashMap<String, String> params) throws ServiceException, IOException {
        Duration timeoutDuration = getDownloadExternalFileConnectionTimeout(params);

        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .proxySelector(ProxySelector.getDefault())
                    .connectTimeout(timeoutDuration)
                    .readTimeout(timeoutDuration)
                    .build();
        }

        Request request = new Request.Builder()
                .url(uri.toURL())
                .get()
                .build();

        int maxRetries = 3;
        IOException lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Response response = httpClient.newCall(request).execute();

                if (!response.isSuccessful()) {
                    response.close();
                    throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Failed to retrieve the file: HTTP Status " + response.code());
                }

                if (response.body() == null) {
                    response.close();
                    throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Failed to retrieve the file, response body is NULL");
                }

                return response;
            } catch (IOException e) {
                lastException = e;
                log.warn("Attempt {}/{} failed for URI {}: {}", attempt, maxRetries, uri, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(500L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Retry interrupted", ie);
                    }
                }
            }
        }

        throw lastException;
    }

    @NotNull
    protected String addSlashAtTheEndIfNeeded(String path) {
        if (!path.endsWith("/")) {
            return path + "/";
        }
        return path;
    }

    @NotNull
    protected String addSlashAtStartIfNeeded(String path) {
        if (!path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    @NotNull
    protected String removeDoubleSlashes(String path) {
        return path.replaceAll("/{2,}", "/");
    }
}
