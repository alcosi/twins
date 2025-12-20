package org.twins.core.service.resource;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponse;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.resource.ResourceRepository;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.storager.AddedFileKey;
import org.twins.core.featurer.storager.Storager;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.storage.StorageService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Function;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class ResourceService extends EntitySecureFindServiceImpl<ResourceEntity> {
    protected final FeaturerService featurerService;
    protected final ResourceRepository resourceRepository;
    protected final StorageService storageService;
    protected final AuthService authService;

    /**
     * Retrieves a resource file from storage based on the specified resource ID and access permissions.
     *
     * @param resourceId the unique identifier of the resource to retrieve
     * @return a {@code DomainFile} object containing the file's data, original file name, and size information
     * @throws ServiceException if the resource with the specified {@code resourceId} is not found or any error occurs during file retrieval
     */
    @Transactional(readOnly = true)
    public InputStreamResponse getResourceFile(UUID resourceId) throws ServiceException {
        var resource = findEntitySafe(resourceId);
        StorageEntity storage = resource.getStorage();
        Storager fileService = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
        var stream = fileService.getFileAsStream(resource.getStorageFileKey(), storage.getStoragerParams());
        return stream;
    }

    /**
     * Adds a new resource to the system using the provided file information and storage details.
     * The method creates a new resource entity backed by the specified external resource URI and associates
     * it with the given storage and domain.
     *
     * @param originalFileName    The original name of the file to be associated with the resource.
     * @param externalResourceUri The URI of the external resource to be linked or downloaded (depends on storager type) with the storage.
     * @return A newly created {@code ResourceEntity} representing the
     */
    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public ResourceEntity addResource(String originalFileName, String externalResourceUri) throws ServiceException {
        UUID resourceId = UUID.randomUUID();
        ApiUser apiUser = authService.getApiUser();
        StorageEntity storage = storageService.findEntitySafe(apiUser.getDomain().getResourcesStorageId());
        Storager storager = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
        AddedFileKey addedFileKey = storager.addExternalUrlFile(resourceId, externalResourceUri, storage.getStoragerParams());
        return createResource(storage, originalFileName, resourceId, addedFileKey);
    }

    /**
     * Adds a new resource using the provided file name, file content, storage identifier, and domain identifier.
     *
     * @param originalFileName the original name of the file to be stored
     * @param bytes            the content of the file as a byte array
     * @return the newly created ResourceEntity object
     * @throws ServiceException if an error occurs while adding the resource
     */
    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public ResourceEntity addResource(String originalFileName, byte[] bytes) throws ServiceException {
        return addResource(originalFileName, new ByteArrayInputStream(bytes));
    }

    /**
     * Adds a new resource to the storage service.
     *
     * @param originalFileName The original name of the file being uploaded.
     * @param inputStream      The InputStream of the file to be uploaded.
     * @return A {@link ResourceEntity} representing the newly created resource.
     * @throws ServiceException If an error occurs during the resource addition process.
     */
    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public ResourceEntity addResource(String originalFileName, InputStream inputStream) throws ServiceException {
        UUID resourceId = UUID.randomUUID();
        ApiUser apiUser = authService.getApiUser();
        StorageEntity storage = storageService.findEntitySafe(apiUser.getDomain().getResourcesStorageId());
        Storager fileService = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
        AddedFileKey addedFileKey = fileService.addFile(resourceId, inputStream, storage.getStoragerParams());
        return createResource(storage, originalFileName, resourceId, addedFileKey);
    }

    protected ResourceEntity createResource(StorageEntity storage, String originalFileName, UUID resourceId, AddedFileKey addedFileKey) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        ResourceEntity resource = new ResourceEntity()
                .setId(resourceId)
                .setStorageId(storage.getId())
                .setStorage(storage)
                .setDomainId(apiUser.getDomainId())
                .setOriginalFileName(originalFileName)
                .setUploadedByUserId(apiUser.getUserId())
                .setSizeInBytes(addedFileKey.fileSize())
                .setStorageFileKey(addedFileKey.fileKey());
        validateEntity(resource, EntitySmartService.EntityValidateMode.beforeSave);
        return resourceRepository.save(resource);
    }

    /**
     * Deletes the resource identified by the provided resourceId.
     * This method ensures the associated storage file is deleted
     * and then removes the resource entity from the repository.
     *
     * @param resourceId the unique identifier of the resource to be deleted
     * @throws ServiceException if an error occurs during the deletion process
     */
    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public void deleteResource(UUID resourceId) throws ServiceException {
        var resource = findEntitySafe(resourceId);
        StorageEntity storage = resource.getStorage();
        Storager storager = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
        storager.tryDeleteFile(resource.getStorageFileKey(), storage.getStoragerParams());
        entitySmartService.deleteAndLog(resource.getId(), resourceRepository);
    }

    /**
     * Transfers a resource from one storage location to another.
     *
     * @param resourceId   the unique identifier of the resource to be transferred
     * @param newStorageId the unique identifier of the target storage location
     * @return the newly transferred ResourceEntity with updated storage details
     * @throws ServiceException if an error occurs during transfer or if the resource/storage cannot be found
     */
    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public ResourceEntity transferResource(UUID resourceId, UUID newStorageId) throws ServiceException {
        UUID newResourceId = UUID.randomUUID();

        var resource = findEntitySafe(resourceId);
        if (resource.getStorageId().equals(newStorageId))
            return resource;
        StorageEntity oldStorage = resource.getStorage();
        StorageEntity newStorage = storageService.findEntitySafe(newStorageId);

        Storager oldStorager = featurerService.getFeaturer(oldStorage.getStorageFeaturerId(), Storager.class);
        Storager newStorager = featurerService.getFeaturer(newStorage.getStorageFeaturerId(), Storager.class);
        InputStreamResponse fileStream = oldStorager.getFileAsStream(resource.getStorageFileKey(), oldStorage.getStoragerParams());
        AddedFileKey addedFileKey = newStorager.addFile(newResourceId, fileStream.getContentStream(), newStorage.getStoragerParams());
        ResourceEntity newResource = new ResourceEntity()
                .setId(resourceId)
                .setStorageId(newStorageId)
                .setDomainId(resource.getDomainId())
                .setOriginalFileName(resource.getOriginalFileName())
                .setUploadedByUserId(resource.getUploadedByUserId())
                .setSizeInBytes(addedFileKey.fileSize())
                .setStorageFileKey(addedFileKey.fileKey());
        validateEntity(newResource, EntitySmartService.EntityValidateMode.beforeSave);
        newResource = resourceRepository.save(newResource);
        oldStorager.tryDeleteFile(resource.getStorageFileKey(), oldStorage.getStoragerParams());
        resourceRepository.delete(resource);
        return newResource;
    }

    public String getResourceUri(ResourceEntity resourceEntity) throws ServiceException {
        if (resourceEntity != null) {
            var featurer = featurerService.getFeaturer(resourceEntity.getStorage().getStorageFeaturerId(), Storager.class);
            return featurer.getFileUri(resourceEntity.getId(), resourceEntity.getStorageFileKey(), resourceEntity.getStorage().getStoragerParams()).toString();
        }
        return null;
    }


    @Override
    public CrudRepository<ResourceEntity, UUID> entityRepository() {
        return resourceRepository;
    }

    @Override
    public Function<ResourceEntity, UUID> entityGetIdFunction() {
        return ResourceEntity::getId;
    }

    /**
     * Always returns true, because it's "public" resources, that will be downloaded by UUID without domain check
     *
     * @param entity                  the resource entity to be checked for read permissions
     * @param readPermissionCheckMode the mode used for checking read permissions
     * @return true if read access to the entity is denied, false otherwise
     * @throws ServiceException if an error occurs during the permission check
     */
    @Override
    public boolean isEntityReadDenied(ResourceEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(ResourceEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

}
