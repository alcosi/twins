package org.twins.core.service.resource;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ResourceService extends EntitySecureFindServiceImpl<ResourceEntity> {

    protected final FeaturerService featurerService;
    protected final ResourceRepository resourceRepository;
    protected final StorageService storageService;
    protected final AuthService authService;
    @Lazy
    protected final UserService userService;

    /**
     * Finds a resource for the public download api. Public download must work for anonymous and
     * cross-domain callers, so no read-permission check here: the resource.public flag is the only
     * guard. Non-public resources are reported as not found to avoid leaking their existence.
     *
     * @param resourceId the unique identifier of the resource to retrieve
     * @return the public resource entity
     * @throws ServiceException if the resource is not found or is not public
     */
    @Transactional(readOnly = true)
    public ResourceEntity findPublicResource(UUID resourceId) throws ServiceException {
        var resource = findEntityPublic(resourceId);
        if (!resource.getIsPublic())
            throw new ServiceException(ErrorCodeTwins.RESOURCE_NOT_PUBLIC, "resource " + resourceId + " is not public");
        return resource;
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
        try {
            return resourceRepository.save(createResource(storage, originalFileName, resourceId, addedFileKey, isPublic));
        } catch (Throwable t) {
            // the tx will roll back; delete the already stored file to not leave an orphan in the storage
            tryDeleteFileSilently(storager, addedFileKey.fileKey(), storage.getStoragerParams());
            throw t;
        }
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
        try {
            return resourceRepository.save(createResource(storage, originalFileName, resourceId, addedFileKey, isPublic));
        } catch (Throwable t) {
            // the tx will roll back; delete the already stored file to not leave an orphan in the storage
            tryDeleteFileSilently(fileService, addedFileKey.fileKey(), storage.getStoragerParams());
            throw t;
        }
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
            throw new ServiceException(ErrorCodeTwins.RESOURCE_CREATE_REQUEST_EMPTY, "resource create request has no files");
        ApiUser apiUser = authService.getApiUser();
        StorageEntity storage = storageService.findEntitySafe(apiUser.getDomain().getResourcesStorageId());
        Storager storager = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
        List<ResourceEntity> result = new ArrayList<>(createList.size());
        List<AddedFileKey> storedFiles = new ArrayList<>(createList.size());
        try {
            for (ResourceCreate create : createList) {
                if (create.getFile() == null && StringUtils.isEmpty(create.getExternalResourceUri()))
                    throw new ServiceException(ErrorCodeTwins.RESOURCE_FILE_OR_LINK_REQUIRED, "resource item must carry a file (multipart link) or an external uri");
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
                storedFiles.add(addedFileKey);
                result.add(createResource(storage, fileName, resourceId, addedFileKey, create.getIsPublic()));
            }
            resourceRepository.saveAll(result);
        } catch (Throwable t) {
            // the tx will roll back; delete the already stored files to not leave orphans in the storage
            for (AddedFileKey storedFile : storedFiles)
                tryDeleteFileSilently(storager, storedFile.fileKey(), storage.getStoragerParams());
            throw t;
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
        return resource;
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
        entitySmartService.deleteAndLog(resource.getId(), resourceRepository);
        // the storage file is deleted last and never fails the tx: an orphan file is cheaper
        // than a row pointing at a deleted file
        tryDeleteFileSilently(storager, resource.getStorageFileKey(), storage.getStoragerParams());
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
        Set<UUID> ids = new HashSet<>(updates.size());
        for (ResourceUpdate update : updates)
            if (update.getId() == null || !ids.add(update.getId()))
                throw new ServiceException(ErrorCodeTwins.RESOURCE_UPDATE_DUPLICATE_ID, "duplicate or empty resource id in update batch: " + update.getId());
        Kit<ResourceEntity, UUID> entitiesKit = findEntitiesSafe(ids.stream().toList());
        // bulk-load storages for entities with file replacement
        List<ResourceEntity> withFileReplace = new ArrayList<>();
        for (ResourceUpdate update : updates)
            if (update.getFile() != null || update.getExternalResourceUri() != null)
                withFileReplace.add(entitiesKit.get(update.getId()));
        loadStorage(withFileReplace);
        ChangesHelperMulti<ResourceEntity> changes = new ChangesHelperMulti<>();
        List<ResourceEntity> allEntities = new ArrayList<>(updates.size());
        List<ReplacedFile> replacedFiles = new ArrayList<>();
        for (ResourceUpdate update : updates) {
            ResourceEntity entity = entitiesKit.get(update.getId());
            allEntities.add(entity);
            ChangesHelper changesHelper = new ChangesHelper();
            updateEntityFieldByValueIfNotNull(update.getOriginalFileName(), entity,
                    ResourceEntity::getOriginalFileName, ResourceEntity::setOriginalFileName, ResourceEntity.Fields.originalFileName, changesHelper);
            updateEntityFieldByValueIfNotNull(update.getIsPublic(), entity,
                    ResourceEntity::getIsPublic, ResourceEntity::setIsPublic, ResourceEntity.Fields.isPublic, changesHelper);
            if (update.getFile() != null || update.getExternalResourceUri() != null)
                replacedFiles.add(new ReplacedFile(entity, replaceResourceFile(entity, update, changesHelper)));
            changes.add(entity, changesHelper);
        }
        updateSafe(changes);
        // old files are deleted only after the db update succeeded and never fail the tx:
        // an orphan file is cheaper than a row pointing at a deleted file
        for (ReplacedFile replacedFile : replacedFiles)
            tryDeleteFileSilently(
                    featurerService.getFeaturer(replacedFile.entity().getStorage().getStorageFeaturerId(), Storager.class),
                    replacedFile.oldFileKey(),
                    replacedFile.entity().getStorage().getStoragerParams());
        return allEntities;
    }

    /**
     * Stores the replacement file and updates the entity fields. Returns the previous storage file key —
     * the caller deletes it only after the db update succeeded.
     */
    protected String replaceResourceFile(ResourceEntity entity, ResourceUpdate update, ChangesHelper changesHelper) throws ServiceException {
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
        String oldFileKey = entity.getStorageFileKey();
        if (changesHelper.isChanged(ResourceEntity.Fields.storageFileKey, entity.getStorageFileKey(), addedFileKey.fileKey()))
            entity.setStorageFileKey(addedFileKey.fileKey());
        if (changesHelper.isChanged(ResourceEntity.Fields.sizeInBytes, entity.getSizeInBytes(), addedFileKey.fileSize()))
            entity.setSizeInBytes(addedFileKey.fileSize());
        // name follows the new file unless explicitly set on the update item
        if (update.getOriginalFileName() == null && StringUtils.isNotEmpty(newFileName) && changesHelper.isChanged(ResourceEntity.Fields.originalFileName, entity.getOriginalFileName(), newFileName))
            entity.setOriginalFileName(newFileName);
        return oldFileKey;
    }

    private record ReplacedFile(ResourceEntity entity, String oldFileKey) {}

    private void tryDeleteFileSilently(Storager storager, String fileKey, HashMap<String, String> params) {
        try {
            storager.tryDeleteFile(fileKey, params);
        } catch (Throwable t) {
            log.error("Failed to delete storage file {}: {}", fileKey, t.getMessage(), t);
        }
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
     * Resources are domain-isolated on private APIs: by-id view/update/delete go through
     * findEntitySafe/findEntitiesSafe and are denied cross-domain. The public download path
     * ({@link #findPublicResource(UUID)}) intentionally bypasses this check via findEntityPublic
     * and guards access with the resource.public flag instead.
     */
    @Override
    public boolean isEntityReadDenied(ResourceEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logShort(), readPermissionCheckMode);
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
