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

import java.util.*;
import java.util.stream.Collectors;

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

    public void loadAttachmentsCount(Collection<TwinEntity> twins, boolean total) {
        if (total) attachmentsCountTotal(twins);
        else attachmentsSeparateCount(twins);
    }

    private void attachmentsCountTotal(Collection<TwinEntity> twins) {
        List<UUID> twinIds = twins.stream()
                .map(TwinEntity::getId)
                .collect(Collectors.toList());

        List<Object[]> results = twinAttachmentRepository.countByTwinIds(twinIds);

        Map<UUID, Long> countMap = results.stream()
                .collect(Collectors.toMap(result -> (UUID) result[0], result -> (Long) result[1]));

        for (TwinEntity twin : twins) {
            Long count = countMap.get(twin.getId());
            if (count == null) count = 0L;
            twin.setAttachmentsCount(new AttachmentsCount().setAll(count.intValue()));
        }
    }

    private void attachmentsSeparateCount(Collection<TwinEntity> twins) {
        List<UUID> collect = twins.stream()
                .map(TwinEntity::getId)
                .collect(Collectors.toList());
        List<Object[]> objects = twinAttachmentRepository.countAttachmentsByTwinIds(collect);
        Map<UUID, Object[]> resultMap = objects.stream()
                .collect(Collectors.toMap(result -> (UUID) result[0], result -> result));

        for (TwinEntity twin : twins) {
            UUID twinId = twin.getId();

            Object[] innerArray = resultMap.get(twinId);

            if (innerArray != null) {
                int directCount = ((Long) innerArray[1]).intValue();
                int commentCount = ((Long) innerArray[2]).intValue();
                int transitionCount = ((Long) innerArray[3]).intValue();
                int fieldCount = ((Long) innerArray[4]).intValue();

                twin.setAttachmentsCount(new AttachmentsCount()
                        .setDirect(directCount)
                        .setFromComments(commentCount)
                        .setFromTransitions(transitionCount)
                        .setFromFields(fieldCount))
                ;
            }
        }
    }
}
