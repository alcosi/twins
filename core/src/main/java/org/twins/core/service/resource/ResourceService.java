package org.twins.core.service.resource;

import lombok.RequiredArgsConstructor;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.resource.ResourceRepository;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.file.DomainFile;
import org.twins.core.featurer.resource.AddedFileKey;
import org.twins.core.featurer.resource.StoragerFileService;
import org.twins.core.service.auth.AuthService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ResourceService extends EntitySecureFindServiceImpl<ResourceEntity> {
    protected final FeaturerService featurerService;
    protected final ResourceRepository repository;
    protected final StorageService storageService;
    protected final AuthService authService;

    /**
     * Retrieves a resource file from storage based on the specified resource ID and access permissions.
     *
     * @param resourceId the unique identifier of the resource to retrieve
     * @param checkMode  the access control mode that determines how entity permissions are validated
     * @return a {@code DomainFile} object containing the file's data, original file name, and size information
     * @throws ServiceException if the resource with the specified {@code resourceId} is not found or any error occurs during file retrieval
     */
    @Transactional(readOnly = true)
    public DomainFile getResourceFile(UUID resourceId, EntitySmartService.ReadPermissionCheckMode checkMode) throws ServiceException {
        var resource = switch (checkMode) {
            case none -> repository.findById(resourceId, ResourceEntity.class);
            default ->
                    findEntity(resourceId, EntitySmartService.FindMode.ifEmptyNull, checkMode, EntitySmartService.EntityValidateMode.none);
        };
        if (resource == null) {
            throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "Resource " + resourceId + " not found!");
        }
        StorageEntity storage = resource.getStorage();
        StoragerFileService fileService = featurerService.getFeaturer(storage.getStorageFeaturer(), StoragerFileService.class);
        var stream = fileService.getFileAsStream(resource.getStorageFileKey(), storage.getStorageParams());
        return new DomainFile(stream, resource.getOriginalFileName(), resource.getSizeInBytes());
    }

    /**
     * Adds a new resource to the system using the provided file information and storage details.
     * The method creates a new resource entity backed by the specified external resource URI and associates
     * it with the given storage and domain.
     *
     * @param originalFileName    The original name of the file to be associated with the resource.
     * @param externalResourceUri The URI of the external resource to be linked or downloaded (depends on storager type) with the storage.
     * @param storagerId          The unique identifier of the storage entity where the resource will be stored.
     * @param domainId            The unique identifier of the domain to which the resource belongs.
     * @return A newly created {@code ResourceEntity} representing the
     */
    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public ResourceEntity addResource(String originalFileName, String externalResourceUri, UUID storagerId, UUID domainId) throws ServiceException {
        UUID resourceId = UUID.randomUUID();
        ApiUser apiUser = null;
        UUID userId = apiUser == null ? null : apiUser.isUserSpecified() ? apiUser.getUserId() : null;
        StorageEntity storage = storageService.findEntity(storagerId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.none, EntitySmartService.EntityValidateMode.none);
        StoragerFileService fileService = featurerService.getFeaturer(storage.getStorageFeaturer(), StoragerFileService.class);
        AddedFileKey addedFileKey = fileService.addExternalUrlFile(resourceId, externalResourceUri, storage.getStorageParams());
        return createResource(originalFileName, storagerId, domainId, resourceId, userId, addedFileKey, storage);
    }

    /**
     * Adds a new resource using the provided file name, file content, storage identifier, and domain identifier.
     *
     * @param originalFileName the original name of the file to be stored
     * @param bytes            the content of the file as a byte array
     * @param storagerId       the unique identifier for the storage location
     * @param domainId         the unique identifier for the domain associated with the resource
     * @return the newly created ResourceEntity object
     * @throws ServiceException if an error occurs while adding the resource
     */
    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public ResourceEntity addResource(String originalFileName, byte[] bytes, UUID storagerId, UUID domainId) throws ServiceException {
        return addResource(originalFileName, new ByteArrayInputStream(bytes), storagerId, domainId);
    }

    /**
     * Adds a new resource to the storage service.
     *
     * @param originalFileName The original name of the file being uploaded.
     * @param inputStream      The InputStream of the file to be uploaded.
     * @param storagerId       The UUID of the storage entity where the file will be stored.
     * @param domainId         The UUID of the domain associated with this resource.
     * @return A {@link ResourceEntity} representing the newly created resource.
     * @throws ServiceException If an error occurs during the resource addition process.
     */
    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public ResourceEntity addResource(String originalFileName, InputStream inputStream, UUID storagerId, UUID domainId) throws ServiceException {
        UUID resourceId = UUID.randomUUID();
        ApiUser apiUser = null;
        UUID userId = apiUser == null ? null : apiUser.isUserSpecified() ? apiUser.getUserId() : null;
        StorageEntity storage = storageService.findEntity(storagerId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.none, EntitySmartService.EntityValidateMode.none);

        StoragerFileService fileService = featurerService.getFeaturer(storage.getStorageFeaturer(), StoragerFileService.class);
        AddedFileKey addedFileKey = fileService.addFile(resourceId, inputStream, storage.getStorageParams());
        return createResource(originalFileName, storagerId, domainId, resourceId, userId, addedFileKey, storage);
    }

    protected ResourceEntity createResource(String originalFileName, UUID storagerId, UUID domainId, UUID resourceId, UUID userId, AddedFileKey addedFileKey, StorageEntity storage) throws ServiceException {
        ResourceEntity resource = new ResourceEntity();
        resource.setId(resourceId);
        resource.setStorageId(storagerId);
        resource.setDomainId(domainId);
        resource.setOriginalFileName(originalFileName);
        resource.setUploadedByUserId(userId);
        resource.setSizeInBytes(addedFileKey.fileSize());
        resource.setStorageFileKey(addedFileKey.fileKey());
        isEntityReadDenied(resource, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
        ResourceEntity saved = repository.save(resource);
        saved.setStorage(storage);
        return saved;
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
        StoragerFileService fileService = featurerService.getFeaturer(storage.getStorageFeaturer(), StoragerFileService.class);
        fileService.tryDeleteFile(resource.getStorageFileKey(), storage.getStorageParams());
        repository.delete(resource);

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
        StorageEntity oldStorage = resource.getStorage();
        StorageEntity newStorage = storageService.findEntitySafe(newStorageId);

        StoragerFileService oldFileService = featurerService.getFeaturer(oldStorage.getStorageFeaturer(), StoragerFileService.class);
        StoragerFileService newFileService = featurerService.getFeaturer(newStorage.getStorageFeaturer(), StoragerFileService.class);
        InputStream fileStream = oldFileService.getFileAsStream(resource.getStorageFileKey(), oldStorage.getStorageParams());
        AddedFileKey addedFileKey = newFileService.addFile(newResourceId, fileStream, newStorage.getStorageParams());
        ResourceEntity newResource = new ResourceEntity();
        newResource.setId(resourceId);
        newResource.setStorageId(newStorageId);
        newResource.setDomainId(resource.getDomainId());
        newResource.setOriginalFileName(resource.getOriginalFileName());
        newResource.setUploadedByUserId(resource.getUploadedByUserId());
        newResource.setSizeInBytes(addedFileKey.fileSize());
        newResource.setStorageFileKey(addedFileKey.fileKey());
        isEntityReadDenied(resource, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
        ResourceEntity saved = repository.save(resource);
        oldFileService.tryDeleteFile(resource.getStorageFileKey(), oldStorage.getStorageParams());
        repository.delete(resource);
        return saved;
    }


    @Override
    public CrudRepository<ResourceEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<ResourceEntity, UUID> entityGetIdFunction() {
        return ResourceEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(ResourceEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(ResourceEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

}
