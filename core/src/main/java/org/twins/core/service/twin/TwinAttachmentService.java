package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinAttachmentRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.AttachmentsCount;
import org.twins.core.mappers.rest.mappercontext.modes.AttachmentCountMode;

import java.util.Collection;
import java.util.UUID;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinAttachmentService extends EntitySecureFindServiceImpl<TwinAttachmentEntity> {

    final private TwinAttachmentRepository twinAttachmentRepository;

    public boolean checkOnDirect(TwinAttachmentEntity twinAttachmentEntity) {
        return twinAttachmentEntity.getTwinflowTransitionId() == null
                && twinAttachmentEntity.getTwinCommentId() == null
                && twinAttachmentEntity.getTwinClassFieldId() == null;
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

    public void attachmentsCount(Collection<TwinEntity> twins, AttachmentCountMode mode) {
        for (TwinEntity twin : twins) {
            if (twin.getAttachmentsCount() != null)
                return;
            twin.setAttachmentsCount(new AttachmentsCount());
            if (mode.equals(AttachmentCountMode.SHORT))
                needLoadAllAttachmentsCount(twin);
            else if (mode.equals(AttachmentCountMode.DETAILED))
                needLoadSeparateAttachmentsCount(twin);
        }
    }

    private void needLoadAllAttachmentsCount(TwinEntity twin) {
        Long count = twinAttachmentRepository.countByTwinId(twin.getId());
        twin.getAttachmentsCount().setAll(count.intValue());
    }

    private void needLoadSeparateAttachmentsCount(TwinEntity twin){
        Object[] objects = twinAttachmentRepository.countAttachmentsByTwinId(twin.getId());
        Object[] innerArray = ((Object[]) objects[0]);

        int directCount = ((Long) innerArray[0]).intValue();
        int commentCount = ((Long) innerArray[1]).intValue();
        int transitionCount = ((Long) innerArray[2]).intValue();
        int fieldCount = ((Long) innerArray[3]).intValue();

        twin.getAttachmentsCount()
                .setDirect(directCount)
                .setFromComments(commentCount)
                .setFromTransitions(transitionCount)
                .setFromFields(fieldCount);
    }
}
