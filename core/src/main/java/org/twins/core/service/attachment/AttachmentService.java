package org.twins.core.service.attachment;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import io.github.breninsul.springHttpMessageConverter.inputStream.InputStreamResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.LoggerUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentModificationEntity;
import org.twins.core.dao.attachment.TwinAttachmentModificationRepository;
import org.twins.core.dao.attachment.TwinAttachmentRepository;
import org.twins.core.dao.history.context.HistoryContextAttachmentChange;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.*;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.enums.attachment.TwinAttachmentAction;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.storager.AddedFileKey;
import org.twins.core.featurer.storager.Storager;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.HistoryItem;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.storage.StorageService;
import org.twins.core.service.twin.TwinActionService;
import org.twins.core.service.twin.TwinService;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
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
    private final AttachmentActionService attachmentActionService;
    private final FeaturerService featurerService;
    private final StorageService storageService;

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

        EntityCUD<TwinAttachmentEntity> attachmentCUD = new EntityCUD<>();
        attachmentCUD.setCreateList(attachments);

        TwinUpdate twinUpdate = new TwinUpdate();
        twinUpdate
                .setAttachmentCUD(attachmentCUD)
                .setDbTwinEntity(twinEntity)
                .setLauncher(TwinOperation.Launcher.direct)
                .setTwinEntity(twinEntity);
        twinService.updateTwin(twinUpdate);
        return attachments;
    }

    @Transactional
    public List<TwinAttachmentEntity> addAttachments(List<TwinAttachmentEntity> attachments) throws ServiceException {
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        addAttachments(attachments, twinChangesCollector);
        TwinChangesApplyResult changesApplyResult = twinChangesService.applyChanges(twinChangesCollector);
        return changesApplyResult.getForClassAsList(TwinAttachmentEntity.class);
    }

    public void addAttachment(TwinAttachmentEntity attachment, TwinChangesCollector twinChangesCollector) throws ServiceException {
        addAttachments(Collections.singletonList(attachment), twinChangesCollector);
    }

    public void addAttachments(List<TwinAttachmentEntity> attachments, TwinChangesCollector twinChangesCollector) throws ServiceException {
        try {
            ApiUser apiUser = authService.getApiUser();
            UUID domainId = apiUser.getDomainId();
            UUID businessAccountId = apiUser.getBusinessAccountId();
            UUID userId = apiUser.getUserId();
            UserEntity user = apiUser.getUser();

            loadTwins(attachments);
            storageService.loadStorages(attachments);

            List<TwinEntity> twins = attachments.stream()
                    .map(TwinAttachmentEntity::getTwin)
                    .distinct()
                    .toList();
            twinActionService.checkAllowed(twins, TwinAction.ATTACHMENT_ADD);

            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                // todo somehow rewrite thread-local logic for api user to use scoped values
                attachments.forEach(attachmentEntity -> scope.fork(() -> {
                    UUID uuid = UuidUtils.generate();
                    LoggerUtils.logSession(uuid);
                    LoggerUtils.logController("addAttachments$");
                    LoggerUtils.logPrefix(STR."ADD_ATTACHMENT[\{uuid}]:");

                    try {
                        // ThreadLocal context
                        authService.setThreadLocalApiUser(domainId, businessAccountId, userId);

                        saveFile(attachmentEntity, uuid);

                        attachmentEntity
                                .setId(uuid)
                                .setCreatedByUserId(userId)
                                .setCreatedByUser(user);

                        if (StringUtils.isEmpty(attachmentEntity.getStorageFileKey())) {
                            throw new ServiceException(
                                    ErrorCodeTwins.ATTACHMENTS_NOT_VALID,
                                    "storageFileKey is empty"
                            );
                        }

                        twinChangesCollector.add(attachmentEntity);

                        if (twinChangesCollector.isHistoryCollectorEnabled()) {
                            twinChangesCollector
                                    .getHistoryCollector(attachmentEntity.getTwin())
                                    .add(historyService.attachmentCreate(attachmentEntity));
                        }

                        if (!CollectionUtils.isEmpty(attachmentEntity.getModifications())) {
                            attachmentEntity.getModifications().forEach(mod -> {
                                if (mod.getTwinAttachmentId() == null) {
                                    mod.setTwinAttachment(attachmentEntity);
                                    mod.setTwinAttachmentId(uuid);
                                }
                            });

                            twinChangesCollector.addAll(attachmentEntity.getModifications());
                        }

                    } catch (Throwable t) {
                        log.error("Unable to add attachment {}: {}", attachmentEntity.logNormal(), t.getMessage(), t);
                        throw t;
                    } finally {
                        authService.removeThreadLocalApiUser();
                        LoggerUtils.cleanMDC();
                    }

                    return null;
                }));

                scope.join().throwIfFailed(cause -> {
                    log.error("One or more attachments failed. First error:", cause);
                    return new ServiceException(ErrorCodeTwins.ATTACHMENTS_NOT_VALID);
                });
            }
        } catch (Throwable t) {
            throw new ServiceException(ErrorCodeTwins.ATTACHMENTS_NOT_VALID, "Unable to add attachments");
        }
    }

    protected void saveFile(TwinAttachmentEntity attachmentEntity, UUID uuid) throws ServiceException {
        //Not just link, have to add file to storage
        StorageEntity storage = attachmentEntity.getStorage();
        Storager fileService = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
        AddedFileKey addedFileKey;
        if (attachmentEntity.getAttachmentFile() != null) {
            addedFileKey = fileService.addFile(uuid, attachmentEntity.getAttachmentFile().content(), storage.getStoragerParams());
        } else {
            addedFileKey = fileService.addExternalUrlFile(uuid, attachmentEntity.getStorageFileKey(), storage.getStoragerParams());
        }

        modifyAttachment(attachmentEntity, addedFileKey);
    }

    private void modifyAttachment(TwinAttachmentEntity attachmentEntity, AddedFileKey addedFileKey) throws ServiceException {
        log.info("Replacing modifications, storage file key and size in attachmentEntity with data from file handler service.");

        attachmentEntity.setSize(addedFileKey.fileSize() != -1 ? addedFileKey.fileSize() : attachmentEntity.getSize());
        attachmentEntity.setStorageFileKey(addedFileKey.fileKey());

        if (CollectionUtils.isNotEmpty(addedFileKey.modifications())) {
            attachmentEntity.setModifications(
                    new Kit<>(
                            addedFileKey.modifications().stream()
                                    .map(
                                            (mod) -> new TwinAttachmentModificationEntity()
                                                    .setTwinAttachmentId(mod.twinAttachmentId())
                                                    .setTwinAttachment(attachmentEntity)
                                                    .setStorageFileKey(mod.storageFileKey())
                                                    .setModificationType(mod.modificationType() == null ? "ORIGINAL" : mod.modificationType())
                                    )
                                    .toList(),
                            TwinAttachmentModificationEntity::getStorageFileKey
                    )
            );
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
            if (!CollectionUtils.isEmpty(twinAttachmentsList))
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
        TwinAttachmentEntity attachement = twinAttachmentRepository.getById(attachmentId);
        attachmentActionService.checkAllowed(attachement, TwinAttachmentAction.DELETE);
        if (attachement == null)
            return;

        EntityCUD<TwinAttachmentEntity> attachmentCUD = new EntityCUD<>();
        attachmentCUD.setDeleteList(List.of(attachement));

        TwinUpdate twinUpdate = new TwinUpdate();
        twinUpdate
                .setAttachmentCUD(attachmentCUD)
                .setDbTwinEntity(attachement.getTwin())
                .setLauncher(TwinOperation.Launcher.direct)
                .setTwinEntity(attachement.getTwin());
        twinService.updateTwin(twinUpdate);
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

    public void updateAttachment(TwinAttachmentEntity attachmentEntity, TwinChangesCollector twinChangesCollector) throws ServiceException {
        updateAttachments(Collections.singletonList(attachmentEntity), twinChangesCollector);
    }

    //todo collect only delta for correct drafting (minimize lockers)
    public void updateAttachments(List<TwinAttachmentEntity> attachmentEntityList, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (CollectionUtils.isEmpty(attachmentEntityList))
            return;
        storageService.loadStorages(attachmentEntityList);
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
            if (twinChangesCollector.collectIfChanged(dbAttachmentEntity, TwinAttachmentEntity.Fields.order, dbAttachmentEntity.getOrder(), attachmentEntity.getOrder())) {
                historyItem.getContext().setNewOrder(attachmentEntity.getOrder());
                dbAttachmentEntity.setOrder(attachmentEntity.getOrder());
            }
            if (attachmentEntity.isFileChanged() || twinChangesCollector.collectIfChanged(dbAttachmentEntity, TwinAttachmentEntity.Fields.storageFileKey, dbAttachmentEntity.getStorageFileKey(), attachmentEntity.getStorageFileKey())) {
                deleteFile(dbAttachmentEntity);
                saveFile(attachmentEntity, dbAttachmentEntity.getId());
                dbAttachmentEntity.setStorageFileKey(attachmentEntity.getStorageFileKey());
                historyItem.getContext().setNewStorageFileKey(attachmentEntity.getStorageFileKey());
            }
            if (KitUtils.isNotEmpty(attachmentEntity.getModifications())) {
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
            if (StringUtils.isEmpty(incomingMod.getStorageFileKey())) {
                throw new ServiceException(ErrorCodeTwins.ATTACHMENTS_NOT_VALID, "storageFileKey is empty for modification");
            }
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

    public Map<UUID, Integer> countByTwinAndFields(TwinEntity twin, Collection<UUID> twinClassFieldIds) {
        if (twin.getId() == null || CollectionUtils.isEmpty(twinClassFieldIds)) {
            return Collections.emptyMap();
        }

        return twinAttachmentRepository.countAttachmentsGroupByField(twin.getId(), twinClassFieldIds)
                .stream()
                .collect(Collectors.toMap(
                        result -> (UUID) result[0],
                        result -> ((Number) result[1]).intValue()
                ));
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


    @Transactional(readOnly = true)
    public InputStreamResponse getAttachmentFile(UUID attachmentId) throws ServiceException {
        var attachment = findEntitySafe(attachmentId);
        StorageEntity storage = attachment.getStorage();
        Storager fileService = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
        var stream = fileService.getFileAsStream(attachment.getStorageFileKey(), storage.getStoragerParams());
        return stream;
    }


    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public TwinAttachmentEntity transferAttachment(UUID attachmentId, UUID newStorageId) throws ServiceException {
        UUID newAttachementId = UuidUtils.generate();

        var attachement = findEntitySafe(attachmentId);
        if (attachement.getStorageId().equals(newStorageId))
            return attachement;
        StorageEntity oldStorage = attachement.getStorage();
        StorageEntity newStorage = storageService.findEntitySafe(newStorageId);

        Storager oldStorager = featurerService.getFeaturer(oldStorage.getStorageFeaturerId(), Storager.class);
        Storager newStorager = featurerService.getFeaturer(newStorage.getStorageFeaturerId(), Storager.class);
        InputStream fileStream = oldStorager.getFileAsStream(attachement.getStorageFileKey(), oldStorage.getStoragerParams()).getContentStream();
        AddedFileKey addedFileKey = newStorager.addFile(newAttachementId, fileStream, newStorage.getStoragerParams());
        TwinAttachmentEntity newAttachement = attachement.clone();
        newAttachement.setId(attachmentId);
        newAttachement.setStorageId(newStorageId);
        newAttachement.setStorage(newStorage);
        newAttachement.setStorageFileKey(addedFileKey.fileKey());
        validateEntity(newAttachement, EntitySmartService.EntityValidateMode.beforeSave);
        newAttachement = twinAttachmentRepository.save(newAttachement);
        oldStorager.tryDeleteFile(attachement.getStorageFileKey(), oldStorage.getStoragerParams());
        twinAttachmentRepository.delete(attachement);
        return newAttachement;
    }

    public String getAttachmentUri(TwinAttachmentEntity attachment) throws ServiceException {
        if (attachment != null) {
            var featurer = featurerService.getFeaturer(attachment.getStorage().getStorageFeaturerId(), Storager.class);
            return featurer.getFileUri(attachment.getId(), attachment.getStorageFileKey(), attachment.getStorage().getStoragerParams()).toString();
        }
        return null;
    }

    protected void deleteFile(TwinAttachmentEntity attachment) throws ServiceException {
        StorageEntity storage = attachment.getStorage();
        Storager fileService = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
        fileService.tryDeleteFile(attachment.getStorageFileKey(), storage.getStoragerParams());
    }


}
