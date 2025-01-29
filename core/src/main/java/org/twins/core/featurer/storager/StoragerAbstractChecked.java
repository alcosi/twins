package org.twins.core.featurer.storager;

import io.github.breninsul.io.service.stream.inputStream.CacheReadenInputStream;
import io.github.breninsul.io.service.stream.inputStream.CountedLimitedSizeInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamWordList;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract class providing a foundational implementation for file storage services
 * with additional checks on file properties such as size and MIME type.
 * This class implements {@link Storager} and
 */
@Slf4j
public abstract class StoragerAbstractChecked extends Storager {
    protected final Tika tika = new Tika();

    @FeaturerParam(name = "fileSizeLimit", description = "Limit of file size")
    public static final FeaturerParamInt fileSizeLimit = new FeaturerParamInt("fileSizeLimit");

    @FeaturerParam(name = "supportedMimeTypes", description = "List of supported mime types")
    public static final FeaturerParamWordList supportedMimeTypes = new FeaturerParamWordList("supportedMimeTypes");


    /**
     * Retrieves the file size limit for a specific operation based on the provided parameters and context.
     * This method fetches properties and extracts the file size limit using the feature service logic.
     *
     * @param params a map of string key-value pairs containing additional parameters required for fetching the file size limit.
     * @return the file size limit as an Integer if defined; otherwise, a null value.
     * @throws ServiceException if an error occurs during the extraction of properties or size limit computation.
     */
    protected Integer getFileSizeLimit(HashMap<String, String> params) throws ServiceException {
        Properties properties = extractProperties(params, false);
        return fileSizeLimit.extract(properties);
    }

    /**
     * Retrieves a set of supported MIME types based on the provided parameters and context.
     * This method processes the provided data to determine and return all valid MIME types
     * in a normalized form.
     *
     * @param params a map containing key-value pairs of parameters required for extracting MIME types.
     * @return a set of supported MIME types in lowercase, which is empty if no valid MIME types are found.
     * @throws ServiceException if an error occurs while extracting properties or processing MIME types.
     */
    protected Set<String> getSupportedMimeTypes(HashMap<String, String> params) throws ServiceException {
        Properties properties = extractProperties(params, false);
        List<String> list = supportedMimeTypes.extract(properties);
        if (list == null || list.isEmpty()) {
            return Collections.emptySet();
        }
        return list.stream().map(String::toLowerCase).collect(Collectors.toSet());
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
    @Override
    public AddedFileKey addFile(UUID fileId, byte[] file, HashMap<String, String> params) throws ServiceException {
        Integer fileSizeLimit = getFileSizeLimit(params);
        if (file.length > fileSizeLimit) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "File size limit " + fileSizeLimit + " exceeded (" + file.length + ")");
        }
        return super.addFile(fileId, file, params);
    }

    /**
     * Adds a file to the storage system.
     *
     * @param fileId     The unique identifier of the file to be uploaded.
     * @param fileStream The input stream containing the file data.
     * @param params     A map of string parameters providing additional information or configuration for the file upload.
     * @return An instance of {@code AddedFileKey} that contains information about the uploaded file, such as its key and size.
     * @throws ServiceException If an error occurs while adding the file to the storage system.
     */
    @Override
    public AddedFileKey addFile(UUID fileId, InputStream fileStream, HashMap<String, String> params) throws ServiceException {
        return checkAndAddFileInternal(fileId, fileStream, params);
    }


    /**
     * Adds a file to the internal storage with specific parameters and context.
     * The method validates the mime type of the file, ensures it conforms to size limits, and stores it properly.
     * If an error occurs, it attempts to delete the file and throws an appropriate exception.
     *
     * @param fileId     The unique identifier of the file to be added.
     * @param fileStream The {@link InputStream} of the file to be processed for storage.
     * @param params     The parameters for file handling, including additional configurations.
     * @return An {@link AddedFileKey} containing the file key and the size of the successfully added file.
     * @throws ServiceException If any error occurs during file handling, such as exceeding size limits or unexpected issues.
     */
    protected AddedFileKey checkAndAddFileInternal(UUID fileId, InputStream fileStream, HashMap<String, String> params) throws ServiceException {
        String fileKey = generateFileKey(fileId, params);
        try {
            fileStream = checkMimeTypeAndCacheStream(fileStream, params);
            //Wrap to count bytes and limit if needed
            Integer fileSizeLimit = getFileSizeLimit(params);
            CountedLimitedSizeInputStream sizeLimitedStream = new CountedLimitedSizeInputStream(fileStream, fileSizeLimit, 0);
            addFileInternal(fileKey, sizeLimitedStream, params);
            return new AddedFileKey(fileKey, sizeLimitedStream.bytesRead());
        } catch (CountedLimitedSizeInputStream.SizeExceededException ex) {
            tryDeleteFile(fileKey, params);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "File size limit " + ex.getLimit() + " exceeded (" + ex.getBytesRead() + ")");
        } catch (Throwable t) {
            log.error("Error adding file: {}", fileKey, t);
            tryDeleteFile(fileKey, params);
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, t.getMessage());
        }
    }

    protected InputStream checkMimeTypeAndCacheStream(InputStream fileStream, HashMap<String, String> params) throws IOException, ServiceException {
        Set<String> supportedMimeTypes = getSupportedMimeTypes(params);
        if (!supportedMimeTypes.isEmpty()) {
            //Not to read IS twice, we have to cache already readen bytes by mime type resolving
            CacheReadenInputStream cachedReadInputStream = new CacheReadenInputStream(fileStream, false, Short.MAX_VALUE);
            checkFileMimeType(cachedReadInputStream, supportedMimeTypes);
            //And then continue to read IS as usual
            fileStream = cachedReadInputStream.toUnreadPushbackInputStream();
        }
        return fileStream;
    }

    /**
     * Abstract method to handle the internal addition of a file to the storage system. Implementations are
     * expected to define how the file input stream, along with associated parameters and context, are handled
     * to store the file in a particular storage medium (e.g., local storage, external URI, etc.).
     *
     * @param fileKey    the unique key used to identify the file in the storage system
     * @param fileStream the input stream representing the file content to be stored
     * @param params     a map of additional parameters required for the storage operation
     * @throws ServiceException if any error occurs during the file addition process
     */
    protected abstract void addFileInternal(String fileKey, InputStream fileStream, HashMap<String, String> params) throws ServiceException;

    /**
     * Validates the MIME type of file against a set of supported MIME types.
     * If the MIME type of the file detected from the input stream is not supported,
     * a ServiceException will be thrown.
     *
     * @param cachedReadInputStream the input stream of the file whose MIME type is to be checked
     * @param supportedMimeTypes    a set of MIME types accepted for validation
     * @throws IOException      if an I/O error occurs while reading the input stream
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
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        return path;
    }

}
