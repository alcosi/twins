package org.twins.core.service.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.cambium.i18n.service.I18nService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.action.TwinAction;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.history.*;
import org.twins.core.dao.history.context.*;
import org.twins.core.dao.history.context.snapshot.FieldSnapshot;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.RepositoryService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinActionService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class HistoryService extends EntitySecureFindServiceImpl<HistoryEntity> {
    @Lazy
    final TwinService twinService;
    final TwinClassFieldService twinClassFieldService;
    final HistoryRepository historyRepository;
    final HistoryTypeRepository historyTypeRepository;
    final HistoryTypeConfigDomainRepository historyTypeConfigDomainRepository;
    final HistoryTypeConfigTwinClassRepository historyTypeConfigTwinClassRepository;
    final HistoryTypeConfigTwinClassFieldRepository historyTypeConfigTwinClassFieldRepository;
    final RepositoryService repositoryService;
    private final TwinActionService twinActionService;
    final AuthService authService;
    final I18nService i18nService;

    @Override
    public CrudRepository<HistoryEntity, UUID> entityRepository() {
        return historyRepository;
    }

    @Override
    public Function<HistoryEntity, UUID> entityGetIdFunction() {
        return HistoryEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(HistoryEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(HistoryEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public PaginationResult<HistoryEntity> findHistory(UUID twinId, int childDepth, SimplePagination pagination) throws ServiceException {
        twinActionService.checkAllowed(twinId, TwinAction.HISTORY_VIEW);
        Pageable pageable = PaginationUtils.pageableOffset(pagination);
        Page<HistoryEntity> historyList;
        if (childDepth == 0)
            historyList = historyRepository.findByTwinId(twinId, pageable);
        else //todo support different depth
            historyList = historyRepository.findByTwinIdIncludeFirstLevelChildren(twinId, pageable);
        List<HistoryEntity> needMessageFinalization = new ArrayList<>();
        HistoryMutableDataCollector mutableDataCollector = new HistoryMutableDataCollector(i18nService);
        for (HistoryEntity historyEntity : historyList) {
            loadFreshestMessageDraft(historyEntity);
            if (!historyEntity.getFreshMessage().contains("${"))
                continue; //message is already filled by all data
            // we will check if template needs some data, that can be refreshed. If so, we collect data id and will try to load all
            // needed data in few queries
            if (historyEntity.getContext().collectMutableData(historyEntity.getFreshMessage(), mutableDataCollector))
                needMessageFinalization.add(historyEntity);
        }
        loadMutableData(mutableDataCollector);
        for (HistoryEntity historyEntity : needMessageFinalization) {
            historyEntity.getContext().spoofSnapshots(mutableDataCollector);
            // snapshots are spoofed with fresh data (or not if data can not be fetched because it was already deleted from DB)
            // so we can use them to finalize message
            historyEntity.setFreshMessage(createMessage(historyEntity.getFreshMessage(), historyEntity, true));
        }
        return PaginationUtils.convertInPaginationResult(historyList, pagination);
    }

    private void loadMutableData(HistoryMutableDataCollector mutableDataCollector) {
        if (CollectionUtils.isNotEmpty(mutableDataCollector.getTwinIdSet()))
            mutableDataCollector.setTwinKit(new Kit<>(
                    repositoryService.getTwinRepository().findByIdIn(mutableDataCollector.getTwinIdSet()), TwinEntity::getId));
        if (CollectionUtils.isNotEmpty(mutableDataCollector.getStatusIdSet()))
            mutableDataCollector.setStatusKit(new Kit<>(
                    repositoryService.getTwinStatusRepository().findByIdIn(mutableDataCollector.getStatusIdSet()), TwinStatusEntity::getId));
        if (CollectionUtils.isNotEmpty(mutableDataCollector.getUserIdSet()))
            mutableDataCollector.setUserKit(new Kit<>(
                    repositoryService.getUserRepository().findByIdIn(mutableDataCollector.getUserIdSet()), UserEntity::getId));
        if (CollectionUtils.isNotEmpty(mutableDataCollector.getLinkIdSet()))
            mutableDataCollector.setLinkKit(new Kit<>(
                    repositoryService.getLinkRepository().findByIdIn(mutableDataCollector.getLinkIdSet()), LinkEntity::getId));
        if (CollectionUtils.isNotEmpty(mutableDataCollector.getDataListOptionIdSet()))
            mutableDataCollector.setDataListOptionKit(new Kit<>(
                    repositoryService.getDataListOptionRepository().findByIdIn(mutableDataCollector.getDataListOptionIdSet()), DataListOptionEntity::getId));
    }

    public void saveHistory(TwinEntity twinEntity, HistoryType type, HistoryContext context) throws ServiceException {
        HistoryEntity historyEntity = createEntityIfWritable(twinEntity, type, context, getActor());
        if (historyEntity != null)
            entitySmartService.save(historyEntity, historyRepository, EntitySmartService.SaveMode.saveAndLogOnException);
    }

    public void saveHistory(HistoryCollectorMultiTwin multiTwinHistoryCollector) throws ServiceException {
        List<HistoryEntity> historyEntityList = convertToEntities(multiTwinHistoryCollector);
        if (historyEntityList == null) return;
        if (CollectionUtils.isNotEmpty(historyEntityList))
            entitySmartService.saveAllAndLog(historyEntityList, historyRepository);
    }

    public void saveHistory(TwinEntity twinEntity, HistoryCollector historyCollector) throws ServiceException {
        if (historyCollector == null || CollectionUtils.isEmpty(historyCollector.getHistoryList()))
            return;
        List<HistoryEntity> historyEntityList = new ArrayList<>();
        UserEntity actor = getActor();
        for (Pair<HistoryType, HistoryContext> pair : historyCollector.getHistoryList()) {
            HistoryEntity historyEntity = createEntityIfWritable(twinEntity, pair.getKey(), pair.getValue(), actor);
            historyEntityList.add(historyEntity);
        }
        if (CollectionUtils.isNotEmpty(historyEntityList))
            entitySmartService.saveAllAndLog(historyEntityList, historyRepository);
    }

    public List<HistoryEntity> convertToEntities(HistoryCollectorMultiTwin multiTwinHistoryCollector) throws ServiceException {
        if (MapUtils.isEmpty(multiTwinHistoryCollector.getMultiTwinHistory()))
            return null;
        List<HistoryEntity> historyEntityList = new ArrayList<>();
        UserEntity actor = getActor();
        for (Pair<TwinEntity, HistoryCollector> twinHistory : multiTwinHistoryCollector.getMultiTwinHistory().values()) {
            if (twinHistory.getValue() == null || CollectionUtils.isEmpty(twinHistory.getValue().getHistoryList()))
                continue;
            for (Pair<HistoryType, HistoryContext> pair : twinHistory.getValue().getHistoryList()) {
                HistoryEntity historyEntity = createEntityIfWritable(twinHistory.getKey(), pair.getKey(), pair.getValue(), actor);
                historyEntityList.add(historyEntity);
            }
        }
        return historyEntityList;
    }

    public HistoryEntity createEntityIfWritable(TwinEntity twinEntity, HistoryType type, HistoryContext context, UserEntity actor) throws ServiceException {
        HistoryEntity historyEntity = new HistoryEntity()
                .setTwin(twinEntity)
                .setTwinId(twinEntity.getId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setActorUser(actor)
                .setActorUserId(actor.getId())
                .setHistoryType(type)
                .setContext(context)
                .setHistoryBatchId(authService.getApiUser().getRequestId());
        fillHistoryEntity(historyEntity, twinEntity, context);
        return historyEntity;
    }

    public void fillHistoryEntity(HistoryEntity historyEntity, TwinEntity twinEntity, HistoryContext context) throws ServiceException {
        if (context != null) {
            if (context.getField() != null) {
                historyEntity.setTwinClassFieldId(context.getField().getId());
                if (historyEntity.getHistoryType() == HistoryType.fieldChanged) //we will use more detailed type
                    if (StringUtils.isEmpty(context.templateFromValue()))
                        historyEntity.setHistoryType(HistoryType.fieldCreated);
                    else if (StringUtils.isEmpty(context.templateToValue()))
                        historyEntity.setHistoryType(HistoryType.fieldDeleted);
            } else if (context instanceof IHistoryContextLink linkChange) {
                //we will try to detect if it's some field configured to represent current link
                TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.getFieldIdConfiguredForLink(twinEntity.getTwinClassId(), linkChange.getLink().getId());
                if (twinClassFieldEntity != null) {
                    historyEntity.setTwinClassFieldId(twinClassFieldEntity.getId());
                    context.setField(FieldSnapshot.convertEntity(twinClassFieldEntity, i18nService));
                }
            }
        }
        if (authService.getApiUser() != null)
            historyEntity.setHistoryBatchId(authService.getApiUser().getRequestId());
        HistoryTypeConfig typeConfig = detectConfig(historyEntity);
        if (typeConfig.getStatus().isDisabled()) //we check status only on write. it will be difficult to check it on read, cause of pagination
            return;
        fillSnapshotMessage(historyEntity, typeConfig);
    }

    private UserEntity getActor() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UserEntity actor;
        if (apiUser != null && apiUser.isUserSpecified())
            actor = apiUser.getUser();
        else
            actor = null; //todo we can have changes not from users but from some system schedulers
        return actor;
    }

    public void fillSnapshotMessage(HistoryEntity historyEntity, HistoryTypeConfig typeConfig) throws ServiceException {
        String snapshotTemplate = typeConfig.getSnapshotMessageTemplate();
        if (StringUtils.isEmpty(snapshotTemplate)) {
            historyEntity.setSnapshotMessage("History message template is not configured");
            return;
        }
        historyEntity.setSnapshotMessage(createMessage(snapshotTemplate, historyEntity, true));
    }

    public String createMessage(String template, HistoryEntity historyEntity, boolean useSnapshots) {
        Map<String, String> templateVars = prepareCommonTemplateVars(template, historyEntity);
        if (useSnapshots && historyEntity.getContext() != null)
            templateVars.putAll(historyEntity.getContext().getTemplateVars());
        return org.cambium.common.util.StringUtils.replaceVariables(template, templateVars);
    }

    public Map<String, String> prepareCommonTemplateVars(String template, HistoryEntity historyEntity) {
        Map<String, String> templateVars = new HashMap<>();
        if (historyEntity.getTwin().getTwinClass() != null) {
            templateVars.put("twin.class.key", historyEntity.getTwin().getTwinClass().getKey());
            if (template.indexOf("twin.class.name") > 0)
                templateVars.put("twin.class.name", i18nService.translateToLocale(historyEntity.getTwin().getTwinClass().getNameI18NId()));
        }
        return templateVars;
    }

    private HistoryTypeConfig detectConfig(HistoryEntity historyEntity) throws ServiceException {
        HistoryTypeConfig config = new HistoryTypeConfig();
        ApiUser apiUser = authService.getApiUser();
        HistoryTypeConfigLevel configLevel;
        configLevel = historyTypeRepository.findConfig(historyEntity.getHistoryType());
        updateConfig(config, configLevel);
        configLevel = historyTypeConfigDomainRepository.findConfig(historyEntity.getHistoryType(), apiUser.getDomain().getId());
        updateConfig(config, configLevel);
        if (config.getStatus().isBlocker())
            return config;
        configLevel = historyTypeConfigTwinClassRepository.findConfig(historyEntity.getHistoryType(), historyEntity.getTwin().getTwinClassId());
        updateConfig(config, configLevel);
        if (config.getStatus().isBlocker() || historyEntity.getTwinClassFieldId() == null)
            return config;
        configLevel = historyTypeConfigTwinClassFieldRepository.findConfig(historyEntity.getHistoryType(), historyEntity.getTwinClassFieldId());
        updateConfig(config, configLevel);
        return config;
    }

    public static void updateConfig(HistoryTypeConfig config, HistoryTypeConfigLevel lowConfigLevel) {
        if (config.getStatus().isBlocker() || lowConfigLevel == null)
            return;
        config.setStatus(lowConfigLevel.getStatus());
        if (StringUtils.isNotEmpty(lowConfigLevel.getSnapshotMessageTemplate()))
            config.setSnapshotMessageTemplate(lowConfigLevel.getSnapshotMessageTemplate());
        if (lowConfigLevel.getMessageTemplateI18nId() != null)
            config.setMessageTemplateI18nId(lowConfigLevel.getMessageTemplateI18nId());
    }

    public void loadFreshestMessageDraft(HistoryEntity historyEntity) throws ServiceException {
        HistoryTypeConfig config = detectConfig(historyEntity);
        String freshMessageDraft = historyEntity.getSnapshotMessage();;
        if (config.getMessageTemplateI18nId() != null) {
            String messageTemplate = i18nService.translateToLocale(config.getMessageTemplateI18nId());
            if (StringUtils.isNotEmpty(messageTemplate))
                freshMessageDraft = createMessage(messageTemplate, historyEntity, false); //we want to use fresh data, not from snapshot
        }
        historyEntity.setFreshMessage(freshMessageDraft);
    }

    public HistoryItem<HistoryContextUserChange> assigneeChanged(UserEntity fromUser, UserEntity toUser) {
        return new HistoryItem<>(HistoryType.assigneeChanged, new HistoryContextUserChange()
                .shotFromUser(fromUser)
                .shotToUser(toUser));
    }

    public HistoryItem<HistoryContextAttachment> attachmentCreate(TwinAttachmentEntity attachmentEntity) {
        return new HistoryItem<>(HistoryType.attachmentCreate, new HistoryContextAttachment()
                .shotAttachment(attachmentEntity));
    }

    public HistoryItem<HistoryContextAttachmentChange> attachmentUpdate(TwinAttachmentEntity attachmentEntity) {
        HistoryContextAttachmentChange context = new HistoryContextAttachmentChange();
        context.shotAttachment(attachmentEntity);
        return new HistoryItem<>(HistoryType.attachmentUpdate, context);
    }

    public HistoryItem<HistoryContextAttachment> attachmentDelete(TwinAttachmentEntity attachmentEntity) {
        return new HistoryItem<>(HistoryType.attachmentDelete, new HistoryContextAttachment()
                .shotAttachment(attachmentEntity));
    }

    public HistoryItem<HistoryContextTwinChange> headChanged(TwinEntity fromTwin, TwinEntity toTwin) {
        return new HistoryItem<>(HistoryType.headChanged, new HistoryContextTwinChange()
                .shotFromTwin(fromTwin)
                .shotToTwin(toTwin));
    }

    public HistoryItem<HistoryContextStringChange> nameChanged(String fromValue, String toValue) {
        return new HistoryItem<>(HistoryType.nameChanged, new HistoryContextStringChange()
                .setFromValue(fromValue)
                .setToValue(toValue));
    }

    public HistoryItem<HistoryContextStringChange> descriptionChanged(String fromValue, String toValue) {
        return new HistoryItem<>(HistoryType.descriptionChanged, new HistoryContextStringChange()
                .setFromValue(fromValue)
                .setToValue(toValue));
    }

    public HistoryItem<HistoryContextStatusChange> statusChanged(TwinStatusEntity fromStatus, TwinStatusEntity toStatus) {
        return new HistoryItem<>(HistoryType.statusChanged, new HistoryContextStatusChange()
                .shotFromStatus(fromStatus, i18nService)
                .shotToStatus(toStatus, i18nService));
    }

    public HistoryItem<HistoryContextStringChange> fieldChangeSimple(TwinClassFieldEntity twinClassFieldEntity, String fromValue, String toValue) {
        HistoryContextStringChange context = new HistoryContextStringChange()
                .setFromValue(fromValue)
                .setToValue(toValue);
        context.shotField(twinClassFieldEntity, i18nService);
        return new HistoryItem<>(HistoryType.fieldChanged, context);
    }

    public HistoryItem<HistoryContextUserChange> fieldChangeUser(TwinClassFieldEntity twinClassFieldEntity, UserEntity fromUser, UserEntity toUser) {
        HistoryContextUserChange context = new HistoryContextUserChange()
                .shotFromUser(fromUser)
                .shotToUser(toUser);
        context.shotField(twinClassFieldEntity, i18nService);
        return new HistoryItem<>(HistoryType.fieldChanged, context);
    }

    public HistoryItem<HistoryContextUserMultiChange> fieldChangeUserMulti(TwinClassFieldEntity twinClassFieldEntity) {
        HistoryContextUserMultiChange context = new HistoryContextUserMultiChange();
        context.shotField(twinClassFieldEntity, i18nService);
        return new HistoryItem<>(HistoryType.fieldChanged, context);
    }

    public HistoryItem<HistoryContextDatalistChange> fieldChangeDataList(TwinClassFieldEntity twinClassFieldEntity, DataListOptionEntity fromDataListOption, DataListOptionEntity toDataListOption) {
        HistoryContextDatalistChange context = new HistoryContextDatalistChange()
                .shotFromDataListOption(fromDataListOption, i18nService)
                .shotToDataListOption(toDataListOption, i18nService);
        context.shotField(twinClassFieldEntity, i18nService);
        return new HistoryItem<>(HistoryType.fieldChanged, context);
    }

    public HistoryItem<HistoryContextDatalistMultiChange> fieldChangeDataListMulti(TwinClassFieldEntity twinClassFieldEntity) {
        HistoryContextDatalistMultiChange context = new HistoryContextDatalistMultiChange();
        context.shotField(twinClassFieldEntity, i18nService);
        return new HistoryItem<>(HistoryType.fieldChanged, context);
    }

    public HistoryItem<HistoryContextLink> linkCreated(UUID twinLinkId, LinkEntity linkEntity, TwinEntity dstTwinEntity, boolean forward) {
        HistoryContextLink context = new HistoryContextLink()
                .setTwinLinkId(twinLinkId)
                .shotLink(linkEntity, forward, i18nService)
                .shotDstTwin(dstTwinEntity);
        return new HistoryItem<>(HistoryType.linkCreated, context);
    }

    public HistoryItem<HistoryContextLink> linkDeleted(UUID twinLinkId, LinkEntity linkEntity, TwinEntity dstTwinEntity, boolean forward) {
        HistoryContextLink context = new HistoryContextLink()
                .setTwinLinkId(twinLinkId)
                .shotLink(linkEntity, forward, i18nService)
                .shotDstTwin(dstTwinEntity);
        return new HistoryItem<>(HistoryType.linkDeleted, context);
    }

    public HistoryItem<HistoryContextLinkChange> linkChange(UUID twinLinkId, LinkEntity linkEntity, TwinEntity fromTwinEntity, TwinEntity toTwinEntity) {
        HistoryContextLinkChange context = new HistoryContextLinkChange()
                .setTwinLinkId(twinLinkId)
                .shotLink(linkEntity, true, i18nService);
        context
                .shotFromTwin(fromTwinEntity)
                .shotToTwin(toTwinEntity);
        return new HistoryItem<>(HistoryType.linkUpdated, context);
    }

    /**
     * 2 change records will be stored. One for each twin (src and dst)
     */
    public HistoryCollectorMultiTwin linkCreated(TwinLinkEntity twinLinkEntity) {
        HistoryCollectorMultiTwin ret = new HistoryCollectorMultiTwin();
        ret.forTwin(twinLinkEntity.getSrcTwin())
                .add(linkCreated(twinLinkEntity.getId(), twinLinkEntity.getLink(), twinLinkEntity.getDstTwin(), true));
        ret.forTwin(twinLinkEntity.getDstTwin())
                .add(linkCreated(twinLinkEntity.getId(), twinLinkEntity.getLink(), twinLinkEntity.getSrcTwin(), false));
        return ret;
    }

    /**
     * 2 change records will be stored. One for each twin (src and dst)
     */
    public HistoryCollectorMultiTwin fieldLinkCreated(TwinClassFieldEntity fieldEntity, TwinLinkEntity twinLinkEntity) {
        HistoryCollectorMultiTwin ret = new HistoryCollectorMultiTwin();
        ret.forTwin(twinLinkEntity.getSrcTwin())
                .add(linkCreated(twinLinkEntity.getId(), twinLinkEntity.getLink(), twinLinkEntity.getDstTwin(), true));
        ret.forTwin(twinLinkEntity.getDstTwin())
                .add(linkCreated(twinLinkEntity.getId(), twinLinkEntity.getLink(), twinLinkEntity.getSrcTwin(), false));
        return ret;
    }

    /**
     * 2 change records will be stored.
     * - one for src twin
     * - one for dst twin
     */
    public HistoryCollectorMultiTwin linkDeleted(TwinLinkEntity twinLinkEntity) {
        HistoryCollectorMultiTwin ret = new HistoryCollectorMultiTwin();
        ret.forTwin(twinLinkEntity.getSrcTwin())
                .add(linkDeleted(twinLinkEntity.getId(), twinLinkEntity.getLink(), twinLinkEntity.getDstTwin(), true));
        ret.forTwin(twinLinkEntity.getDstTwin())
                .add(linkDeleted(twinLinkEntity.getId(), twinLinkEntity.getLink(), twinLinkEntity.getSrcTwin(), false));
        return ret;
    }

    /**
     * 3 change records will be stored.
     * - one for src twin
     * - one for old dst twin
     * - one for new dst twin
     */
    public HistoryCollectorMultiTwin linkUpdated(TwinLinkEntity twinLinkEntity, TwinEntity unlinkedTwinEntity, boolean forward) {
        HistoryCollectorMultiTwin ret = new HistoryCollectorMultiTwin();
        if (forward) { //dstTwinChanged
            ret.forTwin(twinLinkEntity.getSrcTwin())
                    .add(linkChange(twinLinkEntity.getId(), twinLinkEntity.getLink(), unlinkedTwinEntity, twinLinkEntity.getDstTwin()));
            ret.forTwin(twinLinkEntity.getDstTwin())
                    .add(linkCreated(twinLinkEntity.getId(), twinLinkEntity.getLink(), twinLinkEntity.getSrcTwin(), false));
            ret.forTwin(unlinkedTwinEntity)
                    .add(linkDeleted(twinLinkEntity.getId(), twinLinkEntity.getLink(), twinLinkEntity.getSrcTwin(), false));
        } else { // srcTwin changed
            ret.forTwin(twinLinkEntity.getDstTwin())
                    .add(linkChange(twinLinkEntity.getId(), twinLinkEntity.getLink(), unlinkedTwinEntity, twinLinkEntity.getSrcTwin()));
            ret.forTwin(twinLinkEntity.getSrcTwin())
                    .add(linkCreated(twinLinkEntity.getId(), twinLinkEntity.getLink(), twinLinkEntity.getDstTwin(), true));
            ret.forTwin(unlinkedTwinEntity)
                    .add(linkDeleted(twinLinkEntity.getId(), twinLinkEntity.getLink(), twinLinkEntity.getDstTwin(), true));
        }
        return ret;
    }
}
