package org.twins.core.service.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dao.resource.StorageRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperAttachment;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;
import java.util.function.Function;

import static org.twins.core.service.SystemEntityService.TWIN_ATTACHMENT_EXTERNAL_URI_STORAGER_ID;

@Service
@RequiredArgsConstructor
public class StorageService extends EntitySecureFindServiceImpl<StorageEntity> {
    protected final StorageRepository repository;
    protected final AuthService authService;

    @Lazy
    protected final FeaturerService featurerService;

    @Lazy
    protected final TwinClassFieldService twinClassFieldService;

    private StorageEntity externalUrlStorage;

    @Override
    public CrudRepository<StorageEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<StorageEntity, UUID> entityGetIdFunction() {
        return StorageEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(StorageEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = entity.getDomainId() != null && !entity.getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.logNormal() + " is not allowed in" + domain.logShort());
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(StorageEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadStorages(Collection<TwinAttachmentEntity> collection) throws ServiceException {
        twinClassFieldService.loadFields(collection);
        Map<UUID, Set<TwinAttachmentEntity>> needLoad = new HashMap<>();
        for (TwinAttachmentEntity attachmentEntity : collection) {
            if (attachmentEntity.getStorageId() == null) {
                if (attachmentEntity.getTwinClassFieldId() == null) {
                    attachmentEntity
                            .setStorageId(TWIN_ATTACHMENT_EXTERNAL_URI_STORAGER_ID)
                            .setStorage(getExternalUrlStorage());
                } else {
                    TwinClassFieldEntity twinClassField = attachmentEntity.getTwinClassField();
                    FieldTyper fieldTyper = featurerService.getFeaturer(twinClassField.getFieldTyperFeaturerId(), FieldTyper.class);
                    if (!(fieldTyper instanceof FieldTyperAttachment<?> fieldTyperAttachment))
                        throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, twinClassField.logNormal() + " is not attachment");
                    MapUtils.safeAdd(needLoad, fieldTyperAttachment.getStorageId(twinClassField.getFieldTyperParams()), attachmentEntity);
                }
            } else if (attachmentEntity.getStorage() == null) {
                MapUtils.safeAdd(needLoad, attachmentEntity.getStorageId(), attachmentEntity);
            }
        }
        if (needLoad.isEmpty())
            return;
        var loadedKit = findEntitiesSafe(needLoad.keySet());
        for (var entry : needLoad.entrySet()) {
            for (var attachmentEntity : entry.getValue()) {
                var storage = loadedKit.getSafe(entry.getKey());
                attachmentEntity
                        .setStorageId(storage.getId())
                        .setStorage(storage);
            }
        }
    }

    public StorageEntity getExternalUrlStorage() throws ServiceException {
        if (externalUrlStorage == null) {
            externalUrlStorage = findEntitySafe(TWIN_ATTACHMENT_EXTERNAL_URI_STORAGER_ID);
        }
        return externalUrlStorage;
    }
}
