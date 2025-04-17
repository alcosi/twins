package org.twins.core.service.attachment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.attachment.AttachmentQuotas;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.cambium.common.util.InformationVolumeUtils.convertToGb;
import static org.twins.core.dao.attachment.AttachmentFileCreateUpdateProblem.*;
import static org.twins.core.dao.attachment.AttachmentGlobalCreateDeleteProblem.MAX_COUNT_EXCEEDED;
import static org.twins.core.dao.attachment.AttachmentGlobalCreateDeleteProblem.MIN_COUNT_NOT_REACHED;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentRestrictionService extends EntitySecureFindServiceImpl<TwinAttachmentRestrictionEntity> {
    private final TwinAttachmentRestrictionRepository restrictionRepository;
    private final DomainService domainService;
    private final TwinService twinService;
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
        validateTierQuotas(twinId, tierQuotas, cud, result);

        TwinEntity twin = twinService.findEntitySafe(twinId);
        attachmentService.loadAttachmentsCount(twin);

        TwinClassEntity twinClass = twinClassService.findEntitySafe(twin.getTwinClassId());

        EntityCUD<TwinAttachmentEntity> generalCud = new EntityCUD<>();
        EntityCUD<TwinAttachmentEntity> commentCud = new EntityCUD<>();
        EntityCUD<TwinAttachmentEntity> fieldCud = new EntityCUD<>();

        splitAttachmentsByType(cud, generalCud, commentCud, fieldCud);

        if (!generalCud.isEmpty()) {
            validateGeneralAttachments(twin, twinClass, generalCud, result);
        }
        if (!commentCud.isEmpty()) {
            validateCommentAttachments(twin, twinClass, commentCud, result);
        }
        if (!fieldCud.isEmpty()) {
            validateFieldAttachments(twin, fieldCud, result);
        }

        return result;
    }

    private void validateTierQuotas(UUID twinId, AttachmentQuotas tierQuotas, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) throws ServiceException {
        List<TwinAttachmentEntity> deletes = Optional.ofNullable(cud.getDeleteList()).orElse(Collections.emptyList());
        List<TwinAttachmentEntity> updates = Optional.ofNullable(cud.getUpdateList()).orElse(Collections.emptyList());
        List<TwinAttachmentEntity> creates = Optional.ofNullable(cud.getCreateList()).orElse(Collections.emptyList());

        long size = tierQuotas.getUsedSize();
        long count = tierQuotas.getUsedCount();

        for (TwinAttachmentEntity delete : deletes) {
            if (!delete.getTwinId().equals(twinId))
                throw new ServiceException(ErrorCodeTwins.ATTACHMENTS_NOT_VALID, "Deletable attachment [" + delete.getId() + "] is not added to twin[" + twinId + "]");
            size -= delete.getSize();
            count--;
            result.getAttachmentsForUD().add(delete);
        }
        List<UUID> updateIds = updates.stream().map(TwinAttachmentEntity::getId).collect(Collectors.toList());
        Kit<TwinAttachmentEntity, UUID> existingEntities = attachmentService.findEntitiesSafe(updateIds);

        for (TwinAttachmentEntity update : updates) {
            TwinAttachmentEntity existingEntity = existingEntities.get(update.getId());
            if (null == existingEntity)
                throw new ServiceException(ErrorCodeTwins.ATTACHMENTS_NOT_VALID, "Updatable attachment [" + update.getId() + "] is not exists");
            if (!existingEntity.getTwinId().equals(twinId))
                throw new ServiceException(ErrorCodeTwins.ATTACHMENTS_NOT_VALID, "Deletable attachment [" + update.getId() + "] is not added to twin[" + twinId + "]");
            updates.add(existingEntity);
            result.getAttachmentsForUD().add(existingEntity);
            size = size - existingEntity.getSize() + update.getSize();
        }
        for (TwinAttachmentEntity create : creates) {
            size += create.getSize();
            count++;
        }
        if (tierQuotas.getQuotaSize() > 0 && size > tierQuotas.getQuotaSize())
            throw new ServiceException(ErrorCodeTwins.TIER_SIZE_QUOTA_REACHED)
                    .addContext("size", convertToGb(size))
                    .addContext("quotaSize", convertToGb(tierQuotas.getQuotaSize()));
        if (tierQuotas.getQuotaCount() > 0 && count > tierQuotas.getQuotaCount())
            throw new ServiceException(ErrorCodeTwins.TIER_COUNT_QUOTA_REACHED);
    }

    private void splitAttachmentsByType(EntityCUD<TwinAttachmentEntity> source, EntityCUD<TwinAttachmentEntity> generalCud, EntityCUD<TwinAttachmentEntity> commentCud, EntityCUD<TwinAttachmentEntity> fieldCud) {

        if (source.getCreateList() != null) {
            for (TwinAttachmentEntity attachment : source.getCreateList()) {
                if (attachment.getTwinCommentId() != null) {
                    commentCud.getCreateList().add(attachment);
                } else if (attachment.getTwinClassFieldId() != null) {
                    fieldCud.getCreateList().add(attachment);
                } else {
                    generalCud.getCreateList().add(attachment);
                }
            }
        }

        if (source.getUpdateList() != null) {
            for (TwinAttachmentEntity attachment : source.getUpdateList()) {
                if (attachment.getTwinCommentId() != null) {
                    commentCud.getUpdateList().add(attachment);
                } else if (attachment.getTwinClassFieldId() != null) {
                    fieldCud.getUpdateList().add(attachment);
                } else {
                    generalCud.getUpdateList().add(attachment);
                }
            }
        }

        if (source.getDeleteList() != null) {
            for (TwinAttachmentEntity attachment : source.getDeleteList()) {
                if (attachment.getTwinCommentId() != null) {
                    commentCud.getDeleteList().add(attachment);
                } else if (attachment.getTwinClassFieldId() != null) {
                    fieldCud.getDeleteList().add(attachment);
                } else {
                    generalCud.getDeleteList().add(attachment);
                }
            }
        }
    }

    private void validateGeneralAttachments(TwinEntity twin, TwinClassEntity twinClass, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) throws ServiceException {
        TwinAttachmentRestrictionEntity generalRestriction = findEntitySafe(twinClass.getGeneralAttachmentRestrictionId());
        int currentCount = twin.getTwinAttachmentsCount().getDirect();

        validateAttachmentsCount(currentCount, generalRestriction, cud, result);
        validateAttachmentsSize(generalRestriction, cud, result);
        validateAttachmentsNameRegexp(generalRestriction, cud, result);
        validateAttachmentsExtensions(generalRestriction, cud, result);
    }

    private void validateCommentAttachments(TwinEntity twin, TwinClassEntity twinClass, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) throws ServiceException {
        TwinAttachmentRestrictionEntity commentRestriction = findEntitySafe(twinClass.getCommentAttachmentRestrictionId());
        int currentCount = twin.getTwinAttachmentsCount().getFromComments();

        validateAttachmentsCount(currentCount, commentRestriction, cud, result);
        validateAttachmentsSize(commentRestriction, cud, result);
        validateAttachmentsNameRegexp(commentRestriction, cud, result);
        validateAttachmentsExtensions(commentRestriction, cud, result);
    }

    private void validateFieldAttachments(TwinEntity twin, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) throws ServiceException {
        Map<UUID, EntityCUD<TwinAttachmentEntity>> cudByField = splitCudByFieldId(cud);

        for (Map.Entry<UUID, EntityCUD<TwinAttachmentEntity>> entry : cudByField.entrySet()) {
            validateFieldAttachmentsByField(twin, entry.getKey(), entry.getValue(), result);
        }
    }


    private Map<UUID, EntityCUD<TwinAttachmentEntity>> splitCudByFieldId(EntityCUD<TwinAttachmentEntity> cud) {
        Map<UUID, EntityCUD<TwinAttachmentEntity>> result = new HashMap<>();

        if (cud.getCreateList() != null) {
            for (TwinAttachmentEntity attachment : cud.getCreateList()) {
                if (attachment.getTwinClassFieldId() != null) {
                    result.computeIfAbsent(attachment.getTwinClassFieldId(), k -> new EntityCUD<>())
                            .getCreateList().add(attachment);
                }
            }
        }

        if (cud.getUpdateList() != null) {
            for (TwinAttachmentEntity attachment : cud.getUpdateList()) {
                if (attachment.getTwinClassFieldId() != null) {
                    result.computeIfAbsent(attachment.getTwinClassFieldId(), k -> new EntityCUD<>())
                            .getUpdateList().add(attachment);
                }
            }
        }

        if (cud.getDeleteList() != null) {
            for (TwinAttachmentEntity attachment : cud.getDeleteList()) {
                if (attachment.getTwinClassFieldId() != null) {
                    result.computeIfAbsent(attachment.getTwinClassFieldId(), k -> new EntityCUD<>())
                            .getDeleteList().add(attachment);
                }
            }
        }

        return result;
    }

    private void validateFieldAttachmentsByField(TwinEntity twin, UUID twinClassFieldId, EntityCUD<TwinAttachmentEntity> fieldCud, AttachmentCUDValidateResult result) throws ServiceException {
        TwinClassFieldEntity field = twinClassFieldService.findEntitySafe(twinClassFieldId);

        TwinAttachmentRestrictionEntity restriction = findEntitySafe(UUID.fromString(field.getFieldTyperParams().get("restrictionId")));

        int currentCount = attachmentService.countByTwinAndField(twin.getId(), twinClassFieldId);

        validateAttachmentsCount(currentCount, restriction, fieldCud, result);
        validateAttachmentsSize(restriction, fieldCud, result);
        validateAttachmentsNameRegexp(restriction, fieldCud, result);
        validateAttachmentsExtensions(restriction, fieldCud, result);
    }

    private void validateAttachmentsCount(int currentCount, TwinAttachmentRestrictionEntity restriction, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) {
        int toCreate = Optional.ofNullable(cud.getCreateList()).map(List::size).orElse(0);
        int toDelete = Optional.ofNullable(cud.getDeleteList()).map(List::size).orElse(0);

        currentCount = currentCount + toCreate - toDelete;

        if (restriction.getMinCount() > currentCount) {
            result.getCudProblems().getGlobalProblems().add(new AttachmentGlobalProblem().setProblem(MIN_COUNT_NOT_REACHED));
        }

        if (restriction.getMaxCount() < currentCount) {
            result.getCudProblems().getGlobalProblems().add(new AttachmentGlobalProblem().setProblem(MAX_COUNT_EXCEEDED));
        }
    }

    private void validateAttachmentsSize(TwinAttachmentRestrictionEntity restriction, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) {
        if (cud.getCreateList() != null) {
            cud.getCreateList().forEach(attachment -> {
                if (attachment.getSize() > restriction.getFileSizeMbLimit()) {
                    result.getCudProblems().getCreateProblems().add(new AttachmentCreateProblem().setId(attachment.getId().toString()).setProblem(INVALID_SIZE));
                }
            });
        }

        if (cud.getUpdateList() != null) {
            cud.getUpdateList().forEach(attachment -> {
                if (attachment.getSize() > restriction.getFileSizeMbLimit()) {
                    result.getCudProblems().getUpdateProblems().add(new AttachmentUpdateProblem().setId(attachment.getId().toString()).setProblem(INVALID_SIZE));
                }
            });
        }
    }

    private void validateAttachmentsNameRegexp(TwinAttachmentRestrictionEntity restriction, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) {
        if (cud.getCreateList() != null) {
            cud.getCreateList().forEach(attachment -> {
                String fileName = attachment.getTitle();
                if (fileName != null && !fileName.matches(restriction.getFileNameRegexp())) {
                    result.getCudProblems().getCreateProblems().add(new AttachmentCreateProblem().setId(attachment.getId().toString()).setProblem(INVALID_NAME));
                }
            });
        }

        if (cud.getUpdateList() != null) {
            cud.getUpdateList().forEach(attachment -> {
                String fileName = attachment.getTitle();
                if (fileName != null && !fileName.matches(restriction.getFileNameRegexp())) {
                    result.getCudProblems().getUpdateProblems().add(new AttachmentUpdateProblem().setId(attachment.getId().toString()).setProblem(INVALID_NAME));
                }
            });
        }
    }

    private void validateAttachmentsExtensions(TwinAttachmentRestrictionEntity restriction, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) {
        List<String> allowedExtensions = Arrays.asList(
                restriction.getFileExtensionLimit().toLowerCase().split(",")
        );

        if (cud.getCreateList() != null) {
            cud.getCreateList().forEach(attachment -> {
                String ext = getFileExtension(attachment.getTitle()).toLowerCase();
                if (!allowedExtensions.contains(ext)) {
                    result.getCudProblems().getCreateProblems().add(
                            new AttachmentCreateProblem().setId(attachment.getId().toString()).setProblem(INVALID_TYPE));
                }
            });
        }

        if (cud.getUpdateList() != null) {
            cud.getUpdateList().forEach(attachment -> {
                String ext = getFileExtension(attachment.getTitle()).toLowerCase();
                if (!allowedExtensions.contains(ext)) {
                    result.getCudProblems().getUpdateProblems().add(
                            new AttachmentUpdateProblem().setId(attachment.getId().toString()).setProblem(INVALID_TYPE));
                }
            });
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex + 1);
    }

}
