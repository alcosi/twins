package org.twins.core.service.history;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.history.HistoryRepository;
import org.twins.core.dao.history.HistoryTypeDomainTemplateRepository;
import org.twins.core.dao.history.context.*;
import org.twins.core.dao.history.context.snapshot.FieldSnapshot;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinActionService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Lazy
@RequiredArgsConstructor
public class HistoryService extends EntitySecureFindServiceImpl<HistoryEntity> {
    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    private final HistoryRepository historyRepository;
    private final HistoryTypeDomainTemplateRepository historyTypeDomainTemplateRepository;
    private final TwinActionService twinActionService;
    private final AuthService authService;
    private final I18nService i18nService;

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
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied=!entity.getTwin().getTwinClass().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(HistoryEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return !isEntityReadDenied(entity,EntitySmartService.ReadPermissionCheckMode.none);
    }

    public PaginationResult<HistoryEntity> findHistory(UUID twinId, int childDepth, SimplePagination pagination) throws ServiceException {
        twinActionService.checkAllowed(twinId, TwinAction.HISTORY_VIEW);
        Pageable pageable = PaginationUtils.pageableOffset(pagination);
        Page<HistoryEntity> historyList;
        if (childDepth == 0)
            historyList = historyRepository.findByTwinId(twinId, pageable);
        else //todo support different depth
            historyList = historyRepository.findByTwinIdIncludeFirstLevelChildren(twinId, pageable);
        return PaginationUtils.convertInPaginationResult(historyList, pagination);
    }

    public void saveHistory(TwinEntity twinEntity, HistoryType type, HistoryContext context) throws ServiceException {
        HistoryEntity historyEntity = createEntity(twinEntity, type, context, getActor());
        entitySmartService.save(historyEntity, historyRepository, EntitySmartService.SaveMode.saveAndLogOnException);
    }

