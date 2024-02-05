package org.twins.core.service.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.i18n.service.I18nService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.history.HistoryRepository;
import org.twins.core.dao.history.HistoryType;
import org.twins.core.dao.history.HistoryTypeDomainTemplateRepository;
import org.twins.core.dao.history.context.*;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class HistoryService extends EntitySecureFindServiceImpl<HistoryEntity> {
    @Lazy
    final TwinService twinService;
    final HistoryRepository historyRepository;
    final HistoryTypeDomainTemplateRepository historyTypeDomainTemplateRepository;
    final AuthService authService;
    final I18nService i18nService;

    @Override
    public CrudRepository<HistoryEntity, UUID> entityRepository() {
        return historyRepository;
    }

    @Override
    public boolean isEntityReadDenied(HistoryEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(HistoryEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public List<HistoryEntity> findHistory(UUID twinId, int childDepth, Sort.Direction createdBySortDirection) throws ServiceException {
        UserEntity user = authService.getApiUser().getUser();
        List<HistoryEntity> list = null;
        if (childDepth == 0)
            list = historyRepository.findByTwinId(twinId, Sort.by(createdBySortDirection, HistoryEntity.Fields.createdAt));
        else //todo support different depth
            list = historyRepository.findByTwinIdIncludeFirstLevelChildren(twinId, Sort.by(createdBySortDirection, HistoryEntity.Fields.createdAt));
        return list;
    }

    public void saveHistory(TwinEntity twinEntity, HistoryType type, HistoryContext context) throws ServiceException {
        HistoryEntity historyEntity = createEntity(twinEntity, type, context, getActor());
        entitySmartService.save(historyEntity, historyRepository, EntitySmartService.SaveMode.saveAndLogOnException);
    }

    public HistoryEntity createEntity(TwinEntity twinEntity, HistoryType type, HistoryContext context, UserEntity actor) throws ServiceException {
        HistoryEntity historyEntity = new HistoryEntity()
                .setTwin(twinEntity)
                .setTwinId(twinEntity.getId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setActorUser(actor)
                .setActorUserId(actor.getId())
                .setHistoryType(type)
                .setContext(context);
        if (context instanceof HistoryContextFieldChange fieldChange && fieldChange.getField() != null)
            historyEntity.setTwinClassFieldId(fieldChange.getField().getId());
        else if (context instanceof HistoryContextLink linkChange) {

        }
        fillSnapshotMessage(historyEntity);
        return historyEntity;
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

    public void saveHistory(TwinEntity twinEntity, HistoryCollector historyCollector) throws ServiceException {
        if (historyCollector == null || CollectionUtils.isEmpty(historyCollector.getHistoryList()))
            return;
        List<HistoryEntity> historyEntityList = new ArrayList<>();
        UserEntity actor = getActor();
        for (Pair<HistoryType, HistoryContext> pair : historyCollector.getHistoryList()) {
            HistoryEntity historyEntity = createEntity(twinEntity, pair.getKey(), pair.getValue(), actor);
            historyEntityList.add(historyEntity);
        }
        if (CollectionUtils.isNotEmpty(historyEntityList))
            entitySmartService.saveAllAndLog(historyEntityList, historyRepository);
    }

    public void saveHistory(HistoryCollectorMultiTwin multiTwinHistoryCollector) throws ServiceException {
        if (MapUtils.isEmpty(multiTwinHistoryCollector.getMultiTwinHistory()))
            return;
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
        if (CollectionUtils.isNotEmpty(historyEntityList))
            entitySmartService.saveAllAndLog(historyEntityList, historyRepository);
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

    //todo cache it
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

    public HistoryItem<HistoryContextFieldSimpleChange> fieldChangeSimple(TwinClassFieldEntity twinClassFieldEntity, String fromValue, String toValue) {
        HistoryContextFieldSimpleChange context = new HistoryContextFieldSimpleChange()
                .setFromValue(fromValue)
                .setToValue(toValue);
        context.shotField(twinClassFieldEntity, i18nService);
        return new HistoryItem<>(HistoryType.fieldChanged, context);
    }

    public HistoryItem<HistoryContextFieldUserChange> fieldChangeUser(TwinClassFieldEntity twinClassFieldEntity, UserEntity fromUser, UserEntity toUser) {
        HistoryContextFieldUserChange context = new HistoryContextFieldUserChange()
                .shotFromUser(fromUser)
                .shotToUser(toUser);
        context.shotField(twinClassFieldEntity, i18nService);
        return new HistoryItem<>(HistoryType.fieldChanged, context);
    }

    public HistoryItem<HistoryContextFieldUserMultiChange> fieldChangeUserMulti(TwinClassFieldEntity twinClassFieldEntity) {
        HistoryContextFieldUserMultiChange context = new HistoryContextFieldUserMultiChange();
        context.shotField(twinClassFieldEntity, i18nService);
        return new HistoryItem<>(HistoryType.fieldChanged, context);
    }

    public HistoryItem<HistoryContextFieldDatalistChange> fieldChangeDataList(TwinClassFieldEntity twinClassFieldEntity, DataListOptionEntity fromDataListOption, DataListOptionEntity toDataListOption) {
        HistoryContextFieldDatalistChange context = new HistoryContextFieldDatalistChange()
                .shotFromDataListOption(fromDataListOption, i18nService)
                .shotToDataListOption(toDataListOption, i18nService);
        context.shotField(twinClassFieldEntity, i18nService);
        return new HistoryItem<>(HistoryType.fieldChanged, context);
    }

    public HistoryItem<HistoryContextFieldDatalistMultiChange> fieldChangeDataListMulti(TwinClassFieldEntity twinClassFieldEntity) {
        HistoryContextFieldDatalistMultiChange context = new HistoryContextFieldDatalistMultiChange();
        context.shotField(twinClassFieldEntity, i18nService);
        return new HistoryItem<>(HistoryType.fieldChanged, context);
    }

    public HistoryItem<HistoryContextLink> linkCreated(UUID twinLinkId, LinkEntity linkEntity, TwinEntity dstTwinEntity, boolean forward) {
        HistoryContextLink context = new HistoryContextLink()
                .setTwinLinkId(twinLinkId)
                .shotLink(linkEntity, forward, i18nService)
                .shotDstTwin(dstTwinEntity);
        return new HistoryItem<>(HistoryType.linkCreate, context);
    }

    public HistoryItem<HistoryContextLink> linkDeleted(UUID twinLinkId, LinkEntity linkEntity, TwinEntity dstTwinEntity, boolean forward) {
        HistoryContextLink context = new HistoryContextLink()
                .setTwinLinkId(twinLinkId)
                .shotLink(linkEntity, forward, i18nService)
                .shotDstTwin(dstTwinEntity);
        return new HistoryItem<>(HistoryType.linkDelete, context);
    }

    public HistoryItem<HistoryContextLinkChange> linkChange(UUID twinLinkId, LinkEntity linkEntity, TwinEntity fromTwinEntity, TwinEntity toTwinEntity) {
        HistoryContextLinkChange context = new HistoryContextLinkChange()
                .setTwinLinkId(twinLinkId)
                .shotLink(linkEntity, true, i18nService);
        context
                .shotFromTwin(fromTwinEntity)
                .shotToTwin(toTwinEntity);
        return new HistoryItem<>(HistoryType.linkUpdate, context);
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
