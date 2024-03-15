package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;

import java.util.UUID;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinAttachmentService extends EntitySecureFindServiceImpl<TwinAttachmentEntity> {
    public boolean checkOnDirect(TwinAttachmentEntity twinAttachmentEntity) {
        return twinAttachmentEntity.getTwinflowTransitionId() != null
                || twinAttachmentEntity.getTwinCommentId() != null
                || twinAttachmentEntity.getTwinClassFieldId() != null;
    }

    @Override
    public CrudRepository<TwinAttachmentEntity, UUID> entityRepository() {
        return null;
    }

    @Override
    public boolean isEntityReadDenied(TwinAttachmentEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinAttachmentEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return false;
    }
}
