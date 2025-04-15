package org.twins.core.service.attachment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.AttachmentCUDValidateResult;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.attachment.AttachmentQuotas;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentRestrictionService extends EntitySecureFindServiceImpl<TwinAttachmentRestrictionEntity> {
    private final TwinAttachmentRestrictionRepository restrictionRepository;
    private final DomainService domainService;
    private final TwinClassService twinClassService;
    private final TwinClassFieldService twinClassFieldService;
    private final AttachmentService attachmentService;

    @Override
    public CrudRepository<TwinAttachmentRestrictionEntity, UUID> entityRepository() {
        return restrictionRepository;
    }

    @Override
    public Function<TwinAttachmentRestrictionEntity, UUID> entityGetIdFunction() {
        return TwinAttachmentRestrictionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinAttachmentRestrictionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinAttachmentRestrictionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public AttachmentCUDValidateResult validateAttachments(UUID twinId, EntityCUD<TwinAttachmentEntity> cud) throws ServiceException {
        AttachmentCUDValidateResult result = new AttachmentCUDValidateResult();

        AttachmentQuotas tierQuotas = domainService.getTierQuotas();

        TwinClassEntity twinClass = twinClassService.findEntitySafe(twinId);
        TwinAttachmentRestrictionEntity generalRestriction = findEntitySafe(twinClass.getGeneralAttachmentRestrictionId());
        TwinAttachmentRestrictionEntity commentRestriction = findEntitySafe(twinClass.getCommentAttachmentRestrictionId());

        Map<AttachmentType, List<TwinAttachmentEntity>> attachmentsByType = groupAttachmentsByType(cud, twinId);

        validateTierQuotas(tierQuotas, cud);

        validateGeneralAttachments(attachmentsByType.get(AttachmentType.GENERAL), generalRestriction, result);
        validateCommentAttachments(attachmentsByType.get(AttachmentType.COMMENT), commentRestriction, result);
        validateFieldAttachments(attachmentsByType.get(AttachmentType.FIELD), result);

        return result;
    }

    private Map<AttachmentType, List<TwinAttachmentEntity>> groupAttachmentsByType(EntityCUD<TwinAttachmentEntity> cud, UUID twinId) throws ServiceException {
        Map<AttachmentType, List<TwinAttachmentEntity>> result = new EnumMap<>(AttachmentType.class);
        for (AttachmentType type : AttachmentType.values()) {
            result.put(type, new ArrayList<>());
        }

        processAttachments(cud.getCreateList(), twinId, result);
        processAttachments(cud.getUpdateList(), twinId, result);

        return result;
    }

    private void processAttachments(List<TwinAttachmentEntity> attachments, UUID twinId,
                                    Map<AttachmentType, List<TwinAttachmentEntity>> result) throws ServiceException {
        if (attachments == null) return;

        for (TwinAttachmentEntity attachment : attachments) {
            validateAttachmentOwnership(twinId, attachment);

            if (attachment.getCommentId() != null) {
                result.get(AttachmentType.COMMENT).add(attachment);
            } else if (attachment.getTwinClassFieldId() != null) {
                result.get(AttachmentType.FIELD).add(attachment);
            } else {
                result.get(AttachmentType.GENERAL).add(attachment);
            }
        }
    }

    private void validateGeneralAttachments(List<TwinAttachmentEntity> attachments,
                                            TwinAttachmentRestrictionEntity restriction,
                                            AttachmentCUDValidateResult result) {
        if (attachments == null || restriction == null) return;

        for (TwinAttachmentEntity attachment : attachments) {
            validateAttachmentAgainstRestriction(attachment, restriction, result, false);
        }
    }

    private void validateCommentAttachments(List<TwinAttachmentEntity> attachments,
                                            TwinAttachmentRestrictionEntity restriction,
                                            AttachmentCUDValidateResult result) {
        if (attachments == null || restriction == null) return;

        for (TwinAttachmentEntity attachment : attachments) {
            validateAttachmentAgainstRestriction(attachment, restriction, result, false);
        }
    }

    private void validateFieldAttachments(List<TwinAttachmentEntity> attachments,
                                          AttachmentCUDValidateResult result) throws ServiceException {
        if (attachments == null) return;

        for (TwinAttachmentEntity attachment : attachments) {
            TwinClassFieldEntity field = twinClassFieldService.findEntitySafe(attachment.getTwinClassFieldId());
            UUID restrictionId = field.getParams().get("attachmentRestrictionId"); // предполагаем, что ID хранится здесь
            TwinAttachmentRestrictionEntity restriction = getRestrictionById(restrictionId);

            validateAttachmentAgainstRestriction(attachment, restriction, result, false);
        }
    }

    private void validateTierQuotas(AttachmentQuotas tierQuotas, EntityCUD<TwinAttachmentEntity> cud) throws ServiceException {
        if (tierQuotas.getQuotaSize() == 0 && tierQuotas.getQuotaCount() == 0) {
            return; // Нет ограничений
        }

        // Рассчитываем новое использование после выполнения CUD операций
        long newSize = tierQuotas.getUsedSize();
        long newCount = tierQuotas.getUsedCount();

        // Учет удалений
        for (TwinAttachmentEntity deleted : Optional.ofNullable(cud.getDeleteList()).orElse(Collections.emptyList())) {
            newSize -= deleted.getSize();
            newCount--;
        }

        // Учет обновлений
        if (cud.getUpdateList() != null && !cud.getUpdateList().isEmpty()) {
            Map<UUID, TwinAttachmentEntity> updates = cud.getUpdateList().stream()
                    .collect(Collectors.toMap(TwinAttachmentEntity::getId, Function.identity()));

            List<TwinAttachmentEntity> existing = attachmentService.findEntitiesSafe(new ArrayList<>(updates.keySet()));

            for (TwinAttachmentEntity existingAtt : existing) {
                TwinAttachmentEntity updatedAtt = updates.get(existingAtt.getId());
                newSize = newSize - existingAtt.getSize() + updatedAtt.getSize();
            }
        }

        // Учет созданий
        for (TwinAttachmentEntity created : Optional.ofNullable(cud.getCreateList()).orElse(Collections.emptyList())) {
            newSize += created.getSize();
            newCount++;
        }

        // Проверка квот
        if (tierQuotas.getQuotaSize() > 0 && newSize > tierQuotas.getQuotaSize()) {
            throw new ServiceException(ErrorCodeTwins.TIER_SIZE_QUOTA_REACHED)
                    .addContext("size", convertToGb(newSize))
                    .addContext("quotaSize", convertToGb(tierQuotas.getQuotaSize()));
        }

        if (tierQuotas.getQuotaCount() > 0 && newCount > tierQuotas.getQuotaCount()) {
            throw new ServiceException(ErrorCodeTwins.TIER_COUNT_QUOTA_REACHED)
                    .addContext("count", newCount)
                    .addContext("quotaCount", tierQuotas.getQuotaCount());
        }
    }

    private double convertToGb(long bytes) {
        return bytes / (1024.0 * 1024.0 * 1024.0);
    }

}

enum AttachmentType {
    GENERAL,   // Вложения самого twin'а
    COMMENT,   // Вложения комментариев
    FIELD      // Вложения полей
}
