package org.twins.core.service.resource;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponse;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.StringUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.resource.ResourceRepository;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.resource.ResourceCreate;
import org.twins.core.domain.resource.ResourceUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.storager.AddedFileKey;
import org.twins.core.featurer.storager.Storager;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.storage.StorageService;
import org.twins.core.service.user.UserService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class ResourceService extends EntitySecureFindServiceImpl<ResourceEntity> {

    protected final FeaturerService featurerService;
    protected final ResourceRepository resourceRepository;
    protected final StorageService storageService;
    protected final AuthService authService;
    @Lazy
    protected final UserService userService;

    /**
     * Retrieves a resource file from storage based on the specified resource ID and access permissions.
     *
     * @param resourceId the unique identifier of the resource to retrieve
     * @return a {@code DomainFile} object containing the file's data, original file name, and size information
     * @throws ServiceException if the resource with the specified {@code resourceId} is not found or any error occurs during file retrieval
     */
    @Transactional(readOnly = true)
    public InputStreamResponse getResourceFile(UUID resourceId) throws ServiceException {
        return getResourceFile(findEntitySafe(resourceId));
    }

    /**
     * Retrieves a resource file from storage, but only if the resource is marked as public
     * (see {@code resource.public} column). Non-public resources are reported as not found
     * to avoid leaking their existence.
     *
     * @param resourceId the unique identifier of the resource to retrieve
     * @return the resource file stream
     * @throws ServiceException if the resource is not found or is not public
     */
    @Transactional(readOnly = true)
    public InputStreamResponse getPublicResourceFile(UUID resourceId) throws ServiceException {
        var resource = findEntitySafe(resourceId);
        if (!resource.getIsPublic())
            throw new ServiceException(ErrorCodeTwins.RESOURCE_NOT_PUBLIC, "resource " + resourceId + " is not public");
        return getResourceFile(resource);
    }

    public InputStreamResponse getResourceFile(ResourceEntity resource) throws ServiceException {
        loadStorage(resource);
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
        return addResource(originalFileName, externalResourceUri, null);
    }

    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public ResourceEntity addResource(String originalFileName, String externalResourceUri, Boolean isPublic) throws ServiceException {
        UUID resourceId = UuidUtils.generate();
        ApiUser apiUser = authService.getApiUser();
        StorageEntity storage = storageService.findEntitySafe(apiUser.getDomain().getResourcesStorageId());
        Storager storager = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
        AddedFileKey addedFileKey = storager.addExternalUrlFile(resourceId, externalResourceUri, storage.getStoragerParams());
        return createResource(storage, originalFileName, resourceId, addedFileKey, isPublic);
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
        return addResource(originalFileName, bytes, null);
    }

    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public ResourceEntity addResource(String originalFileName, byte[] bytes, Boolean isPublic) throws ServiceException {
        return addResource(originalFileName, new ByteArrayInputStream(bytes), isPublic);
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
        return addResource(originalFileName, inputStream, null);
    }

    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public ResourceEntity addResource(String originalFileName, InputStream inputStream, Boolean isPublic) throws ServiceException {
        UUID resourceId = UuidUtils.generate();
        ApiUser apiUser = authService.getApiUser();
        StorageEntity storage = storageService.findEntitySafe(apiUser.getDomain().getResourcesStorageId());
        Storager fileService = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
        AddedFileKey addedFileKey = fileService.addFile(resourceId, inputStream, storage.getStoragerParams());
        return createResource(storage, originalFileName, resourceId, addedFileKey, isPublic);
    }

    /**
     * Batch creation of resources: files are stored in the domain resources storage, one storager
     * instance is resolved for the whole batch. Item name is the title override if set, otherwise
     * the uploaded file name / external uri.
     *
     * @param createList create items, each carrying a resolved file or an external uri
     * @return the created resources (in request order)
     * @throws ServiceException if an error occurs while adding any of the resources
     */
    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public List<ResourceEntity> createResources(List<ResourceCreate> createList) throws ServiceException {
        if (createList == null || createList.isEmpty())
            return Collections.emptyList();
        ApiUser apiUser = authService.getApiUser();
        StorageEntity storage = storageService.findEntitySafe(apiUser.getDomain().getResourcesStorageId());
        Storager storager = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
        List<ResourceEntity> result = new ArrayList<>(createList.size());
        for (ResourceCreate create : createList) {
            UUID resourceId = UuidUtils.generate();
            AddedFileKey addedFileKey;
            String fileName;
            if (create.getFile() != null) {
                addedFileKey = storager.addFile(resourceId, create.getFile().content(), storage.getStoragerParams());
                fileName = StringUtils.isNotEmpty(create.getTitle()) ? create.getTitle() : create.getFile().originalFileName();
            } else {
                addedFileKey = storager.addExternalUrlFile(resourceId, create.getExternalResourceUri(), storage.getStoragerParams());
                fileName = StringUtils.isNotEmpty(create.getTitle()) ? create.getTitle() : create.getExternalResourceUri();
            }
            result.add(createResource(storage, fileName, resourceId, addedFileKey, create.getIsPublic()));
        }
        return result;
    }

    protected ResourceEntity createResource(StorageEntity storage, String originalFileName, UUID resourceId, AddedFileKey addedFileKey, Boolean isPublic) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        ResourceEntity resource = new ResourceEntity()
                .setId(resourceId)
                .setStorageId(storage.getId())
                .setStorage(storage)
                .setDomainId(apiUser.getDomainId())
                .setOriginalFileName(originalFileName)
                .setUploadedByUserId(apiUser.getUserId())
                .setSizeInBytes(addedFileKey.fileSize())
                .setStorageFileKey(addedFileKey.fileKey())
                .setIsPublic(isPublic == null || isPublic);
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
        loadStorage(resource);
        StorageEntity storage = resource.getStorage();
        Storager storager = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
        storager.tryDeleteFile(resource.getStorageFileKey(), storage.getStoragerParams());
        entitySmartService.deleteAndLog(resource.getId(), resourceRepository);
    }

    /**
     * Batch update of editable resource attributes: original file name, public flag and file replacement.
     * Null fields keep the current values. When a file replacement is requested ({@link ResourceUpdate#getFile()}
     * or {@link ResourceUpdate#getExternalResourceUri()}), the new file is stored in the same storage, the old one
     * is deleted, and the resource name follows the new file name unless explicitly set on the update item.
     *
     * @param updates update items, each carrying the resource id
     * @return the updated resources (in request order)
     * @throws ServiceException if any resource id is not found
     */
    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public List<ResourceEntity> updateResources(Collection<ResourceUpdate> updates) throws ServiceException {
        if (updates == null || updates.isEmpty())
            return Collections.emptyList();
        Kit<ResourceEntity, UUID> entitiesKit = findEntitiesSafe(updates.stream().map(ResourceUpdate::getId).toList());
        // bulk-load storages for entities with file replacement
        List<ResourceEntity> withFileReplace = new ArrayList<>();
        for (ResourceUpdate update : updates)
            if (update.getFile() != null || update.getExternalResourceUri() != null)
                withFileReplace.add(entitiesKit.get(update.getId()));
        loadStorage(withFileReplace);
        ChangesHelperMulti<ResourceEntity> changes = new ChangesHelperMulti<>();
        List<ResourceEntity> allEntities = new ArrayList<>(updates.size());
        for (ResourceUpdate update : updates) {
            ResourceEntity entity = entitiesKit.get(update.getId());
            allEntities.add(entity);
            ChangesHelper changesHelper = new ChangesHelper();
            updateEntityFieldByValueIfNotNull(update.getOriginalFileName(), entity,
                    ResourceEntity::getOriginalFileName, ResourceEntity::setOriginalFileName, ResourceEntity.Fields.originalFileName, changesHelper);
            updateEntityFieldByValueIfNotNull(update.getIsPublic(), entity,
                    ResourceEntity::getIsPublic, ResourceEntity::setIsPublic, ResourceEntity.Fields.isPublic, changesHelper);
            if (update.getFile() != null || update.getExternalResourceUri() != null)
                replaceResourceFile(entity, update, changesHelper);
            changes.add(entity, changesHelper);
        }
        updateSafe(changes);
        return allEntities;
    }

    protected void replaceResourceFile(ResourceEntity entity, ResourceUpdate update, ChangesHelper changesHelper) throws ServiceException {
        StorageEntity storage = entity.getStorage();
        Storager storager = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
        AddedFileKey addedFileKey;
        String newFileName;
        if (update.getFile() != null) {
            addedFileKey = storager.addFile(entity.getId(), update.getFile().content(), storage.getStoragerParams());
            newFileName = update.getFile().originalFileName();
        } else {
            addedFileKey = storager.addExternalUrlFile(entity.getId(), update.getExternalResourceUri(), storage.getStoragerParams());
            newFileName = update.getExternalResourceUri();
        }
        storager.tryDeleteFile(entity.getStorageFileKey(), storage.getStoragerParams());
        if (changesHelper.isChanged(ResourceEntity.Fields.storageFileKey, entity.getStorageFileKey(), addedFileKey.fileKey()))
            entity.setStorageFileKey(addedFileKey.fileKey());
        if (changesHelper.isChanged(ResourceEntity.Fields.sizeInBytes, entity.getSizeInBytes(), addedFileKey.fileSize()))
            entity.setSizeInBytes(addedFileKey.fileSize());
        // name follows the new file unless explicitly set on the update item
        if (update.getOriginalFileName() == null && StringUtils.isNotEmpty(newFileName) && changesHelper.isChanged(ResourceEntity.Fields.originalFileName, entity.getOriginalFileName(), newFileName))
            entity.setOriginalFileName(newFileName);
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
        UUID newResourceId = UuidUtils.generate();

        var resource = findEntitySafe(resourceId);
        if (resource.getStorageId().equals(newStorageId))
            return resource;
        loadStorage(resource);
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
                .setStorageFileKey(addedFileKey.fileKey())
                .setIsPublic(resource.getIsPublic());
        validateEntity(newResource, EntitySmartService.EntityValidateMode.beforeSave);
        newResource = resourceRepository.save(newResource);
        oldStorager.tryDeleteFile(resource.getStorageFileKey(), oldStorage.getStoragerParams());
        resourceRepository.delete(resource);
        return newResource;
    }

    public String getResourceUri(ResourceEntity resourceEntity) throws ServiceException {
        if (resourceEntity != null) {
            loadStorage(resourceEntity);
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

    public void loadIconResources(TwinClassEntity twinClass) throws ServiceException {
        loadIconResources(Collections.singletonList(twinClass));
    }

    public void loadIconResources(Collection<TwinClassEntity> twinClasses) throws ServiceException {
        load(twinClasses,
                TwinClassEntity::getIconLightResourceId,
                TwinClassEntity::getIconLightResource,
                TwinClassEntity::setIconLightResource);

        load(twinClasses,
                TwinClassEntity::getIconDarkResourceId,
                TwinClassEntity::getIconDarkResource,
                TwinClassEntity::setIconDarkResource);
    }

    public void loadUploadedByUser(ResourceEntity resource) throws ServiceException {
        loadUploadedByUser(Collections.singletonList(resource));
    }

    public void loadUploadedByUser(Collection<ResourceEntity> resources) throws ServiceException {
        userService.load(resources,
                ResourceEntity::getUploadedByUserId,
                ResourceEntity::getUploadedByUser,
                ResourceEntity::setUploadedByUser);
    }

    public void loadStorage(ResourceEntity resource) throws ServiceException {
        loadStorage(Collections.singletonList(resource));
    }

    public void loadStorage(Collection<ResourceEntity> resources) throws ServiceException {
        storageService.load(resources,
                ResourceEntity::getStorageId,
                ResourceEntity::getStorage,
                ResourceEntity::setStorage);
    }

}