    public void saveHistory(HistoryCollectorMultiTwin multiTwinHistoryCollector) throws ServiceException {
        List<HistoryEntity> historyEntityList = convertToEntities(multiTwinHistoryCollector);
        if (historyEntityList == null) return;
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
                HistoryEntity historyEntity = createEntity(twinHistory.getKey(), pair.getKey(), pair.getValue(), actor);
                historyEntityList.add(historyEntity);
            }
        }
        return historyEntityList;
    }

    public HistoryEntity createEntity(TwinEntity twinEntity, HistoryType type, HistoryContext context, UserEntity actor) throws ServiceException {
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
                        if (twinEntity.isCreateElseUpdate()) {
                            historyEntity.setHistoryType(HistoryType.fieldCreatedOnCreate);
                        } else {
                            historyEntity.setHistoryType(HistoryType.fieldChanged);
                        }
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
        fillSnapshotMessage(historyEntity);
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

    public void fillSnapshotMessage(HistoryEntity historyEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        String snapshotTemplate = getSnapshotMessageTemplate(historyEntity.getHistoryType(), apiUser.isDomainSpecified() ? apiUser.getDomain().getId() : null);
        if (StringUtils.isEmpty(snapshotTemplate)) {
            historyEntity.setSnapshotMessage("History message template is not configured");
            return;
        }
        Map<String, String> templateVars = prepareCommonTemplateVars(snapshotTemplate, historyEntity);
        if (historyEntity.getContext() != null)
            templateVars.putAll(historyEntity.getContext().getTemplateVars());
        historyEntity.setSnapshotMessage(org.cambium.common.util.StringUtils.replaceVariables(snapshotTemplate, templateVars));
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

    private String getSnapshotMessageTemplate(HistoryType historyType, UUID domainId) {
        String snapshotTemplate = null;
        if (domainId != null)
            snapshotTemplate = historyTypeDomainTemplateRepository.findSnapshotMessageTemplate(historyType, domainId);
        if (snapshotTemplate == null)
            snapshotTemplate = historyTypeDomainTemplateRepository.findSnapshotMessageTemplate(historyType.getId());
        return snapshotTemplate;
    }

    public String getChangeFreshestDescription(HistoryEntity historyEntity) {
        return historyEntity.getSnapshotMessage(); //todo
    }

    public HistoryItem<HistoryContextUserChange> assigneeChanged(UserEntity fromUser, UserEntity toUser) {
        return new HistoryItem<>(HistoryType.assigneeChanged, new HistoryContextUserChange()
                .shotFromUser(fromUser)
                .shotToUser(toUser));
    }

    public HistoryItem<HistoryContextAttachment> attachmentCreate(TwinAttachmentEntity attachmentEntity) {
        if (attachmentEntity.getTwin().isCreateElseUpdate()) {
            return new HistoryItem<>(HistoryType.attachmentCreateOnCreate, new HistoryContextAttachment()
                    .shotAttachment(attachmentEntity));
        } else {
            return new HistoryItem<>(HistoryType.attachmentCreate, new HistoryContextAttachment()
                    .shotAttachment(attachmentEntity));
        }
    }

    public HistoryItem<HistoryContextComment> commentCreate(org.twins.core.dao.comment.TwinCommentEntity commentEntity) {
        return new HistoryItem<>(HistoryType.commentCreate, new HistoryContextComment()
                .shotComment(commentEntity));
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

    public HistoryItem<HistoryContextStringChange> externalIdChanged(String fromValue, String toValue) {
        return new HistoryItem<>(HistoryType.externalIdChanged, new HistoryContextStringChange()
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

    public HistoryItem<HistoryContextStringChange> fieldChangeSimpleSecret(TwinClassFieldEntity twinClassFieldEntity, String fromValue) {
        HistoryContextStringChange context = new HistoryContextStringChange()
                .setFromValue(fromValue == null ? null : "***")
                .setToValue("***");
        context.shotField(twinClassFieldEntity, i18nService);
        return new HistoryItem<>(HistoryType.fieldChanged, context);
    }

    public HistoryItem<HistoryContextI18nChange> fieldChangeI18n(
            TwinClassFieldEntity twinClassFieldEntity,
            Locale fromLocale,
            String fromTranslation,
            Locale toLocale,
            String toTranslation
    ) {
        HistoryContextI18nChange context = new HistoryContextI18nChange()
                .shotFrom(fromLocale, fromTranslation)
                .shotTo(toLocale, toTranslation);

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

    public HistoryItem<HistoryContextTwinClassChange> fieldChangeTwinClass(TwinClassFieldEntity twinClassFieldEntity, TwinClassEntity fromClass, TwinClassEntity toClass) {
        HistoryContextTwinClassChange context = new HistoryContextTwinClassChange()
                .shotFromClass(fromClass)
                .shotToClass(toClass);
        context.shotField(twinClassFieldEntity, i18nService);

        return new HistoryItem<>(HistoryType.fieldChanged, context);
    }

    public HistoryItem<HistoryContextTwinClassMultiChange> fieldChangeTwinClassMulti(TwinClassFieldEntity twinClassFieldEntity) {
        HistoryContextTwinClassMultiChange context = new HistoryContextTwinClassMultiChange();
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
        if (dstTwinEntity.isCreateElseUpdate()) {
            return new HistoryItem<>(HistoryType.linkCreatedOnCreate, context);
        } else {
            return new HistoryItem<>(HistoryType.linkCreated, context);
        }
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

    public HistoryItem<HistoryContextSpaceRoleUserChange> spaceRoleUserAdd(TwinClassFieldEntity twinClassFieldEntity, UUID roleId, List<UUID> userIdList) {
        HistoryContextSpaceRoleUserChange context = new HistoryContextSpaceRoleUserChange()
                .setRoleId(roleId)
                .setTargetedUserIds(userIdList);
        context.shotField(twinClassFieldEntity, i18nService);
        return new HistoryItem<>(HistoryType.spaceRoleUserAdded, context);
    }

    public HistoryItem<HistoryContextSpaceRoleUserChange> spaceRoleUserDelete(TwinClassFieldEntity twinClassFieldEntity, UUID roleId, List<UUID> userIdList) {
        HistoryContextSpaceRoleUserChange context = new HistoryContextSpaceRoleUserChange()
                .setRoleId(roleId)
                .setTargetedUserIds(userIdList);
        context.shotField(twinClassFieldEntity, i18nService);
        return new HistoryItem<>(HistoryType.spaceRoleUserRemoved, context);
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

    public boolean existsByHistoryBatchIdAndHistoryType(UUID historyBatchId, HistoryType type) {
        return historyRepository.existsByHistoryBatchIdAndHistoryType(historyBatchId, type);
    }
}
