package org.twins.core.featurer.resource;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.featurer.FeaturerTwins;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.UUID;
/**
 * Provides services for handling file uploads and resource management.
 * Includes functionalities for saving, retrieving, and deleting files, as well as validating file size and MIME types.
 */
@FeaturerType(id = FeaturerTwins.TYPE_29,
        name = "StorageResourceService",
        description = "Services for resource(file) uploading")
public interface StorageFileService {
    /**
     * Constructs and returns the URI for the file controller based on the provided parameters and context.
     *
     * @param params  a map of string key-value pairs containing file-specific parameters.
     * @param context a map of string-object pairs containing additional contextual information.
     * @return the URI of the file controller as a string.
     * @throws ServiceException if an error occurs while constructing the URI.
     */
    String getFileControllerUri(HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException;

    /**
     * Provides a mechanism to retrieve a file as an InputStream using the given file key and parameters.
     * This method allows fetching file content from respective storage or source
     * based on the implementation of the service.
     *
     * @param fileKey The unique key or identifier corresponding to the file.
     * @param params  A map containing additional parameters required to locate or fetch the file.
     * @param context A map containing contextual information for retrieving the file.
     * @return An InputStream representing the file's content.
     * @throws ServiceException If there is an error while retrieving the file or if the file cannot be found.
     */
    InputStream getFileAsStream(String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException;

    /**
     * Retrieves the content of a file as a byte array based on the provided file key,
     * parameters, and context. This method interacts with the storage service to fetch
     * the file data.
     *
     * @param fileKey A unique identifier or key corresponding to the specific file.
     * @param params  A map of string key-value pairs containing additional parameters
     *                required to locate or fetch the file.
     * @param context A map containing contextual information or metadata which might be
     *                necessary for retrieving the file.
     * @return A byte array representing the content of the file.
     * @throws ServiceException If there is an error fetching the file or if the file
     *                          cannot be found in the storage service.
     */
    byte[] getFileBytes(String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException;

    /**
     * Constructs and returns a URI for the specified file based on its identifier, key, parameters, and context.
     *
     * @param fileId  the unique identifier of the file.
     * @param fileKey a string key identifying the file.
     * @param params  a map of string key-value pairs containing file-specific parameters.
     * @param context a map of string-object pairs containing contextual information for the operation.
     * @return a {@code URI} representing the file's location.
     * @throws ServiceException if there is an error while constructing the URI.
     */
    URI getFileUri(UUID fileId, String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException;

    /**
     * Adds a file to the storage service with the specified file identifier, file content, parameters, and context.
     * This method returns an {@code AddedFileKey} object that contains the unique file key and file size.
     *
     * @param fileId  the unique identifier of the file to be added.
     * @param file    a byte array representing the content of the file to be added.
     * @param params  a map of string key-value pairs containing file-specific parameters required for the operation.
     * @param context a map containing additional contextual information or metadata.
     * @return an {@code AddedFileKey} object containing the unique file key and file size.
     * @throws ServiceException if there is an error during the file addition process.
     */
    AddedFileKey addFile(UUID fileId, byte[] file, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException;

    /**
     * Adds a file to the storage service with the specified file identifier, file stream, parameters, and context.
     * Returns an {@code AddedFileKey} containing the unique key and size of the added file.
     *
     * @param fileId     the unique identifier of the file to be added.
     * @param fileStream an InputStream representing the content of the file to be added.
     * @param params     a map containing file-specific parameters required for the operation.
     * @param context    a map containing additional contextual information or metadata.
     * @return an {@code AddedFileKey} object containing the unique file key and file size.
     * @throws ServiceException if there is an error during the file addition process.
     */
    AddedFileKey addFile(UUID fileId, InputStream fileStream, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException;

    /**
     * Attempts to delete a file identified by the provided file key. This method is designed
     * to handle potential failures in the deletion process gracefully and may not throw
     * exceptions, even if the deletion is unsuccessful. The exact behavior of the method
     * depends on the underlying implementation in the storage service.
     *
     * @param fileKey A unique identifier for the file to be deleted.
     * @param params  A HashMap containing additional parameters needed for file deletion.
     * @param context A HashMap containing contextual information or metadata necessary
     *                for the delete operation.
     */
    void tryDeleteFile(String fileKey, HashMap<String, String> params, HashMap<String, Object> context);

    /**
     * Deletes the specified file based on the provided file key, parameters, and context.
     * This method is intended to handle the deletion of a file in implementations of storage services.
     *
     * @param fileKey A unique identifier for the file to be deleted.
     * @param params  A HashMap containing service-specific parameters required for file deletion.
     * @param context A HashMap containing additional context or metadata required for the operation.
     * @throws ServiceException If there is an issue during the deletion process.
     */
    void deleteFile(String fileKey, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException;

    /**
     * Generates a unique file key based on the provided file identifier, parameters, and contextual data.
     * This method is abstract and must be implemented by subclasses to define specific key generation logic.
     *
     * @param fileId  the unique identifier of the file for which the key is being generated
     * @param params  a map containing parameters required for generating the file key
     * @param context a map containing contextual information used in the key generation process
     * @return a string representing the generated file key
     * @throws ServiceException if an error occurs during the key generation process
     */
    String generateFileKey(UUID fileId, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException;
}
