package org.twins.core.service.resource;

import lombok.RequiredArgsConstructor;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.resource.ResourceRepository;
import org.twins.core.dao.resource.ResourceStorageEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.file.DomainFile;
import org.twins.core.featurer.resource.AddedFileKey;
import org.twins.core.featurer.resource.StorageFileService;
import org.twins.core.featurer.resource.external.ExternalUriStorageFileService;
import org.twins.core.service.auth.AuthService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

import static org.twins.core.featurer.resource.StorageFileService.*;

@Service
@RequiredArgsConstructor
public class ResourceService extends EntitySecureFindServiceImpl<ResourceEntity> {
    protected final FeaturerService featurerService;
    protected final ResourceRepository repository;
    protected final ResourceStorageService storageService;
    protected final AuthService authService;

    @Transactional(readOnly = true)
    public DomainFile getResourceFile(UUID resourceId) throws ServiceException {
        var resource = findEntitySafe(resourceId);
        if (resource == null) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "Resource "+resourceId+" not found!");
        }
        ResourceStorageEntity storage = resource.getStorage();
        StorageFileService fileService = featurerService.getFeaturer(storage.getStorageFeaturer(), StorageFileService.class);
        var stream = fileService.getFileAsStream(resource.getStorageFileKey(), storage.getStorageParams(), createContext(null, null, null));
        return new DomainFile(stream, resource.getOriginalFileName(),resource.getSizeInBytes());
    }

    @Transactional(readOnly = false)
    public ResourceEntity addResource(String originalFileName, byte[] bytes, UUID storagerId, UUID domainId) throws ServiceException {
        return addResource(originalFileName, new ByteArrayInputStream(bytes), storagerId, domainId);
    }

    @Transactional(readOnly = false)
    public ResourceEntity addResource(String originalFileName, InputStream inputStream, UUID storagerId, UUID domainId) throws ServiceException {
        UUID resourceId = UUID.randomUUID();
        ApiUser apiUser = null;
        UUID userId = null;
        UUID businessAccountId = null;
        try {
            apiUser = authService.getApiUser();
            userId = apiUser == null ? null : apiUser.isUserSpecified() ? apiUser.getUserId() : null;
            businessAccountId = apiUser == null ? null : apiUser.isBusinessAccountSpecified() ? apiUser.getBusinessAccountId() : null;
        } catch (Throwable t) {
        }
        ResourceStorageEntity storage = storageService.findEntitySafe(storagerId);

        HashMap<String, Object> context = createContext(domainId, businessAccountId, null);
        StorageFileService fileService = featurerService.getFeaturer(storage.getStorageFeaturer(), StorageFileService.class);
        if (fileService instanceof ExternalUriStorageFileService) {
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "External URI service is configured to store file bytes!");
        }
        AddedFileKey addedFileKey = fileService.addFile(resourceId, inputStream, storage.getStorageParams(), context);
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
        return saved;
    }

    @Transactional(readOnly = false)
    public void deleteResource(UUID resourceId) throws ServiceException {
        var resource = findEntitySafe(resourceId);
        ResourceStorageEntity storage = resource.getStorage();
        StorageFileService fileService = featurerService.getFeaturer(storage.getStorageFeaturer(), StorageFileService.class);
        fileService.tryDeleteFile(resource.getStorageFileKey(), storage.getStorageParams(), createContext(null, null, null));
        repository.delete(resource);

    }

    @Transactional(readOnly = false)
    public ResourceEntity transferResource(UUID resourceId, UUID newStorageId) throws ServiceException {
        UUID newResourceId = UUID.randomUUID();

        var resource = findEntitySafe(resourceId);
        ResourceStorageEntity oldStorage = resource.getStorage();
        ResourceStorageEntity newStorage = storageService.findEntitySafe(newStorageId);

        StorageFileService oldFileService = featurerService.getFeaturer(oldStorage.getStorageFeaturer(), StorageFileService.class);
        StorageFileService newFileService = featurerService.getFeaturer(newStorage.getStorageFeaturer(), StorageFileService.class);
        InputStream fileStream=oldFileService.getFileAsStream(resource.getStorageFileKey(), oldStorage.getStorageParams(), createContext(null, null, null));
        AddedFileKey addedFileKey = newFileService.addFile(newResourceId, fileStream, newStorage.getStorageParams(), createContext(resource.getDomainId(), null, newFileService.getFileControllerUri(newStorage.getStorageParams(),createContext(resource.getDomainId(), null,null))));
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
        oldFileService.tryDeleteFile(resource.getStorageFileKey(), oldStorage.getStorageParams(), createContext(null, null, null));
        repository.delete(resource);
        return saved;
    }

    @NotNull
    private static HashMap<String, Object> createContext(UUID domainId, UUID businessAccountId, String externalUrl) {
        HashMap<String, Object> context = new HashMap<>();
        context.put(CONTEXT_ATTRIBUTE_BUSINESS_ACCOUNT, businessAccountId);
        context.put(CONTEXT_ATTRIBUTE_BUSINESS_DOMAIN, domainId);
        context.put(CONTEXT_ATTRIBUTE_FILE_URI, externalUrl);

        return context;
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
        return !isEntityReadDenied(entity, EntitySmartService.ReadPermissionCheckMode.none);
    }

}
