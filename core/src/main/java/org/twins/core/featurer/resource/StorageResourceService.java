package org.twins.core.featurer.resource;

import io.github.breninsul.io.service.stream.inputStream.CacheReadenInputStream;
import io.github.breninsul.io.service.stream.inputStream.CountedLimitedSizeInputStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamWordList;
import org.twins.core.featurer.FeaturerTwins;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@FeaturerType(id = FeaturerTwins.TYPE_29,
        name = "StorageResourceService",
        description = "Services for resource(file) uploading")
@Slf4j
public abstract class StorageResourceService extends FeaturerTwins {
    protected final Tika tika = new Tika();

    @FeaturerParam(name = "selfHostDomainBaseUri", description = "external URI/domain of twins application to create resource links")
    public static final FeaturerParamString selfHostDomainBaseUri = new FeaturerParamString("selfHostDomainBaseUri");
    @FeaturerParam(name = "fileSizeLimit", description = "Limit of file size")
    public static final FeaturerParamInt fileSizeLimit = new FeaturerParamInt("fileSizeLimit");
    @FeaturerParam(name = "supportedMimeTypes", description = "List of supported mime types")
    public static final FeaturerParamWordList supportedMimeTypes = new FeaturerParamWordList("supportedMimeTypes");

    /**
     * Retrieves the self-hosted base domain URI using the provided parameters.
     *
     * @param params a HashMap containing parameters required for extracting properties and determining the domain URI
     * @return a String representing the base domain URI for the self-hosted implementation
     * @throws ServiceException if there is an error during the extraction of properties or the URI determination process
     */
    protected String getResourceControllerUri(HashMap<String, String> params) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params, new HashMap<>());
        String domain = selfHostDomainBaseUri.extract(properties);
        if (!domain.endsWith("/")) {
            domain = domain + "/";
        }
        return domain + "resource/";
    }

    /**
     * Retrieves the file size limit based on the provided parameters.
     *
     * @param params a map of parameters used to extract the configuration for file size limit
     * @return the file size limit as an Integer
     * @throws ServiceException if an error occurs while extracting the properties or calculating the file size limit
     */
    protected Integer getFileSizeLimit(HashMap<String, String> params) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params, new HashMap<>());
        return fileSizeLimit.extract(properties);
    }

    /**
     * Retrieves the set of supported MIME types for the service based on the provided parameters.
     *
     * @param params a HashMap containing the parameters required to extract the properties
     *               and fetch the supported MIME types.
     * @return a set of supported MIME types in lowercase. Returns an empty set if no MIME types are found.
     * @throws ServiceException if an error occurs while extracting properties or determining supported MIME types.
     */
    protected Set<String> getSupportedMimeTypes(HashMap<String, String> params) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, params, new HashMap<>());
        List<String> list = supportedMimeTypes.extract(properties);
        if (list == null || list.isEmpty()) {
            return Collections.emptySet();
        }
        return list.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }

    /**
     * Retrieves a resource as an InputStream based on the provided resource key
     * and additional parameters.
     *
     * @param resourceKey the key identifying the requested resource
     * @param params      a map of parameters that may influence the resource retrieval process
     * @return an InputStream to access the resource, or null if the resource could not be found
     * @throws ServiceException if an error occurs during resource retrieval
     */
    protected abstract InputStream getResourceAsStream(String resourceKey, HashMap<String, String> params) throws ServiceException;

    /**
     * Retrieves the byte content of a resource based on its key and optional parameters.
     *
     * @param resourceKey the key identifying the resource to be retrieved
     * @param params      a map of additional parameters relevant to the resource retrieval
     * @return a byte array representing the content of the resource
     * @throws ServiceException if an I/O error occurs during reading the resource
     */
    @SneakyThrows
    protected byte[] getResourceBytes(String resourceKey, HashMap<String, String> params) throws ServiceException {
        try (InputStream stream = getResourceAsStream(resourceKey, params)) {
            return stream.readAllBytes();
        }
    }

    /**
     * Constructs and returns the URI for a resource based on the provided resource ID and parameters.
     *
     * @param resourceId the unique identifier of the resource
     * @param params     a map of parameters that may impact the construction of the resource URI
     * @return the constructed URI for the resource
     * @throws ServiceException if an error occurs during URI construction
     */
    protected URI getResourceUri(UUID resourceId, HashMap<String, String> params) throws ServiceException {
        return URI.create(getResourceControllerUri(params) + resourceId);
    }

    /**
     * Saves a resource consisting of a resource key, an input stream, and additional parameters.
     *
     * @param resourceKey    The unique key used to identify the resource. Cannot be null or empty.
     * @param resourceStream The input stream containing the data of the resource to be saved. Cannot be null.
     * @param params         A map of additional parameters required for saving the resource (e.g., configuration details). Optional parameter, can be null.
     * @throws ServiceException If an error occurs during the process of saving the resource.
     */
    protected Long saveResource(String resourceKey, InputStream resourceStream, HashMap<String, String> params) throws ServiceException {
        return saveResourceInternal(resourceKey, resourceStream, params);
    }

    /**
     * Saves a resource into the storage system with specified parameters.
     * Validates the resource size against a defined limit and throws an exception if exceeded.
     *
     * @param resourceKey A unique key identifying the resource.
     * @param resource    The byte array representing the resource content to be saved.
     * @param params      A map of string key-value pairs containing additional parameters for resource saving operations.
     * @throws ServiceException If the resource size exceeds the limit or if a service-related error occurs during the save operation.
     */
    protected Long saveResource(String resourceKey, byte[] resource, HashMap<String, String> params) throws ServiceException {
        Integer fileSizeLimit = getFileSizeLimit(params);
        if (resource.length > fileSizeLimit) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Resource size limit " + fileSizeLimit + " exceeded (" + resource.length + ")");
        }
        return saveResource(resourceKey, new ByteArrayInputStream(resource), params);
    }

    /**
     * Saves a resource with a specific key and input stream, adhering to optional size and MIME type constraints.
     * The method checks for file size limits and validates MIME types if specified in the parameters.
     *
     * @param resourceKey    The unique key associated with the resource being saved.
     * @param resourceStream The input stream containing the data of the resource.
     * @param params         Additional parameters that may include constraints such as size limits or supported MIME types.
     * @throws ServiceException If the resource exceeds the size limit, has an unsupported MIME type, or other errors occur during the process.
     */
    protected Long saveResourceInternal(String resourceKey, InputStream resourceStream, HashMap<String, String> params) throws ServiceException {
        try {
            Set<String> supportedMimeTypes = getSupportedMimeTypes(params);
            if (!supportedMimeTypes.isEmpty()) {
                //Not to read IS twice, we have to cache already readen bytes by mime type resolving
                CacheReadenInputStream cachedReadInputStream = new CacheReadenInputStream(resourceStream, false,Short.MAX_VALUE);
                String[] mimeTypeArray = tika.detect(cachedReadInputStream).toLowerCase().split("/");
                String mimeType = mimeTypeArray[0];
                String mimeSubType = mimeTypeArray.length > 1 ? mimeTypeArray[1] : "*";
                supportedMimeTypes.stream().filter(t -> {
                    if (t.contains("/")) {
                        String[] tArray = t.split("/");
                        boolean typeIsAccepted = tArray[0].equals("*") || tArray[0].isEmpty() || tArray[0].equals(mimeType);
                        boolean subTypeIsAccepted = tArray[1].equals("*") || tArray[1].isEmpty() || tArray[1].equals(mimeSubType);
                        return typeIsAccepted && subTypeIsAccepted;
                    } else {
                        return t.equals("*") || t.isEmpty() || t.equals(mimeType) || t.equals(mimeSubType);
                    }
                }).findFirst().orElseThrow(() -> new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Unsupported mime type " + mimeType + ". Supported types:" + String.join(";", supportedMimeTypes)));
                //And then continue to read IS as usual
                resourceStream = cachedReadInputStream.toUnreadPushbackInputStream();
            }
            //Wrap to count bytes and limit if needed
            Integer fileSizeLimit = getFileSizeLimit(params);
            CountedLimitedSizeInputStream sizeLimitedStream = new CountedLimitedSizeInputStream(resourceStream, fileSizeLimit,0);
            saveResource(resourceKey, sizeLimitedStream, params);
            return sizeLimitedStream.bytesRead();
        } catch (CountedLimitedSizeInputStream.SizeExceededException ex) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Resource size limit " + ex.getLimit() + " exceeded (" + ex.getBytesRead() + ")");
        } catch (Throwable t) {
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, t.getMessage());
        }
    }

    /**
     * Deletes a resource identified by the given resource key and optional parameters.
     *
     * @param resourceKey the unique key identifying the resource to be deleted
     * @param params      a map of additional parameters required for the deletion process
     * @throws ServiceException if there is an error during the deletion process
     */
    protected abstract void deleteResource(String resourceKey, HashMap<String, String> params) throws ServiceException;

}
