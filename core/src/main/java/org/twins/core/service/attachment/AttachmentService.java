package org.twins.core.service.attachment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.action.TwinAction;
import org.twins.core.dao.attachment.*;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.history.HistoryType;
import org.twins.core.dao.history.context.HistoryContextAttachment;
import org.twins.core.dao.history.context.HistoryContextAttachmentChange;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.*;
import org.twins.core.domain.attachment.AttachmentQuotas;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.history.HistoryItem;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.twin.TwinActionService;
import org.twins.core.service.twin.TwinService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.cambium.common.util.InformationVolumeUtils.convertToGb;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService extends EntitySecureFindServiceImpl<TwinAttachmentEntity> {
    private final TwinAttachmentRepository twinAttachmentRepository;
    private final HistoryService historyService;
    private final TwinActionService twinActionService;
    private final TwinChangesService twinChangesService;
    private final AuthService authService;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final DomainService domainService;
    private final AttachmentActionService attachmentActionService;

    public boolean checkOnDirect(TwinAttachmentEntity twinAttachmentEntity) {
        return twinAttachmentEntity.getTwinflowTransitionId() == null
                && twinAttachmentEntity.getTwinCommentId() == null
                && twinAttachmentEntity.getTwinClassFieldId() == null;
    }

    public TwinAttachmentEntity findAttachment(UUID attachmentId, EntitySmartService.FindMode findMode) throws ServiceException {
        return entitySmartService.findById(attachmentId, twinAttachmentRepository, findMode);
    }

    @Transactional
    public List<TwinAttachmentEntity> addAttachments(List<TwinAttachmentEntity> attachments, TwinEntity twinEntity) throws ServiceException {
        checkAndSetAttachmentTwin(attachments, twinEntity);
        return addAttachments(attachments);
    }

    @Transactional
    public List<TwinAttachmentEntity> addAttachments(List<TwinAttachmentEntity> attachments) throws ServiceException {
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        addAttachments(attachments, twinChangesCollector);
        TwinChangesApplyResult changesApplyResult = twinChangesService.applyChanges(twinChangesCollector);
        return changesApplyResult.getForClassAsList(TwinAttachmentEntity.class);
    }

    public void addAttachments(List<TwinAttachmentEntity> attachments, TwinChangesCollector twinChangesCollector) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        loadTwins(attachments);
        for (TwinAttachmentEntity attachmentEntity : attachments) {
            twinActionService.checkAllowed(attachmentEntity.getTwin(), TwinAction.ATTACHMENT_ADD);
            attachmentEntity
                    .setId(UUID.randomUUID()) // need for history
                    .setCreatedByUserId(apiUser.getUserId())
                    .setCreatedByUser(apiUser.getUser());
            twinChangesCollector.add(attachmentEntity);
            if (twinChangesCollector.isHistoryCollectorEnabled())
                twinChangesCollector.getHistoryCollector(attachmentEntity.getTwin()).add(historyService.attachmentCreate(attachmentEntity));
        }
    }

    public void checkAndSetAttachmentTwin(List<TwinAttachmentEntity> attachments, TwinEntity twinEntity) throws ServiceException {
        for (TwinAttachmentEntity attachmentEntity : attachments) {
            //twin relink is not security safe
            if (attachmentEntity.getTwinId() != null && !attachmentEntity.getTwinId().equals(twinEntity.getId()))
                throw new ServiceException(ErrorCodeTwins.TWIN_ATTACHMENT_CAN_NOT_BE_RELINKED);
            attachmentEntity
                    .setTwinId(twinEntity.getId())
                    .setTwin(twinEntity);
        }
    }

    public void loadTwins(List<TwinAttachmentEntity> attachments) throws ServiceException {
        //be careful, TwinAttachmentEntity can be without id, because it's not saved yet, do not use kit.getMap function
        KitGrouped<TwinAttachmentEntity, UUID, UUID> needLoad = new KitGrouped<>(TwinAttachmentEntity::getId, TwinAttachmentEntity::getTwinId);
        for (TwinAttachmentEntity attachmentEntity : attachments) {
            if (attachmentEntity.getTwinId() == null)
                throw new ServiceException(ErrorCodeTwins.TWIN_ATTACHMENT_EMPTY_TWIN_ID);
            if (attachmentEntity.getTwin() == null)
                needLoad.add(attachmentEntity);
        }
        if (needLoad.isEmpty())
            return;
        Kit<TwinEntity, UUID> twinsKit = twinService.findEntitiesSafe(needLoad.getGroupedMap().keySet());
        for (var entry : needLoad.getGroupedMap().entrySet()) {
            for (TwinAttachmentEntity attachmentEntity : entry.getValue()) {
                attachmentEntity.setTwin(twinsKit.get(attachmentEntity.getTwinId()));
            }
        }
    }

    public List<TwinAttachmentEntity> findAttachmentByTwinId(UUID twinId) {
        return twinAttachmentRepository.findByTwinId(twinId);
    }

    public Kit<TwinAttachmentEntity, UUID> loadAttachments(TwinEntity twinEntity) {
        if (twinEntity.getAttachmentKit() != null)
            return twinEntity.getAttachmentKit();
        List<TwinAttachmentEntity> attachmentEntityList = twinAttachmentRepository.findByTwinId(twinEntity.getId());
        if (attachmentEntityList != null)
            twinEntity.setAttachmentKit(new Kit<>(attachmentEntityList, TwinAttachmentEntity::getId));
        return twinEntity.getAttachmentKit();
    }

    public void loadAttachments(Collection<TwinEntity> twinEntityList) {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getAttachmentKit() == null)
                needLoad.put(twinEntity.getId(), twinEntity);
        if (needLoad.isEmpty())
            return;
        List<TwinAttachmentEntity> attachmentEntityList = twinAttachmentRepository.findByTwinIdIn(needLoad.keySet());
        if (CollectionUtils.isEmpty(attachmentEntityList))
            return;
        Map<UUID, List<TwinAttachmentEntity>> attachmentMap = new HashMap<>(); // key - twinId
        for (TwinAttachmentEntity attachmentEntity : attachmentEntityList) { //grouping by twin
            attachmentMap.computeIfAbsent(attachmentEntity.getTwinId(), k -> new ArrayList<>());
            attachmentMap.get(attachmentEntity.getTwinId()).add(attachmentEntity);
        }
        TwinEntity twinEntity;
        List<TwinAttachmentEntity> twinAttachmentList;
        for (Map.Entry<UUID, TwinEntity> entry : needLoad.entrySet()) {
            twinEntity = entry.getValue();
            twinAttachmentList = attachmentMap.get(entry.getKey());
            twinEntity.setAttachmentKit(new Kit<>(twinAttachmentList, TwinAttachmentEntity::getId));
        }
    }

    public void loadAttachmentsCount(TwinEntity twinEntity) {
        loadAttachmentsCount(Collections.singletonList(twinEntity));
    }

    public void loadAttachmentsCount(Collection<TwinEntity> twinEntityList) {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getTwinAttachmentsCount() == null) {
                needLoad.put(twinEntity.getId(), twinEntity);
                twinEntity.setTwinAttachmentsCount(TwinAttachmentsCount.EMPTY);
            }
        if (needLoad.isEmpty())
            return;
        List<Object[]> objects = twinAttachmentRepository.countAttachmentsByTwinIds(new ArrayList<>(needLoad.keySet()));
        if (CollectionUtils.isEmpty(objects))
            return;
        Map<UUID, Object[]> resultMap = objects.stream()
                .collect(Collectors.toMap(result -> (UUID) result[0], result -> result));
        for (TwinEntity twin : needLoad.values()) {
            Object[] innerArray = resultMap.get(twin.getId());
            if (innerArray == null)
                return;
            twin.setTwinAttachmentsCount(new TwinAttachmentsCount(
                    parseInt(innerArray[1]),
                    parseInt(innerArray[3]),
                    parseInt(innerArray[3]),
                    parseInt(innerArray[4]))
            );
        }
    }

    private static int parseInt(Object obj) {
        return ((Long) obj).intValue();
    }

    @Transactional
    public void deleteById(ApiUser apiUser, UUID attachmentId) throws ServiceException {
        TwinAttachmentEntity attachmentEntity = twinAttachmentRepository.getById(attachmentId);
        attachmentActionService.checkAllowed(attachmentEntity, TwinAttachmentAction.DELETE);
        if (attachmentEntity == null)
            return;
        log.info(attachmentEntity.logDetailed() + " will be deleted");
        entitySmartService.deleteAndLog(attachmentId, twinAttachmentRepository);
        historyService.saveHistory(attachmentEntity.getTwin(), HistoryType.attachmentDelete, new HistoryContextAttachment()
                .shotAttachment(attachmentEntity));
    }

    @Transactional
    public void updateAttachments(List<TwinAttachmentEntity> attachmentEntityList, TwinEntity twinEntity) throws ServiceException {
        checkAndSetAttachmentTwin(attachmentEntityList, twinEntity);
        updateAttachments(attachmentEntityList);
    }

    @Transactional
    public void updateAttachments(List<TwinAttachmentEntity> attachmentEntityList) throws ServiceException {
        if (CollectionUtils.isEmpty(attachmentEntityList))
            return;
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        updateAttachments(attachmentEntityList, twinChangesCollector);
        if (twinChangesCollector.hasChanges()) {
            twinChangesService.applyChanges(twinChangesCollector);
        }
    }

    //todo collect only delta for correct drafting (minimize lockers)
    public void updateAttachments(List<TwinAttachmentEntity> attachmentEntityList, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (CollectionUtils.isEmpty(attachmentEntityList))
            return;
        Kit<TwinAttachmentEntity, UUID> newAttachmentKit = new Kit<>(attachmentEntityList, TwinAttachmentEntity::getId);
        Kit<TwinAttachmentEntity, UUID> dbAttachmentKit = new Kit<>(twinAttachmentRepository.findByIdIn(newAttachmentKit.getIdSet()), TwinAttachmentEntity::getId);
        TwinAttachmentEntity dbAttachmentEntity;
        for (TwinAttachmentEntity attachmentEntity : attachmentEntityList) {
            dbAttachmentEntity = dbAttachmentKit.get(attachmentEntity.getId());
            attachmentActionService.checkAllowed(dbAttachmentEntity, TwinAttachmentAction.EDIT);
            HistoryItem<HistoryContextAttachmentChange> historyItem = historyService.attachmentUpdate(attachmentEntity);

            updateEntityField(tierUpdate, dbTierEntity, TierEntity::getName,
                    TierEntity::setName, TierEntity.Fields.name, changesHelper);

            if (twinChangesCollector.collectIfChanged(attachmentEntity, TwinAttachmentEntity.Fields.twinId, dbAttachmentEntity.getTwinId(), attachmentEntity.getTwinId())) {
                // twin relink is not security safe, so it's currently denied. perhaps we can move it to permissions
                throw new ServiceException(ErrorCodeTwins.TWIN_ATTACHMENT_CAN_NOT_BE_RELINKED, "This attachment belongs to another twin");
            }
            if (twinChangesCollector.collectIfChanged(attachmentEntity, TwinAttachmentEntity.Fields.twinCommentId, dbAttachmentEntity.getTwinCommentId(), attachmentEntity.getTwinCommentId())) {
                // comment relink is not security safe, so it's currently denied. perhaps we can move it to permissions
                throw new ServiceException(ErrorCodeTwins.TWIN_ATTACHMENT_INCORRECT_COMMENT, "This attachment belongs to another comment");
            }
            if (twinChangesCollector.collectIfChanged(attachmentEntity, TwinAttachmentEntity.Fields.description, dbAttachmentEntity.getDescription(), attachmentEntity.getDescription())) {
                historyItem.getContext().setNewDescription(attachmentEntity.getDescription());
                dbAttachmentEntity.setDescription(attachmentEntity.getDescription());
            }
            if (twinChangesCollector.collectIfChanged(attachmentEntity, TwinAttachmentEntity.Fields.title, dbAttachmentEntity.getTitle(), attachmentEntity.getTitle())) {
                historyItem.getContext().setNewTitle(attachmentEntity.getTitle());
                dbAttachmentEntity.setTitle(attachmentEntity.getTitle());
            }
            //TODO updateEntityField
            if (twinChangesCollector.collectIfChanged(attachmentEntity, "storageLink", dbAttachmentEntity.getStorageLink(), attachmentEntity.getStorageLink())) {
                historyItem.getContext().setNewStorageLink(attachmentEntity.getStorageLink());
                dbAttachmentEntity.setStorageLink(attachmentEntity.getStorageLink());
            }
            if (twinChangesCollector.collectIfChanged(attachmentEntity, TwinAttachmentEntity.Fields.modificationLinks, dbAttachmentEntity.getModificationLinks(), attachmentEntity.getModificationLinks())) {
                dbAttachmentEntity.setModificationLinks(attachmentEntity.getModificationLinks());
            }
            if (twinChangesCollector.collectIfChanged(attachmentEntity, TwinAttachmentEntity.Fields.externalId, dbAttachmentEntity.getExternalId(), attachmentEntity.getExternalId())) {
                historyItem.getContext().setNewExternalId(attachmentEntity.getExternalId());
                dbAttachmentEntity.setExternalId(attachmentEntity.getExternalId());
            }
            if (twinChangesCollector.hasChanges(attachmentEntity) && twinChangesCollector.isHistoryCollectorEnabled()) {
                twinChangesCollector.getHistoryCollector(attachmentEntity.getTwin()).add(historyItem);
            }
        }
    }

    private String createChangesLogString(String field, String oldValue, String newValue) {
        return field + " was changed from[" + oldValue + "] to[" + newValue + "]";
    }

    public void deleteAttachments(TwinEntity twinEntity, List<TwinAttachmentEntity> attachmentDeleteList) throws ServiceException {
        if (CollectionUtils.isEmpty(attachmentDeleteList))
            return;
        checkAndSetAttachmentTwin(attachmentDeleteList, twinEntity);
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        deleteAttachments(attachmentDeleteList, twinChangesCollector);
        twinChangesService.applyChanges(twinChangesCollector);
    }

    public void deleteAttachments(List<TwinAttachmentEntity> attachmentDeleteList, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (CollectionUtils.isEmpty(attachmentDeleteList))
            return;
        loadTwins(attachmentDeleteList);
        for (TwinAttachmentEntity attachmentEntity : attachmentDeleteList) {
            if (!attachmentActionService.isAllowed(attachmentEntity, TwinAttachmentAction.DELETE)) {// N+1
                log.info("{} cannot be deleted because it is not allowed", attachmentEntity.logShort());
                continue;
            }
            twinChangesCollector.delete(attachmentEntity);
            if (twinChangesCollector.isHistoryCollectorEnabled())
                twinChangesCollector.getHistoryCollector(attachmentEntity.getTwin()).add(historyService.attachmentDelete(attachmentEntity));
        }
    }

    public void cudAttachments(TwinEntity twinEntity, EntityCUD<TwinAttachmentEntity> attachmentCUD, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (attachmentCUD == null)
            return;
        if (CollectionUtils.isNotEmpty(attachmentCUD.getCreateList())) {
            checkAndSetAttachmentTwin(attachmentCUD.getCreateList(), twinEntity);
            addAttachments(attachmentCUD.getCreateList(), twinChangesCollector);
        }
        if (CollectionUtils.isNotEmpty(attachmentCUD.getUpdateList())) {
            checkAndSetAttachmentTwin(attachmentCUD.getUpdateList(), twinEntity);
            updateAttachments(attachmentCUD.getUpdateList(), twinChangesCollector);
        }
        if (CollectionUtils.isNotEmpty(attachmentCUD.getDeleteList())) {
            checkAndSetAttachmentTwin(attachmentCUD.getDeleteList(), twinEntity);
            deleteAttachments(attachmentCUD.getDeleteList(), twinChangesCollector);
        }
    }

    @Override
    public CrudRepository<TwinAttachmentEntity, UUID> entityRepository() {
        return twinAttachmentRepository;
    }

    @Override
    public Function<TwinAttachmentEntity, UUID> entityGetIdFunction() {
        return TwinAttachmentEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinAttachmentEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinAttachmentEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public AttachmentCUDValidateResult validateCUD(UUID twinId, EntityCUD<TwinAttachmentEntity> cud) throws ServiceException {
        AttachmentCUDValidateResult result = new AttachmentCUDValidateResult();
        AttachmentQuotas tierQuotas = domainService.getTierQuotas();

        List<TwinAttachmentEntity> deletes = cud.getDeleteList();
        List<TwinAttachmentEntity> updates = cud.getUpdateList();
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
        List<UUID> updateIds = updates.stream().map(TwinAttachmentEntity::getId).collect(Collectors.toList());
        Kit<TwinAttachmentEntity, UUID> existingEntities = findEntitiesSafe(updateIds);

        for (TwinAttachmentEntity update : updates) {
            TwinAttachmentEntity existingEntity = existingEntities.get(update.getId());
            if (null == existingEntity)
                throw new ServiceException(ErrorCodeTwins.ATTACHMENTS_NOT_VALID, "Updatable attachment [" + update.getId() + "] is not exists");
            if (!existingEntity.getTwinId().equals(twinId))
                throw new ServiceException(ErrorCodeTwins.ATTACHMENTS_NOT_VALID, "Deletable attachment [" + update.getId() + "] is not added to twin[" + twinId + "]");
            updates.add(existingEntity);
            result.getAttachmentsForUD().add(existingEntity);
            size = size - existingEntity.getSize() + update.getSize();
//            if (size > tierQuotas.getQuotaSize()) {
//                result.getCudProblems().getUpdateProblems().add(new AttachmentUpdateProblem()
//                        .setId(existingEntity.getId().toString())
//                        .setProblem(AttachmentFileCreateUpdateProblem.INVALID_SIZE));
//            }
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
        return result;
    }

    public enum CommentRelinkMode {
        denied,
        allowed
    }
}
