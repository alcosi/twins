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
import org.jetbrains.annotations.NotNull;
import org.twins.core.featurer.FeaturerTwins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides services for handling file uploads and resource management.
 * Includes functionalities for saving, retrieving, and deleting files, as well as validating file size and MIME types.
 */
@FeaturerType(id = FeaturerTwins.TYPE_29,
        name = "StorageResourceService",
        description = "Services for resource(file) uploading")
@Slf4j
public abstract class AbstractStorageFileService extends FeaturerTwins {
    protected final Tika tika = new Tika();

    @FeaturerParam(name = "selfHostDomainBaseUri", description = "external URI/domain of twins application to create resource links")
    public static final FeaturerParamString selfHostDomainBaseUri = new FeaturerParamString("selfHostDomainBaseUri");
    @FeaturerParam(name = "fileSizeLimit", description = "Limit of file size")
    public static final FeaturerParamInt fileSizeLimit = new FeaturerParamInt("fileSizeLimit");
    @FeaturerParam(name = "supportedMimeTypes", description = "List of supported mime types")
    public static final FeaturerParamWordList supportedMimeTypes = new FeaturerParamWordList("supportedMimeTypes");


    /**
     * Constructs and returns the URI for the file controller based on the provided parameters and context.
     *
     * @param params  a map of string key-value pairs containing file-specific parameters.
     * @param context a map of string-object pairs containing additional contextual information.
     * @return the URI of the file controller as a string.
     * @throws ServiceException if an error occurs while constructing the URI.
     */
    protected abstract String getFileControllerUri(HashMap<String, String> params,HashMap<String, Object> context) throws ServiceException;

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
     * Provides a mechanism to retrieve a file as an InputStream using the given file key and parameters.
     * This method allows fetching file content from respective storage or source
     * based on the implementation of the service.
     *
     * @param fileKey The unique key or identifier corresponding to the file.
     * @param params A map containing additional parameters required to locate or fetch the file.
     * @param context A map containing contextual information for retrieving the file.
     * @return An InputStream representing the file's content.
     * @throws ServiceException If there is an error while retrieving the file or if the file cannot be found.
     */
    public abstract InputStream getFileAsStream(String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException;

    /**
     * Retrieves the contents of a file as a byte array.
     *
     * @param fileKey The unique key or identifier of the file to retrieve.
     * @param params A set of key-value pair parameters used for file retrieval,
     *               such as configurations or contextual information.
     * @param context A map of additional context-specific objects that could assist
     *                in retrieving the file, such as environment settings or metadata.
     * @return A byte array representing the content of the requested file.
     * @throws ServiceException If an error occurs while retrieving or processing the file.
     */
    @SneakyThrows
    public byte[] getFileBytes(String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        try (InputStream stream = getFileAsStream(fileKey, params,context)) {
            return stream.readAllBytes();
        }
    }

    /**
     * Retrieves the URI of a file based on the provided file identifier, file key, parameters,
     * and context. This method constructs a URI using the file controller's URI and the file identifier.
     *
     * @param fileId the unique identifier of the file
     * @param fileKey the key associated with the file
     * @param params a map of parameters used for file configuration and URI generation
     * @param context a map of additional context information required for URI generation
     * @return the URI of the file
     * @throws ServiceException if any error occurs during the URI generation
     */
    public URI getFileUri(UUID fileId,String fileKey, HashMap<String, String> params,HashMap<String, Object> context) throws ServiceException {
        return URI.create(getFileControllerUri(params,context) + fileId);
    }

    /**
     * Adds a file to the storage based on the provided file identifier, file content,
     * parameters, and context. Validates if the file size does not exceed the limit
     * specified within the parameters. If the file size exceeds the limit, a
     * {@link ServiceException} is thrown.
     *
     * @param fileId Unique identifier for the file being added.
     * @param file Byte array representing the content of the file.
     * @param params A map of parameters that may include file-related metadata or configuration.
     * @param context A map containing context-specific information for the file operation.
     * @return An {@link AddedFileKey} object containing information about the stored file, such as its key and size.
     * @throws ServiceException If the file size exceeds the allowed limit or if any storage operation fails.
     */
    public AddedFileKey addFile(UUID fileId, byte[] file, HashMap<String, String> params,HashMap<String, Object> context) throws ServiceException {
        Integer fileSizeLimit = getFileSizeLimit(params);
        if (file.length > fileSizeLimit) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "File size limit " + fileSizeLimit + " exceeded (" + file.length + ")");
        }
        return addFile(fileId, new ByteArrayInputStream(file), params,context);
    }
    /**
     * Adds a file to the storage system.
     *
     * @param fileId The unique identifier of the file to be uploaded.
     * @param fileStream The input stream containing the file data.
     * @param params A map of string parameters providing additional information or configuration for the file upload.
     * @param context A map of contextual objects to provide extended information used during the file processing.
     * @return An instance of {@code AddedFileKey} that contains information about the uploaded file, such as its key and size.
     * @throws ServiceException If an error occurs while adding the file to the storage system.
     */
    public AddedFileKey addFile(UUID fileId, InputStream fileStream, HashMap<String, String> params,HashMap<String, Object> context) throws ServiceException {
        return addFileInternal(fileId, fileStream, params,context);
    }

    /**
     * Attempts to delete a file using the provided file key, parameters, and context.
     * If the deletion fails, logs the error with the file key and exception details.
     *
     * @param fileKey the unique identifier of the file to be deleted
     * @param params a map containing parameters related to the file deletion
     * @param context a map containing contextual information for file deletion
     */
    public void tryDeleteFile(String fileKey, HashMap<String, String> params, HashMap<String, Object> context){
        try {
            deleteFile(fileKey, params,context);
        }catch (Throwable t){
            log.error("Error deleting file: {}",fileKey,t);
        }
    }

    /**
     * Deletes the specified file based on the provided file key, parameters, and context.
     * This method is intended to handle the deletion of a file in implementations of storage services.
     *
     * @param fileKey A unique identifier for the file to be deleted.
     * @param params A HashMap containing service-specific parameters required for file deletion.
     * @param context A HashMap containing additional context or metadata required for the operation.
     * @throws ServiceException If there is an issue during the deletion process.
     */
    public abstract void deleteFile(String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException;

    /**
     * Adds a file to the internal storage with specific parameters and context.
     * The method validates the mime type of the file, ensures it conforms to size limits, and stores it properly.
     * If an error occurs, it attempts to delete the file and throws an appropriate exception.
     *
     * @param fileId       The unique identifier of the file to be added.
     * @param fileStream   The {@link InputStream} of the file to be processed for storage.
     * @param params       The parameters for file handling, including additional configurations.
     * @param context      The context map providing necessary contextual data for file handling.
     * @return An {@link AddedFileKey} containing the file key and the size of the successfully added file.
     * @throws ServiceException If any error occurs during file handling, such as exceeding size limits or unexpected issues.
     */
    protected AddedFileKey addFileInternal(UUID fileId, InputStream fileStream, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        String fileKey=generateFileKey(fileId,params,context);
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
            addFileInternal(fileKey, sizeLimitedStream, params,context);
            return new AddedFileKey(fileKey,sizeLimitedStream.bytesRead());
        } catch (CountedLimitedSizeInputStream.SizeExceededException ex) {
            tryDeleteFile(fileKey,params,context);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "File size limit " + ex.getLimit() + " exceeded (" + ex.getBytesRead() + ")");
        } catch (Throwable t) {
            tryDeleteFile(fileKey,params,context);
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, t.getMessage());
        }
    }

    /**
     * Abstract method to handle the internal addition of a file to the storage system. Implementations are
     * expected to define how the file input stream, along with associated parameters and context, are handled
     * to store the file in a particular storage medium (e.g., local storage, external URI, etc.).
     *
     * @param fileKey the unique key used to identify the file in the storage system
     * @param fileStream the input stream representing the file content to be stored
     * @param params a map of additional parameters required for the storage operation
     * @param context a map of contextual information required for the operation, which may include metadata or configuration data
     * @throws ServiceException if any error occurs during the file addition process
     */
    protected abstract void addFileInternal( String fileKey, InputStream fileStream, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException ;
    /**
     * Generates a unique file key based on the provided file identifier, parameters, and contextual data.
     * This method is abstract and must be implemented by subclasses to define specific key generation logic.
     *
     * @param fileId   the unique identifier of the file for which the key is being generated
     * @param params   a map containing parameters required for generating the file key
     * @param context  a map containing contextual information used in the key generation process
     * @return a string representing the generated file key
     * @throws ServiceException if an error occurs during the key generation process
     */
    public abstract String generateFileKey(UUID fileId,HashMap<String, String> params,HashMap<String, Object> context) throws ServiceException;
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

    @NotNull
    protected String addSlashAtTheEndIfNeeded(String path) {
        if (!path.endsWith("/")){
            path = path +"/";
        }
        return path;
    }

}