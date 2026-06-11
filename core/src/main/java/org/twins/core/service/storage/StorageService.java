package org.twins.core.service.storage;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dao.resource.StorageRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperAttachment;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class StorageService extends EntitySecureFindServiceImpl<StorageEntity> {
    protected final StorageRepository repository;
    protected final AuthService authService;

    @Lazy
    protected final FeaturerService featurerService;

    @Lazy
    protected final TwinClassFieldService twinClassFieldService;

    @Lazy
    private final AttachmentService attachmentService;

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
        return checkDomainAccessDenied(entity.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(StorageEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void detectStorage(Collection<TwinAttachmentEntity> collection) throws ServiceException {
        attachmentService.loadTwinClassField(collection);
        var domainStorageId = authService.getApiUser().getDomain().getAttachmentsStorageId();
        for (TwinAttachmentEntity attachmentEntity : collection) {
            if (attachmentEntity.getStorageId() == null) {
                if (attachmentEntity.getTwinClassFieldId() == null) {
                    attachmentEntity.setStorageId(domainStorageId);
                } else {
                    TwinClassFieldEntity twinClassField = attachmentEntity.getTwinClassField();
                    FieldTyper fieldTyper = featurerService.getFeaturer(twinClassField.getFieldTyperFeaturerId(), FieldTyper.class);
                    if (!(fieldTyper instanceof FieldTyperAttachment<?> fieldTyperAttachment))
                        throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, twinClassField.logNormal() + " is not attachment");
                    attachmentEntity.setStorageId(fieldTyperAttachment.getStorageId(twinClassField.getFieldTyperParams()));
                }
            }
        }
        attachmentService.loadStorage(collection);
    }
}
