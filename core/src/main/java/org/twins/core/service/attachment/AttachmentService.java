package org.twins.core.service.attachment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.cambium.common.Kit;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.history.HistoryType;
import org.twins.core.dao.history.context.HistoryContextAttachment;
import org.twins.core.dao.history.context.HistoryContextAttachmentChange;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinAttachmentRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.history.HistoryCollector;
import org.twins.core.service.history.HistoryCollectorMultiTwin;
import org.twins.core.service.history.HistoryItem;
import org.twins.core.service.history.HistoryService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {
    final EntitySmartService entitySmartService;
    final TwinAttachmentRepository twinAttachmentRepository;
    final HistoryService historyService;

    public UUID checkAttachmentId(UUID attachmentId, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(attachmentId, twinAttachmentRepository, checkMode);
    }

    public TwinAttachmentEntity findAttachment(UUID attachmentId, EntitySmartService.FindMode findMode) throws ServiceException {
        return entitySmartService.findById(attachmentId, twinAttachmentRepository, findMode);
    }

    @Transactional
    public List<TwinAttachmentEntity> addAttachments(TwinEntity twinEntity, UserEntity userEntity, List<TwinAttachmentEntity> attachments) throws ServiceException {
        HistoryCollector historyCollector = new HistoryCollector();
        for (TwinAttachmentEntity attachmentEntity : attachments) {
            attachmentEntity
                    .setId(UUID.randomUUID()) // need for history
                    .setTwinId(twinEntity.getId())
                    .setTwin(twinEntity)
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(userEntity.getId())
                    .setCreatedByUser(userEntity);
            historyCollector.add(historyService.attachmentCreate(attachmentEntity));
        }
        List<TwinAttachmentEntity> ret = IterableUtils.toList(entitySmartService.saveAllAndLog(attachments, twinAttachmentRepository));
        historyService.saveHistory(twinEntity, historyCollector);
        return ret;
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

    @Transactional
    public void deleteById(ApiUser apiUser, UUID attachmentId) throws ServiceException {
        TwinAttachmentEntity attachmentEntity = twinAttachmentRepository.getById(attachmentId);
        if (attachmentEntity == null)
            return;
        log.info(attachmentEntity.logDetailed() + " will be deleted");
        entitySmartService.deleteAndLog(attachmentId, twinAttachmentRepository);
        historyService.saveHistory(attachmentEntity.getTwin(), HistoryType.attachmentDelete, new HistoryContextAttachment()
                .shotAttachment(attachmentEntity));
    }

    @Transactional
    public void updateAttachments(List<TwinAttachmentEntity> attachmentEntityList) throws ServiceException {
        updateAttachments(attachmentEntityList, CommentRelinkMode.denied);
    }

    @Transactional
    public void updateAttachments(List<TwinAttachmentEntity> attachmentEntityList, CommentRelinkMode commentRelinkMode) throws ServiceException {
        if (CollectionUtils.isEmpty(attachmentEntityList))
            return;
        ChangesHelper changesHelper = new ChangesHelper();
        TwinAttachmentEntity dbAttachmentEntity;
        HistoryCollectorMultiTwin historyCollector = new HistoryCollectorMultiTwin();
        List<TwinAttachmentEntity> saveList = new ArrayList<>();
        for (TwinAttachmentEntity attachmentEntity : attachmentEntityList) {
            changesHelper.flush();
            dbAttachmentEntity = entitySmartService.findById(attachmentEntity.getId(), twinAttachmentRepository, EntitySmartService.FindMode.ifEmptyThrows);
            HistoryItem<HistoryContextAttachmentChange> historyItem = historyService.attachmentUpdate(attachmentEntity);
            if (changesHelper.isChanged("commentId", dbAttachmentEntity.getTwinCommentId(), attachmentEntity.getTwinCommentId())) {
                if (CommentRelinkMode.denied.equals(commentRelinkMode))
                    throw new ServiceException(ErrorCodeTwins.TWIN_ATTACHMENT_INCORRECT_COMMENT, "This attachment belongs to another comment");
                //todo add history context
                dbAttachmentEntity.setTwinCommentId(attachmentEntity.getTwinCommentId());
            }
            if (changesHelper.isChanged("description", dbAttachmentEntity.getDescription(), attachmentEntity.getDescription())) {
                historyItem.getContext().setNewDescription(attachmentEntity.getDescription());
                dbAttachmentEntity.setDescription(attachmentEntity.getDescription());
            }
            if (changesHelper.isChanged("title", dbAttachmentEntity.getTitle(), attachmentEntity.getTitle())) {
                historyItem.getContext().setNewTitle(attachmentEntity.getTitle());
                dbAttachmentEntity.setTitle(attachmentEntity.getTitle());
            }
            if (changesHelper.isChanged("storageLink", dbAttachmentEntity.getStorageLink(), attachmentEntity.getStorageLink())) {
                historyItem.getContext().setNewStorageLink(attachmentEntity.getStorageLink());
                dbAttachmentEntity.setStorageLink(attachmentEntity.getStorageLink());
            }
            if (changesHelper.isChanged("externalId", dbAttachmentEntity.getExternalId(), attachmentEntity.getExternalId())) {
                historyItem.getContext().setNewExternalId(attachmentEntity.getExternalId());
                dbAttachmentEntity.setExternalId(attachmentEntity.getExternalId());
            }
            if (changesHelper.hasChanges()) {
                saveList.add(dbAttachmentEntity);
                historyCollector.forTwin(dbAttachmentEntity.getTwin()).add(historyItem);
            }
        }
        if (CollectionUtils.isEmpty(saveList)) {
            entitySmartService.saveAllAndLog(saveList, twinAttachmentRepository);
            historyService.saveHistory(historyCollector);
        }
    }

    private String createChangesLogString(String field, String oldValue, String newValue) {
        return field + " was changed from[" + oldValue + "] to[" + newValue + "]";
    }

    @Transactional
    public void deleteAttachments(UUID twinId, List<UUID> attachmentDeleteUUIDList) throws ServiceException {
        if (CollectionUtils.isEmpty(attachmentDeleteUUIDList))
            return;
        List<TwinAttachmentEntity> deleteEntityList = twinAttachmentRepository.findByTwinIdAndIdIn(twinId, attachmentDeleteUUIDList); //we have to load to create informative history
        if (CollectionUtils.isEmpty(deleteEntityList))
            return;
        HistoryCollector historyCollector = new HistoryCollector();
        TwinEntity twinEntity = null;
        for (TwinAttachmentEntity attachmentEntity : deleteEntityList) {
            if (twinEntity == null)
                twinEntity = attachmentEntity.getTwin(); // we need twinEntity for history save
            log.info(attachmentEntity.logDetailed() + " will be deleted");
            historyCollector.add(historyService.attachmentDelete(attachmentEntity));
        }
        twinAttachmentRepository.deleteAllByTwinIdAndIdIn(twinId, attachmentDeleteUUIDList);
        historyService.saveHistory(twinEntity, historyCollector);
    }

    public enum CommentRelinkMode {
        denied,
        allowed
    }
}
