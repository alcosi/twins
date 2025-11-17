package org.twins.core.featurer.storager;

import io.github.breninsul.io.service.stream.inputStream.CacheReadenInputStream;
import io.github.breninsul.io.service.stream.inputStream.CountedLimitedSizeInputStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypes;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamWordList;

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

    @FeaturerParam(name = "fileSizeLimit", description = "Maximum file size to save.\nSet -1 or to prevent check.",
            optional = true,
            defaultValue = "-1",
            exampleValues = {"-1", "5242880", "1000000"}
    )
    public static final FeaturerParamInt fileSizeLimit = new FeaturerParamInt("fileSizeLimit");

    @FeaturerParam(name = "supportedMimeTypes", description = "List of supported mime types.\nSet empty list to prevent check.",
            optional = true,
            defaultValue = "*",
            exampleValues = {"image/*", "image/png,image/webp", "*/png", "*/webp", "*", "*/*"})
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
        Set<String> allMediaTypes = Set.of("*", "*/*");
        if (list.stream().anyMatch(allMediaTypes::contains)) {
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
            var fileInfo = checkMimeTypeAndCacheStream(fileStream, params);
            fileKey = fileKey + fileInfo.extension;
            fileStream = fileInfo.fileStream;

            return addFileInternal(fileKey, fileStream, fileInfo.mimeType, params);
        } catch (CountedLimitedSizeInputStream.SizeExceededException ex) {
            tryDeleteFile(fileKey, params);
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "File size limit " + ex.getLimit() + " exceeded (" + ex.getBytesRead() + ")");
        } catch (Throwable t) {
            log.error("Error adding file: {}", fileKey, t);
            tryDeleteFile(fileKey, params);
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, t.getMessage());
        }
    }

    protected FileInputStreamWithMetaInfo checkMimeTypeAndCacheStream(InputStream fileStream, HashMap<String, String> params) throws IOException, ServiceException {
        Set<String> supportedMimeTypes = getSupportedMimeTypes(params);
        FileInputStreamWithMetaInfo result = getFileInputStreamWithMetaInfo(fileStream);
        if (!supportedMimeTypes.isEmpty()) {
            //Not to read IS twice, we have to cache already readen bytes by mime type resolving
            checkFileMimeType(result.mimeType(), supportedMimeTypes);
        }
        return result;
    }

    @SneakyThrows
    protected FileInputStreamWithMetaInfo getFileInputStreamWithMetaInfo(InputStream fileStream) throws IOException {
        CacheReadenInputStream cachedReadInputStream = null;
        if (fileStream instanceof CacheReadenInputStream) {
            cachedReadInputStream = (CacheReadenInputStream) fileStream;
        } else {
            cachedReadInputStream = new CacheReadenInputStream(fileStream, false, Short.MAX_VALUE);
        }
        String mimeType = tika.detect(cachedReadInputStream);
        String extension = "unknown";
        try {
            var mimeTypeInfo = MimeTypes.getDefaultMimeTypes().forName(mimeType);
            extension = mimeTypeInfo.getExtension();
        } catch (Exception e) {
            log.warn("Error parsing mime type: {}", mimeType, e);
        }
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        //And then continue to read IS as usual
        fileStream = cachedReadInputStream.toUnreadPushbackInputStream();
        FileInputStreamWithMetaInfo result = new FileInputStreamWithMetaInfo(fileStream, mimeType, extension);
        return result;
    }

    public static record FileInputStreamWithMetaInfo(InputStream fileStream, String mimeType, String extension) {
    }

    /**
     * Abstract method to handle the internal addition of a file to the storage system. Implementations are
     * expected to define how the file input stream, along with associated parameters and context, are handled
     * to store the file in a particular storage medium (e.g., local storage, external URI, etc.).
     *
     * @param fileKey    the unique key used to identify the file in the storage system
     * @param fileStream the input stream representing the file content to be stored
     * @param mimeType   the MIME type of the file being stored, if known; otherwise, set to null or empty string.
     * @param params     a map of additional parameters required for the storage operation
     * @return An instance of {@code AddedFileKey} that contains information about the uploaded file, such as its key and size.
     * @throws ServiceException if any error occurs during the file addition process
     */
    protected abstract AddedFileKey addFileInternal(String fileKey, InputStream fileStream, String mimeType, HashMap<String, String> params) throws ServiceException;

    /**
     * Validates whether a given raw MIME type is supported based on a set of supported MIME types.
     * The method splits the raw MIME type into its primary type and sub-type, then checks if the
     * provided MIME type matches any of the supported patterns. If no match is found, a
     * {@link ServiceException} is thrown with details about the unsupported type.
     *
     * @param mimeTypeRaw        the raw MIME type as a string, typically in the form of "type/subtype".
     * @param supportedMimeTypes a set of supported MIME types, where each entry can be in the
     *                           form of "type/subtype", "type/*", or "*".
     * @throws IOException      if an I/O error occurs during the processing.
     * @throws ServiceException if the MIME type is not supported by the provided set.
     */
    protected void checkFileMimeType(String mimeTypeRaw, Set<String> supportedMimeTypes) throws IOException, ServiceException {
        String[] mimeTypeArray = mimeTypeRaw.split("/");
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
