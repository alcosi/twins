package org.twins.core.service.attachment;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.InformationVolumeUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.attachment.AttachmentQuotas;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperAttachment;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageAttachment;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.twin.TwinActionService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.cambium.common.util.InformationVolumeUtils.convertToGb;
import static org.twins.core.enums.attachment.problem.AttachmentFileCreateUpdateProblem.*;
import static org.twins.core.enums.attachment.problem.AttachmentGlobalCreateDeleteProblem.MAX_COUNT_EXCEEDED;
import static org.twins.core.enums.attachment.problem.AttachmentGlobalCreateDeleteProblem.MIN_COUNT_NOT_REACHED;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class AttachmentRestrictionService extends EntitySecureFindServiceImpl<TwinAttachmentRestrictionEntity> {
    private final TwinAttachmentRestrictionRepository restrictionRepository;
    private final DomainService domainService;
    @Lazy
    private final TwinService twinService;
    private final TwinClassService twinClassService;
    private final TwinClassFieldService twinClassFieldService;
    private final AttachmentService attachmentService;
    private final FeaturerService featurerService;
    private final AuthService authService;
    private final TwinActionService twinActionService;

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
        ApiUser apiUser = authService.getApiUser();
        if (entity.getDomainId() != null
                && !entity.getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logNormal() + " is not allowed in " + apiUser.getDomain().logNormal());
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(TwinAttachmentRestrictionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public AttachmentCUDValidateResult validateAttachments(UUID twinId, UUID twinClassId, EntityCUD<TwinAttachmentEntity> cud) throws ServiceException {
        AttachmentCUDValidateResult result = new AttachmentCUDValidateResult();

        AttachmentQuotas tierQuotas = domainService.getTierQuotas();
        validateTierQuotas(twinId, tierQuotas, cud, result);

        TwinEntity twin = null;
        TwinClassEntity twinClass;
        if (twinId != null) {
            twin = twinService.findEntitySafe(twinId);
            twinClass = twin.getTwinClass();
            attachmentService.loadAttachmentsCount(twin);
            twinActionService.checkAllowed(twin, TwinAction.ATTACHMENT_ADD);
        } else {
            twinClass = twinClassService.findEntitySafe(twinClassId);
            //todo check permission for class
        }
        EntityCUD<TwinAttachmentEntity> generalCud = new EntityCUD<>();
        EntityCUD<TwinAttachmentEntity> commentCud = new EntityCUD<>();
        Map<UUID, EntityCUD<TwinAttachmentEntity>> fieldCudMap = new HashMap<>();

        splitAttachmentsByType(cud, generalCud, commentCud, fieldCudMap);

        if (!generalCud.isEmpty()) {
            validateGeneralAttachments(twin, twinClass, generalCud, result);
        }
        if (!commentCud.isEmpty()) {
            validateCommentAttachments(twin, twinClass, commentCud, result);
        }
        if (!fieldCudMap.isEmpty()) {
            validateFieldAttachmentsBatch(twin, fieldCudMap, result);
        }
        return result;
    }

    public Map<UUID, TwinAttachmentRestrictionEntity> getRestrictionsFromFieldTyper(Set<UUID> fieldIds) throws ServiceException {
        Map<UUID, UUID> fieldToRestrictionMap = getRestrictionIdsFromFieldTyper(fieldIds);

        Kit<TwinAttachmentRestrictionEntity, UUID> restrictions = findEntitiesSafe(new HashSet<>(fieldToRestrictionMap.values()));
        Map<UUID, TwinAttachmentRestrictionEntity> ret = new HashMap<>();
        for (var fieldRestrictionEntry : fieldToRestrictionMap.entrySet()) {
            if (!restrictions.containsKey(fieldRestrictionEntry.getValue())) {
                throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "twinClassField[" + fieldRestrictionEntry.getKey() + "] has unknown restriction[" + fieldRestrictionEntry.getValue() + "]");
            }
            ret.put(fieldRestrictionEntry.getKey(), restrictions.get(fieldRestrictionEntry.getValue()));
        }

        return ret;
    }

    public void loadGeneralRestrictions(Collection<TwinClassEntity> twinClasses) throws ServiceException {
        KitGrouped<TwinClassEntity, UUID, UUID> needLoad = null;
        for (var twinClassEntity : twinClasses) {
            if (twinClassEntity.getGeneralAttachmentRestrictionId() != null && twinClassEntity.getGeneralAttachmentRestriction() == null) {
                if (needLoad == null) needLoad = new KitGrouped<>(TwinClassEntity::getId, TwinClassEntity::getGeneralAttachmentRestrictionId);
                needLoad.add(twinClassEntity);
            }
        }
        if (needLoad == null)
            return;
        Kit<TwinAttachmentRestrictionEntity, UUID> restrictions = findEntitiesSafe(needLoad.getGroupedKeySet());
        for (var twinClassEntity : needLoad) {
            twinClassEntity.setGeneralAttachmentRestriction(restrictions.get(twinClassEntity.getGeneralAttachmentRestrictionId()));
        }
    }

    private Map<UUID, UUID> getRestrictionIdsFromFieldTyper(Set<UUID> twinClassFieldIds) throws ServiceException {
        Map<UUID, UUID> result = new HashMap<>();
        Kit<TwinClassFieldEntity, UUID> twinClassFieldKit = twinClassFieldService.findEntitiesSafe(twinClassFieldIds);

        for (var fieldEntity : twinClassFieldKit.getCollection()) {
            FieldTyper<?, ?, ?, ?> fieldTyper = featurerService.getFeaturer(fieldEntity.getFieldTyperFeaturerId(), FieldTyper.class);

            if (fieldTyper.getStorageType() != TwinFieldStorageAttachment.class) {
                throw new ServiceException(ErrorCodeTwins.ATTACHMENTS_NOT_VALID, "Wrong fieldTyper for [" + fieldEntity.getId() + "]");
            }
            FieldTyperAttachment<?> fieldTyperAttachment = (FieldTyperAttachment) fieldTyper;
            UUID restrictionId = fieldTyperAttachment.getRestrictionId(fieldEntity.getFieldTyperParams());
            if (restrictionId != null) {
                result.put(fieldEntity.getId(), restrictionId);
            }
        }

        return result;
    }

    public UUID getRestrictionIdFromFieldTyper(TwinClassFieldEntity fieldEntity) throws ServiceException {
        FieldTyper<?, ?, ?, ?> fieldTyper = featurerService.getFeaturer(fieldEntity.getFieldTyperFeaturerId(), FieldTyper.class);

        if (fieldTyper.getStorageType() != TwinFieldStorageAttachment.class) {
            throw new ServiceException(ErrorCodeTwins.ATTACHMENTS_NOT_VALID, "Wrong fieldTyper for [" + fieldEntity.getId() + "]");
        }

        return ((FieldTyperAttachment) fieldTyper).getRestrictionId(fieldEntity.getFieldTyperParams());
    }

    private void validateTierQuotas(UUID twinId, AttachmentQuotas tierQuotas, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) throws ServiceException {
        List<TwinAttachmentEntity> deletes = cud.getDeleteListSafe();
        List<TwinAttachmentEntity> updates = cud.getUpdateListSafe();
        List<TwinAttachmentEntity> creates = cud.getCreateList();

        long size = tierQuotas.getUsedSize();
        long count = tierQuotas.getUsedCount();

        for (TwinAttachmentEntity delete : deletes) {
            if (!delete.getTwinId().equals(twinId))
                throw new ServiceException(ErrorCodeTwins.ATTACHMENTS_NOT_VALID, "Deletable attachment [" + delete.getId() + "] is not added to twin[" + twinId + "]");
            size -= delete.getSize();
            count--;
            result.getAttachmentsForUD().add(delete);
        }
        if (CollectionUtils.isEmpty(updates) && CollectionUtils.isEmpty(creates))
            return;

        List<UUID> updateIds = updates.stream().map(TwinAttachmentEntity::getId).collect(Collectors.toList());
        Kit<TwinAttachmentEntity, UUID> existingEntities = attachmentService.findEntitiesSafe(updateIds);

        for (TwinAttachmentEntity update : updates) {
            TwinAttachmentEntity existingEntity = existingEntities.get(update.getId());
            if (null == existingEntity)
                throw new ServiceException(ErrorCodeTwins.ATTACHMENTS_NOT_VALID, "Updatable attachment [" + update.getId() + "] is not exists");
            if (!existingEntity.getTwinId().equals(twinId))
                throw new ServiceException(ErrorCodeTwins.ATTACHMENTS_NOT_VALID, "Deletable attachment [" + update.getId() + "] is not added to twin[" + twinId + "]");
            result.getAttachmentsForUD().add(existingEntity);
            size -= existingEntity.getSize() - update.getSize();
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

    private void splitAttachmentsByType(EntityCUD<TwinAttachmentEntity> source, EntityCUD<TwinAttachmentEntity> generalCud, EntityCUD<TwinAttachmentEntity> commentCud, Map<UUID, EntityCUD<TwinAttachmentEntity>> fieldCudMap) {
        if (source.getCreateList() != null) {
            for (TwinAttachmentEntity attachment : source.getCreateList()) {
                if (attachment.getTwinCommentId() != null) {
                    commentCud.getCreateListSafe().add(attachment);
                } else if (attachment.getTwinClassFieldId() != null) {
                    EntityCUD<TwinAttachmentEntity> fieldCud = fieldCudMap.computeIfAbsent(
                            attachment.getTwinClassFieldId(),
                            k -> new EntityCUD<>()
                    );
                    fieldCud.getCreateListSafe().add(attachment);
                } else {
                    generalCud.getCreateListSafe().add(attachment);
                }
            }
        }

        if (source.getUpdateList() != null) {
            for (TwinAttachmentEntity attachment : source.getUpdateList()) {
                if (attachment.getTwinCommentId() != null) {
                    commentCud.getUpdateListSafe().add(attachment);
                } else if (attachment.getTwinClassFieldId() != null) {
                    EntityCUD<TwinAttachmentEntity> fieldCud = fieldCudMap.computeIfAbsent(
                            attachment.getTwinClassFieldId(),
                            k -> new EntityCUD<>()
                    );
                    fieldCud.getUpdateListSafe().add(attachment);
                } else {
                    generalCud.getUpdateListSafe().add(attachment);
                }
            }
        }

        if (source.getDeleteList() != null) {
            for (TwinAttachmentEntity attachment : source.getDeleteList()) {
                if (attachment.getTwinCommentId() != null) {
                    commentCud.getDeleteListSafe().add(attachment);
                } else if (attachment.getTwinClassFieldId() != null) {
                    EntityCUD<TwinAttachmentEntity> fieldCud = fieldCudMap.computeIfAbsent(
                            attachment.getTwinClassFieldId(),
                            k -> new EntityCUD<>()
                    );
                    fieldCud.getDeleteListSafe().add(attachment);
                } else {
                    generalCud.getDeleteListSafe().add(attachment);
                }
            }
        }
    }

    private void validateGeneralAttachments(TwinEntity twin, TwinClassEntity twinClass, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) throws ServiceException {
        if (twinClass.getGeneralAttachmentRestrictionId() == null)
            return;

        TwinAttachmentRestrictionEntity generalRestriction = findEntitySafe(twinClass.getGeneralAttachmentRestrictionId());
        int currentCount = twin == null ? 0 : twin.getTwinAttachmentsCount().getDirect();
        validateAttachmentRestrictions(currentCount, generalRestriction, cud, result);
    }

    private void validateCommentAttachments(TwinEntity twin, TwinClassEntity twinClass, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) throws ServiceException {
        if (twinClass.getCommentAttachmentRestrictionId() == null)
            return;

        TwinAttachmentRestrictionEntity commentRestriction = findEntitySafe(twinClass.getCommentAttachmentRestrictionId());
        int currentCount = twin == null ? 0 : twin.getTwinAttachmentsCount().getFromComments();
        validateAttachmentRestrictions(currentCount, commentRestriction, cud, result);
    }

    private void validateFieldAttachmentsBatch(TwinEntity twin, Map<UUID, EntityCUD<TwinAttachmentEntity>> fieldCudMap, AttachmentCUDValidateResult result) throws ServiceException {
        Set<UUID> fieldIds = fieldCudMap.keySet();
        if (fieldIds.isEmpty())
            return;

        Kit<TwinClassFieldEntity, UUID> fieldsKit = twinClassFieldService.findEntitiesSafe(fieldIds);
        Map<UUID, Integer> currentCounts = attachmentService.countByTwinAndFields(twin, fieldIds);

        Map<UUID, UUID> fieldToRestrictionMap = new HashMap<>();
        Set<UUID> restrictionIds = new HashSet<>();

        for (TwinClassFieldEntity field : fieldsKit.getCollection()) {
            UUID restrictionId = getRestrictionIdFromFieldTyper(field);
            if (restrictionId == null) {
                continue;
            }
            fieldToRestrictionMap.put(field.getId(), restrictionId);
            restrictionIds.add(restrictionId);
        }

        if (restrictionIds.isEmpty())
            return;
        Kit<TwinAttachmentRestrictionEntity, UUID> restrictionsKit = findEntitiesSafe(restrictionIds);
        for (var entry : fieldCudMap.entrySet()) {
            UUID fieldId = entry.getKey();
            UUID restrictionId = fieldToRestrictionMap.get(fieldId);
            if (restrictionId == null) {
                continue;
            }
            TwinAttachmentRestrictionEntity restriction = restrictionsKit.get(restrictionId);
            if (restriction == null) {
                throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "incorrect restriction id[" + restrictionId + "]");
            }
            int currentCount = currentCounts.getOrDefault(fieldId, 0);
            validateAttachmentRestrictions(currentCount, restriction, entry.getValue(), result);
        }
    }

    private void validateAttachmentRestrictions(int currentCount, TwinAttachmentRestrictionEntity restriction, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) {
        validateAttachmentsCount(currentCount, restriction, cud, result);
        validateAttachmentsSize(restriction, cud, result);
        validateAttachmentsNameRegexp(restriction, cud, result);
        validateAttachmentsExtensions(restriction, cud, result);
    }

    private void validateAttachmentsCount(int currentCount, TwinAttachmentRestrictionEntity restriction, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) {
        int toCreate = Optional.ofNullable(cud.getCreateList()).map(List::size).orElse(0);
        int toDelete = Optional.ofNullable(cud.getDeleteList()).map(List::size).orElse(0);

        currentCount = currentCount + toCreate - toDelete;

        if (restriction.getMinCount() > 0 && restriction.getMinCount() > currentCount) {
            result.getCudProblems().getGlobalProblems().add(new AttachmentGlobalProblem().setProblem(MIN_COUNT_NOT_REACHED));
        }

        if (restriction.getMaxCount() > 0 && restriction.getMaxCount() < currentCount) {
            result.getCudProblems().getGlobalProblems().add(new AttachmentGlobalProblem().setProblem(MAX_COUNT_EXCEEDED));
        }
    }

    private void validateAttachmentsSize(TwinAttachmentRestrictionEntity restriction, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) {
        if (restriction.getFileSizeMbLimit() == 0)
            return;

        if (cud.getCreateList() != null) {
            cud.getCreateList().forEach(attachment -> {
                if (attachment.getSize() > InformationVolumeUtils.convertToBytes(restriction.getFileSizeMbLimit())) {
                    result.getCudProblems().getCreateProblems().add(new AttachmentCreateProblem().setProblem(INVALID_SIZE));
                }
            });
        }

        if (cud.getUpdateList() != null) {
            cud.getUpdateList().forEach(attachment -> {
                if (attachment.getSize() > InformationVolumeUtils.convertToBytes(restriction.getFileSizeMbLimit())) {
                    result.getCudProblems().getUpdateProblems().add(new AttachmentUpdateProblem().setId(attachment.getId().toString()).setProblem(INVALID_SIZE));
                }
            });
        }
    }

    private void validateAttachmentsNameRegexp(TwinAttachmentRestrictionEntity restriction, EntityCUD<TwinAttachmentEntity> cud, AttachmentCUDValidateResult result) {
        if (restriction.getFileNameRegexp() == null)
            return;

        if (cud.getCreateList() != null) {
            cud.getCreateList().forEach(attachment -> {
                String fileName = attachment.getTitle();
                if (fileName != null && !fileName.matches(restriction.getFileNameRegexp())) {
                    result.getCudProblems().getCreateProblems().add(new AttachmentCreateProblem().setProblem(INVALID_NAME));
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
        if (restriction.getFileExtensionLimit() == null)
            return;

        List<String> allowedExtensions = Arrays.asList(
                restriction.getFileExtensionLimit().toLowerCase().split(",")
        );

        if (cud.getCreateList() != null) {
            cud.getCreateList().forEach(attachment -> {
                String ext = getFileExtension(attachment.getTitle()).toLowerCase();
                if (!allowedExtensions.contains(ext)) {
                    result.getCudProblems().getCreateProblems().add(
                            new AttachmentCreateProblem().setProblem(INVALID_TYPE));
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
        if (filename == null)
            return "";

        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex + 1);
    }
}
