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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@FeaturerType(id = FeaturerTwins.TYPE_29,
        name = "StorageResourceService",
        description = "Services for resource(file) uploading")
@Slf4j
public abstract class StorageFileService extends FeaturerTwins {
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
    protected String getFileControllerUri(HashMap<String, String> params) throws ServiceException {
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
     * Retrieves a resource as an InputStream based on the provided file key
     * and additional parameters.
     *
     * @param fileKey the key identifying the requested file
     * @param params      a map of parameters that may influence the resource retrieval process
     * @return an InputStream to access the resource, or null if the resource could not be found
     * @throws ServiceException if an error occurs during resource retrieval
     */
    public abstract InputStream getFileAsStream(String fileKey, HashMap<String, String> params) throws ServiceException;

    /**
     * Retrieves the byte content of a resource based on its key and optional parameters.
     *
     * @param fileKey the key identifying the file to be retrieved
     * @param params      a map of additional parameters relevant to the resource retrieval
     * @return a byte array representing the content of the resource
     * @throws ServiceException if an I/O error occurs during reading the resource
     */
    @SneakyThrows
    public byte[] getFileBytes(String fileKey, HashMap<String, String> params) throws ServiceException {
        try (InputStream stream = getFileAsStream(fileKey, params)) {
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
    public URI getFileUri(UUID resourceId, HashMap<String, String> params) throws ServiceException {
        return URI.create(getFileControllerUri(params) + resourceId);
    }

    /**
     * Saves a resource consisting of a resource key, an input stream, and additional parameters.
     *
     * @param fileKey    The unique key used to identify the file. Cannot be null or empty.
     * @param fileStream The input stream containing the data of the file to be saved. Cannot be null.
     * @param params         A map of additional parameters required for saving the resource (e.g., configuration details). Optional parameter, can be null.
     * @throws ServiceException If an error occurs during the process of saving the resource.
     */
    public Long saveFile(String fileKey, InputStream fileStream, HashMap<String, String> params) throws ServiceException {
        return saveResourceInternal(fileKey, fileStream, params);
    }

    /**
     * Saves a file into the storage system with specified parameters.
     * Validates the file size against a defined limit and throws an exception if exceeded.
     *
     * @param fileKey A unique key identifying the file.
     * @param file    The byte array representing the file content to be saved.
     * @param params      A map of string key-value pairs containing additional parameters for file saving operations.
     * @throws ServiceException If the file size exceeds the limit or if a service-related error occurs during the save operation.
     */
    public Long saveFile(String fileKey, byte[] file, HashMap<String, String> params) throws ServiceException {
        Integer fileSizeLimit = getFileSizeLimit(params);
        if (file.length > fileSizeLimit) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "File size limit " + fileSizeLimit + " exceeded (" + file.length + ")");
        }
        return saveFile(fileKey, new ByteArrayInputStream(file), params);
    }
    /**
     * Attempts to delete a file identified by the provided key and parameters.
     * Logs any errors encountered during the deletion process.
     *
     * @param fileKey the key that uniquely identifies the resource to be deleted
     * @param params a map of additional parameters required for file deletion
     */
    public void tryDeleteFile(String fileKey, HashMap<String, String> params){
        try {
            deleteFile(fileKey, params);
        }catch (Throwable t){
            log.error("Error deleting file: {}",fileKey,t);
        }
    }
    /**
     * Deletes a file identified by the given file key and optional parameters.
     *
     * @param fileKey the unique key identifying the file to be deleted
     * @param params      a map of additional parameters required for the deletion process
     * @throws ServiceException if there is an error during the deletion process
     */
    public abstract void deleteFile(String fileKey, HashMap<String, String> params) throws ServiceException;
    /**
     * Saves a resource internally after validating its MIME type and size constraints.
     *
     * This method ensures the resource's MIME type is supported based on the provided parameters.
     * It also limits the file size and throws exceptions if constraints are violated or in case of unexpected failures.
     *
     * @param fileKey The key associated with the resource to be saved.
     * @param fileStream The input stream of the file content being saved.
     * @param params Additional parameters for saving the resource, such as file size limits or supported MIME types.
     * @return The number of bytes read from the resource stream during the save operation.
     * @throws ServiceException If the resource's MIME type is unsupported, its size exceeds the allowed limit,
     *                           or an unexpected error occurs during the save operation.
     */
    protected Long saveResourceInternal(String fileKey, InputStream fileStream, HashMap<String, String> params) throws ServiceException {
        try {
            Set<String> supportedMimeTypes = getSupportedMimeTypes(params);
            if (!supportedMimeTypes.isEmpty()) {
                //Not to read IS twice, we have to cache already readen bytes by mime type resolving
                CacheReadenInputStream cachedReadInputStream = new CacheReadenInputStream(fileStream, false,Short.MAX_VALUE);
                checkFileMimeType(cachedReadInputStream, supportedMimeTypes);
                //And then continue to read IS as usual
                fileStream = cachedReadInputStream.toUnreadPushbackInputStream();
            }
            //Wrap to count bytes and limit if needed
            Integer fileSizeLimit = getFileSizeLimit(params);
            CountedLimitedSizeInputStream sizeLimitedStream = new CountedLimitedSizeInputStream(fileStream, fileSizeLimit,0);
            saveFile(fileKey, sizeLimitedStream, params);
            return sizeLimitedStream.bytesRead();
        } catch (CountedLimitedSizeInputStream.SizeExceededException ex) {
            tryDeleteFile(fileKey,params);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "File size limit " + ex.getLimit() + " exceeded (" + ex.getBytesRead() + ")");
        } catch (Throwable t) {
            tryDeleteFile(fileKey,params);
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, t.getMessage());
        }
    }

    /**
     * Validates the MIME type of a file against a set of supported MIME types.
     * If the MIME type of the file detected from the input stream is not supported,
     * a ServiceException will be thrown.
     *
     * @param cachedReadInputStream the input stream of the file whose MIME type is to be checked
     * @param supportedMimeTypes a set of MIME types accepted for validation
     * @throws IOException if an I/O error occurs while reading the input stream
     * @throws ServiceException if the MIME type of the input file is not supported
     */
    protected void checkFileMimeType(CacheReadenInputStream cachedReadInputStream, Set<String> supportedMimeTypes) throws IOException, ServiceException {
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
    }



}
