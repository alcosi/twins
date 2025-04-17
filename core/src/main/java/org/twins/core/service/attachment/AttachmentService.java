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
import org.twins.core.dao.history.HistoryType;
import org.twins.core.dao.history.context.HistoryContextAttachment;
import org.twins.core.dao.history.context.HistoryContextAttachmentChange;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.*;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService extends EntitySecureFindServiceImpl<TwinAttachmentEntity> {
    private final TwinAttachmentRepository twinAttachmentRepository;
    private final TwinAttachmentModificationRepository twinAttachmentModificationRepository;
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
            final UUID uuid = UUID.randomUUID();
            attachmentEntity
                    .setId(uuid) // need for history
                    .setCreatedByUserId(apiUser.getUserId())
                    .setCreatedByUser(apiUser.getUser());
            twinChangesCollector.add(attachmentEntity);
            if (twinChangesCollector.isHistoryCollectorEnabled())
                twinChangesCollector.getHistoryCollector(attachmentEntity.getTwin()).add(historyService.attachmentCreate(attachmentEntity));
            if (!CollectionUtils.isEmpty(attachmentEntity.getModifications())) {
                attachmentEntity.getModifications().forEach(mod -> {
                    mod.setTwinAttachment(attachmentEntity);
                    mod.setTwinAttachmentId(uuid);
                });
                twinChangesCollector.addAll(attachmentEntity.getModifications());
            }
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

    public void loadAttachments(TwinEntity twinEntity) {
        loadAttachments(Collections.singletonList(twinEntity));
    }

    public void loadAttachments(Collection<TwinEntity> twinEntityList) {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getAttachmentKit() == null) {
                twinEntity.setAttachmentKit(new Kit<>(new ArrayList<>(), TwinAttachmentEntity::getId));
                needLoad.put(twinEntity.getId(), twinEntity);
            }
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
        for (Map.Entry<UUID, TwinEntity> entry : needLoad.entrySet()) {
            List<TwinAttachmentEntity> twinAttachmentsList = attachmentMap.get(entry.getKey());
            if(!CollectionUtils.isEmpty(twinAttachmentsList))
                entry.getValue().getAttachmentKit().addAll(twinAttachmentsList);
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

    public void loadAttachmentModifications(TwinAttachmentEntity attachmentEntity) throws ServiceException {
        loadAttachmentModifications(Collections.singletonList(attachmentEntity));
    }

    public void loadAttachmentModifications(Collection<TwinAttachmentEntity> collection) throws ServiceException {
        Kit<TwinAttachmentEntity, UUID> needLoad = new Kit<>(TwinAttachmentEntity::getId);
        for (TwinAttachmentEntity attachmentEntity : collection) {
            if (attachmentEntity.getModifications() != null)
                continue;
            needLoad.add(attachmentEntity);
            attachmentEntity.setModifications(new Kit<>(TwinAttachmentModificationEntity::getModificationType));
        }
        if (needLoad.isEmpty())
            return;
        List<TwinAttachmentModificationEntity> modifications = twinAttachmentModificationRepository.findAllByTwinAttachmentIdIn(needLoad.getIdSet());
        if (CollectionUtils.isEmpty(modifications))
            return;
        KitGrouped<TwinAttachmentModificationEntity, UUID, UUID> modificationsKit =
                new KitGrouped<>(modifications, TwinAttachmentModificationEntity::getId, TwinAttachmentModificationEntity::getTwinAttachmentId);
        for (TwinAttachmentEntity attachment : needLoad.getCollection()) {
            List<TwinAttachmentModificationEntity> innerArray = modificationsKit.getGrouped(attachment.getId());
            if (innerArray == null)
                continue;
            attachment.getModifications().addAll(innerArray);
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

            if (twinChangesCollector.collectIfChanged(dbAttachmentEntity, TwinAttachmentEntity.Fields.twinId, dbAttachmentEntity.getTwinId(), attachmentEntity.getTwinId())) {
                throw new ServiceException(ErrorCodeTwins.TWIN_ATTACHMENT_CAN_NOT_BE_RELINKED, "This attachment belongs to another twin");
            }
            if (twinChangesCollector.collectIfChanged(dbAttachmentEntity, TwinAttachmentEntity.Fields.twinCommentId, dbAttachmentEntity.getTwinCommentId(), attachmentEntity.getTwinCommentId())) {
                throw new ServiceException(ErrorCodeTwins.TWIN_ATTACHMENT_INCORRECT_COMMENT, "This attachment belongs to another comment");
            }
            if (twinChangesCollector.collectIfChanged(dbAttachmentEntity, TwinAttachmentEntity.Fields.description, dbAttachmentEntity.getDescription(), attachmentEntity.getDescription())) {
                historyItem.getContext().setNewDescription(attachmentEntity.getDescription());
                dbAttachmentEntity.setDescription(attachmentEntity.getDescription());
            }
            if (twinChangesCollector.collectIfChanged(dbAttachmentEntity, TwinAttachmentEntity.Fields.title, dbAttachmentEntity.getTitle(), attachmentEntity.getTitle())) {
                historyItem.getContext().setNewTitle(attachmentEntity.getTitle());
                dbAttachmentEntity.setTitle(attachmentEntity.getTitle());
            }
            if (twinChangesCollector.collectIfChanged(dbAttachmentEntity, TwinAttachmentEntity.Fields.storageFileKey, dbAttachmentEntity.getStorageFileKey(), attachmentEntity.getStorageFileKey())) {
                historyItem.getContext().setNewStorageFileKey(attachmentEntity.getStorageFileKey());
                dbAttachmentEntity.setStorageFileKey(attachmentEntity.getStorageFileKey());
            }
            if (attachmentEntity.getModifications() != null) {
                updateAttachmentModifications(attachmentEntity, dbAttachmentEntity, twinChangesCollector);
            }
            if (twinChangesCollector.collectIfChanged(dbAttachmentEntity, TwinAttachmentEntity.Fields.externalId, dbAttachmentEntity.getExternalId(), attachmentEntity.getExternalId())) {
                historyItem.getContext().setNewExternalId(attachmentEntity.getExternalId());
                dbAttachmentEntity.setExternalId(attachmentEntity.getExternalId());
            }
            if (twinChangesCollector.hasChanges(dbAttachmentEntity) && twinChangesCollector.isHistoryCollectorEnabled()) {
                twinChangesCollector.getHistoryCollector(dbAttachmentEntity.getTwin()).add(historyItem);
            }
        }
    }

    private void updateAttachmentModifications(TwinAttachmentEntity attachmentEntity,
                                               TwinAttachmentEntity dbAttachmentEntity,
                                               TwinChangesCollector twinChangesCollector) throws ServiceException {
        loadAttachmentModifications(dbAttachmentEntity);
        List<TwinAttachmentModificationEntity> incomingMods = new ArrayList<>(attachmentEntity.getModifications());

        List<TwinAttachmentModificationEntity> toUpdate = new ArrayList<>();
        List<TwinAttachmentModificationEntity> toCreate = new ArrayList<>();

        for (TwinAttachmentModificationEntity incomingMod : incomingMods) {
            incomingMod
                    .setTwinAttachmentId(dbAttachmentEntity.getId())
                    .setTwinAttachment(dbAttachmentEntity); // for sure
            String modType = incomingMod.getModificationType();
            TwinAttachmentModificationEntity existingMod = dbAttachmentEntity.getModifications().get(modType);
            if (existingMod == null) {
                toCreate.add(incomingMod);
            } else {
                if (!Objects.equals(existingMod.getStorageFileKey(), incomingMod.getStorageFileKey())) {
                    existingMod
                            .setStorageFileKey(incomingMod.getStorageFileKey())
                            .setTwinAttachment(dbAttachmentEntity);
                    toUpdate.add(existingMod);
                }
                dbAttachmentEntity.getModifications().remove(existingMod);
            }
        }
        Collection<TwinAttachmentModificationEntity> toDelete = dbAttachmentEntity.getModifications().getCollection();
        if (!toDelete.isEmpty()) twinChangesCollector.deleteAll(toDelete);
        if (!toUpdate.isEmpty()) twinChangesCollector.addAll(toUpdate);
        if (!toCreate.isEmpty()) twinChangesCollector.addAll(toCreate);
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
        loadAttachmentModifications(attachmentDeleteList);
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

    public int countByTwinAndField(UUID twinId, UUID twinClassFieldId) {
        return twinAttachmentRepository.findByTwinIdAndTwinClassFieldId(twinId, twinClassFieldId).size();
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

    public enum CommentRelinkMode {
        denied,
        allowed
    }
}
