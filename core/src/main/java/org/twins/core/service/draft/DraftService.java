package org.twins.core.service.draft;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LTreeUtils;
import org.cambium.common.util.MapUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.CUD;
import org.twins.core.dao.draft.*;
import org.twins.core.dao.twin.*;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryResultUncommited;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinDelete;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.factory.TwinFactoryService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinflow.TwinflowService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class DraftService {
    private final DraftRepository draftRepository;
    private final DraftTwinTagRepository draftTwinTagRepository;
    private final DraftTwinMarkerRepository draftTwinMarkerRepository;
    private final DraftTwinEraseRepository draftTwinEraseRepository;
    private final DraftTwinAttachmentRepository draftTwinAttachmentRepository;
    private final DraftTwinLinkRepository draftTwinLinkRepository;
    private final DraftTwinFieldSimpleRepository draftTwinFieldSimpleRepository;
    private final DraftTwinFieldUserRepository draftTwinFieldUserRepository;
    private final DraftTwinFieldDataListRepository draftTwinFieldDataListRepository;
    private final DraftTwinPersistRepository draftTwinPersistRepository;
    private final EntitySmartService entitySmartService;
    @Lazy
    private final TwinflowService twinflowService;
    @Lazy
    private final AuthService authService;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinFactoryService twinFactoryService;

    public void add(DraftTwinEraseEntity draftTwinEraseEntity) {
        draftTwinEraseRepository.save(draftTwinEraseEntity);
    }

    public void add(DraftEntity draftEntity, List<TwinDelete> deletes, TwinEntity reasonTwin) throws ServiceException {
        twinflowService.loadTwinflow(deletes.stream().map(TwinDelete::getTwinEntity).toList()); //bulk load
        for (TwinDelete twinDelete : deletes) {

        }
    }

    public DraftCollector createDraftCollector() throws ServiceException {
        return new DraftCollector(
                new DraftEntity()
                        .setId(UUID.randomUUID())
                        .setCreatedAt(Timestamp.from(Instant.now()))
                        .setCreatedByUser(authService.getApiUser().getUser())
                        .setCreatedByUserId(authService.getApiUser().getUserId())
                        .setStatus(DraftEntity.Status.UNCOMMITED)
        );
    }

    public DraftEntity draftErase(TwinEntity twinEntity) throws ServiceException {
        DraftCollector draftCollector = createDraftCollector();
        twinflowService.loadTwinflow(twinEntity);
        draftErase(draftCollector, twinEntity, twinEntity, DraftTwinEraseEntity.Reason.TARGET);
        runEraseFactory(draftCollector, twinEntity);
        draftCollector.flush();
        /* scope is not fully loaded because:
        1. linked twins can also have children and links
        2. child twins can also have links
        scope will be loaded when all items will have eraseReady = true
        */
        List<DraftTwinEraseEntity> erseNotReadyList = draftTwinEraseRepository.findByDraftIdAndEraseReadyFalse(draftCollector.getDraftEntity().getId());
        int cascadeDepth = 0;
        while (CollectionUtils.isNotEmpty(erseNotReadyList)) {
            cascadeDepth++;
            if (cascadeDepth >= 5)
                throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_CASCADE_ERASE_LIMIT);
            twinflowService.loadTwinflow(erseNotReadyList.stream().map(DraftTwinEraseEntity::getTwin).toList()); //bulk detect
            for (DraftTwinEraseEntity eraseItem : erseNotReadyList) {
                switch (eraseItem.getReason()) { // switch is more clear here
                    case TARGET:
                    case FACTORY:
                    case LINK:
                        // after running erase factory we can have updated child twins with new heads (not current twin)
                        // so we should exclude them from deletion
                        draftTwinEraseRepository.addChildTwins(draftCollector.getDraftEntity().getId(), eraseItem.getTwinId(), LTreeUtils.matchInTheMiddle(eraseItem.getTwinId()));
                        // after running erase factory we can have some link updates (dst twin was changed from current twin)
                        // so we should exclude them from deletion
                        draftTwinEraseRepository.addLinked(draftCollector.getDraftEntity().getId(), eraseItem.getTwinId());
                        break;
                    case CHILD:
                        // we do not need to add child of child, because it's already done, so we will add only links
                        draftTwinEraseRepository.addLinked(draftCollector.getDraftEntity().getId(), eraseItem.getTwinId());
                        break;
                    default:
                        throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "something went wrong");
                }
                runEraseFactory(draftCollector, eraseItem.getTwin());
                draftCollector.flush();
                eraseItem
                        .setEraseReady(true)
                        .setEraseTwinStatusId(eraseItem.getTwin().getTwinflow().getEraseTwinStatusId());

            }
            draftTwinEraseRepository.saveAll(erseNotReadyList);
            erseNotReadyList = draftTwinEraseRepository.findByDraftIdAndEraseReadyFalse(draftCollector.getDraftEntity().getId());
        }
        return draftCollector.getDraftEntity();
    }

    public DraftCollector runEraseFactory(DraftCollector draftCollector, TwinEntity twinEntity) throws ServiceException {
        UUID eraseFactoryId = twinEntity.getTwinflow().getEraseTwinFactoryId();
        if (eraseFactoryId == null)
            return draftCollector;
        FactoryContext factoryContext = new FactoryContext()
                .addInputTwin(twinEntity);
        draftFactoryResult(draftCollector, eraseFactoryId, factoryContext, twinEntity);
        return draftCollector;
    }

    public DraftCollector draftFactoryResult(DraftCollector draftCollector, UUID factoryId, FactoryContext factoryContext, TwinEntity reasonTwin) throws ServiceException {
        if (factoryId == null)
            return draftCollector;
        FactoryResultUncommited factoryResultUncommited = twinFactoryService.runFactory(factoryId, factoryContext);
        if (!factoryResultUncommited.isCommittable()) // we will anyway create draft to show locked twin
            draftCollector.getDraftEntity().setStatus(DraftEntity.Status.LOCKED);
        if (CollectionUtils.isNotEmpty(factoryResultUncommited.getCreates()))
            for (TwinCreate twinCreate : factoryResultUncommited.getCreates())
                draftCreate(draftCollector, twinCreate);
        if (CollectionUtils.isNotEmpty(factoryResultUncommited.getUpdates()))
            for (TwinUpdate twinUpdate : factoryResultUncommited.getUpdates())
                draftUpdate(draftCollector, twinUpdate);
        if (CollectionUtils.isNotEmpty(factoryResultUncommited.getDeletes()))
            for (TwinDelete twinDelete : factoryResultUncommited.getDeletes())
                draftErase(draftCollector, twinDelete.getTwinEntity(), reasonTwin, DraftTwinEraseEntity.Reason.FACTORY);
        return draftCollector;
    }

    public DraftTwinEraseEntity draftErase(DraftCollector draftCollector, TwinEntity twinEntity, TwinEntity reasonTwin, DraftTwinEraseEntity.Reason reason) throws ServiceException {
        DraftTwinEraseEntity draftTwinEraseEntity = new DraftTwinEraseEntity()
                .setTwinId(twinEntity.getId())
                .setTwin(twinEntity)
                .setEraseReady(false)
                .setDraftId(draftCollector.getDraftEntity().getId())
                .setReasonTwinId(reasonTwin != null ? reasonTwin.getId() : null)
                .setReason(reason)
                .setEraseTwinStatusId(null); //we will fill it later
        draftCollector.add(draftTwinEraseEntity);
        return draftTwinEraseEntity;
    }

    public DraftCollector draftUpdate(DraftCollector draftCollector, TwinUpdate twinUpdate) throws ServiceException {
        draftCollector.add(createTwinPersistDraft(draftCollector.getDraftEntity(), twinUpdate.getTwinEntity()));
        draftTagsUpdate(draftCollector, twinUpdate.getDbTwinEntity().getId(), twinUpdate.getTagsAddExisted(), twinUpdate.getTagsDelete());
        draftMarkersUpdate(draftCollector, twinUpdate.getDbTwinEntity().getId(), twinUpdate.getMarkersAdd(), twinUpdate.getMarkersDelete());
        if (MapUtils.isNotEmpty(twinUpdate.getFields())) {
            TwinChangesCollector twinChangesCollector = twinService.convertTwinFields(twinUpdate.getDbTwinEntity(), twinUpdate.getFields());
            draftFieldSimpleUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldSimpleEntity.class));
            draftFieldUserUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldUserEntity.class));
            draftFieldDataListUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldDataListEntity.class));
            draftLinkUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinLinkEntity.class));

            draftFieldUserDelete(draftCollector, twinChangesCollector.getDeleteIds(TwinFieldUserEntity.class));
            draftFieldDataListDelete(draftCollector, twinChangesCollector.getDeleteIds(TwinFieldDataListEntity.class));
            draftLinkDelete(draftCollector, twinChangesCollector.getDeleteIds(TwinLinkEntity.class));
        }
        if (twinUpdate.getTwinLinkCUD() != null) {
            draftLinkUpdate(draftCollector, twinUpdate.getTwinLinkCUD().getUpdateList());
            draftLinkUpdate(draftCollector, twinUpdate.getTwinLinkCUD().getCreateList());
            draftLinkDelete(draftCollector, twinUpdate.getTwinLinkCUD().getDeleteUUIDList());
        }
        if (twinUpdate.getAttachmentCUD() != null) {
            draftAttachmentUpdate(draftCollector, twinUpdate.getAttachmentCUD().getUpdateList());
            draftAttachmentUpdate(draftCollector, twinUpdate.getAttachmentCUD().getCreateList());
            draftLinkDelete(draftCollector, twinUpdate.getTwinLinkCUD().getDeleteUUIDList());
        }
        return draftCollector;
    }

    public DraftCollector draftCreate(DraftCollector draftCollector, TwinCreate twinCreate) throws ServiceException {
        draftCollector.add(createTwinPersistDraft(draftCollector.getDraftEntity(), twinCreate.getTwinEntity()));
        draftTagsUpdate(draftCollector, twinCreate.getTwinEntity().getId(), twinCreate.getTagsAddExisted(), null);
        draftMarkersUpdate(draftCollector, twinCreate.getTwinEntity().getId(), twinCreate.getMarkersAdd(), null);
        if (MapUtils.isNotEmpty(twinCreate.getFields())) {
            TwinChangesCollector twinChangesCollector = twinService.convertTwinFields(twinCreate.getTwinEntity(), twinCreate.getFields());
            draftFieldSimpleUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldSimpleEntity.class));
            draftFieldUserUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldUserEntity.class));
            draftFieldDataListUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldDataListEntity.class));
            draftLinkUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinLinkEntity.class));
        }
        if (twinCreate.getLinksEntityList() != null) {
            draftLinkUpdate(draftCollector, twinCreate.getLinksEntityList());
        }
        if (twinCreate.getAttachmentEntityList() != null) {
            draftAttachmentUpdate(draftCollector, twinCreate.getAttachmentEntityList());
        }
        return draftCollector;
    }

    public void draftFieldSimpleUpdate(DraftCollector draftCollector, Collection<TwinFieldSimpleEntity> fieldSimpleEntities) throws ServiceException {
        for (TwinFieldSimpleEntity twinFieldSimpleEntity : fieldSimpleEntities)
            draftCollector.add(createFieldDraft(draftCollector.getDraftEntity(), twinFieldSimpleEntity));
    }

    public void draftFieldUserUpdate(DraftCollector draftCollector, Collection<TwinFieldUserEntity> fieldUserEntities) throws ServiceException {
        for (TwinFieldUserEntity twinFieldUserEntity : fieldUserEntities)
            draftCollector.add(createFieldDraft(draftCollector.getDraftEntity(), twinFieldUserEntity));
    }

    public void draftFieldDataListUpdate(DraftCollector draftCollector, Collection<TwinFieldDataListEntity> fieldDataListEntities) throws ServiceException {
        for (TwinFieldDataListEntity twinFieldDataListEntity : fieldDataListEntities)
            draftCollector.add(createFieldDraft(draftCollector.getDraftEntity(), twinFieldDataListEntity));
    }

    public void draftLinkUpdate(DraftCollector draftCollector, Collection<TwinLinkEntity> twinLinkEntities) throws ServiceException {
        for (TwinLinkEntity twinLinkEntity : twinLinkEntities)
            draftCollector.add(createTwinLinkDraft(draftCollector.getDraftEntity(), twinLinkEntity));
    }

    public void draftAttachmentUpdate(DraftCollector draftCollector, Collection<TwinAttachmentEntity> twinAttachmentEntities) throws ServiceException {
        for (TwinAttachmentEntity twinLinkEntity : twinAttachmentEntities)
            draftCollector.add(createTwinAttachmentDraft(draftCollector.getDraftEntity(), twinLinkEntity));
    }

    public void draftFieldUserDelete(DraftCollector draftCollector, Set<UUID> fieldUserIds) throws ServiceException {
        for (UUID twinFieldUserId : fieldUserIds)
            draftCollector.add(createFieldUserDeleteDraft(draftCollector.getDraftEntity(), twinFieldUserId));
    }

    public void draftFieldDataListDelete(DraftCollector draftCollector, Set<UUID> fieldDataListIds) throws ServiceException {
        for (UUID twinFieldDataListId : fieldDataListIds)
            draftCollector.add(createFieldDataListDeleteDraft(draftCollector.getDraftEntity(), twinFieldDataListId));
    }

    public void draftLinkDelete(DraftCollector draftCollector, Set<UUID> twinLinkIds) throws ServiceException {
        for (UUID twinLinkId : twinLinkIds)
            draftCollector.add(createLinkDeleteDraft(draftCollector.getDraftEntity(), twinLinkId));
    }

    public void draftTagsUpdate(DraftCollector draftCollector, UUID twinId, Set<UUID> tagsAddExisted, Set<UUID> tagsDelete) throws ServiceException {
        if (CollectionUtils.isNotEmpty(tagsAddExisted)) {
            for (UUID tagId : tagsAddExisted) {
                draftCollector.add(
                        createTagDraft(draftCollector.getDraftEntity(), twinId, tagId, true));
            }
        }
        if (CollectionUtils.isNotEmpty(tagsDelete)) {
            for (UUID tagId : tagsDelete) {
                draftCollector.add(
                        createTagDraft(draftCollector.getDraftEntity(), twinId, tagId, false));
            }
        }
    }

    public void draftMarkersUpdate(DraftCollector draftCollector, UUID twinId, Set<UUID> markersAdd, Set<UUID> markersDelete) throws ServiceException {
        if (CollectionUtils.isNotEmpty(markersAdd))
            for (UUID markerId : markersAdd)
                draftCollector.add(
                        createMarkerDraft(draftCollector.getDraftEntity(), twinId, markerId, true));

        if (CollectionUtils.isNotEmpty(markersDelete))
            for (UUID markerId : markersDelete)
                draftCollector.add(
                        createMarkerDraft(draftCollector.getDraftEntity(), twinId, markerId, false));
    }

    public DraftTwinPersistEntity createTwinPersistDraft(DraftEntity draftEntity, TwinEntity twinEntity) throws ServiceException {
        return new DraftTwinPersistEntity()
                .setTwinId(twinEntity.getId())
                .setDescription(twinEntity.getDescription())
                .setName(twinEntity.getName())
                .setAssignerUserId(twinEntity.getAssignerUserId())
                .setCreatedByUserId(twinEntity.getCreatedByUserId())
                .setHeadTwinId(twinEntity.getHeadTwinId())
                .setExternalId(twinEntity.getExternalId())
                .setViewPermissionId(twinEntity.getViewPermissionId())
//                .setTwinClassId()
//                .setOwnerUserId()
//                .setOwnerBusinessAccountId()
                .setCreateElseUpdate(false)
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId());
    }

    public DraftTwinEraseEntity createTwinEraseDraft(DraftEntity draftEntity, TwinEntity twinEntity, TwinEntity reasonTwin, DraftTwinEraseEntity.Reason reason) throws ServiceException {
        twinflowService.loadTwinflow(twinEntity);
        return new DraftTwinEraseEntity()
                .setTwinId(twinEntity.getId())
                .setTwin(twinEntity)
                .setEraseReady(false)
                .setDraftId(draftEntity.getId())
                .setReasonTwinId(reasonTwin.getId())
                .setReason(reason)
                .setEraseTwinStatusId(twinEntity.getTwinflow().getEraseTwinStatusId());
    }

    public DraftTwinTagEntity createTagDraft(DraftEntity draftEntity, UUID twinId, UUID tagId, boolean createElseDelete) throws ServiceException {
        return new DraftTwinTagEntity()
                .setTwinId(twinId)
                .setTagDataListOptionId(tagId)
                .setCreateElseDelete(createElseDelete)
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId());
    }

    public DraftTwinMarkerEntity createMarkerDraft(DraftEntity draftEntity, UUID twinId, UUID markerId, boolean createElseDelete) throws ServiceException {
        return new DraftTwinMarkerEntity()
                .setTwinId(twinId)
                .setMarkerDataListOptionId(markerId)
                .setCreateElseDelete(createElseDelete)
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId());
    }

    public DraftTwinFieldSimpleEntity createFieldDraft(DraftEntity draftEntity, TwinFieldSimpleEntity twinFieldSimpleEntity) throws ServiceException {
        CUD cud = twinFieldSimpleEntity.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinFieldSimpleEntity draftTwinFieldSimpleEntity = new DraftTwinFieldSimpleEntity()
                .setTwinId(twinFieldSimpleEntity.getTwinId()) // can we guarantee that it's not null?
                .setCud(cud)
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId());
        switch (cud) {
            case CREATE:
                if (twinFieldSimpleEntity.getTwinClassFieldId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field is required for new field creation");
                draftTwinFieldSimpleEntity
                        .setTwinClassFieldId(twinFieldSimpleEntity.getTwinClassFieldId())
                        .setValue(twinFieldSimpleEntity.getValue());
                break;
            case UPDATE:
                if (twinFieldSimpleEntity.getId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field id required for field update");
                draftTwinFieldSimpleEntity
                        .setTwinFieldSimpleId(twinFieldSimpleEntity.getId())
                        .setValue(twinFieldSimpleEntity.getValue());
                break;
        }
        return draftTwinFieldSimpleEntity;
    }

    public DraftTwinFieldUserEntity createFieldDraft(DraftEntity draftEntity, TwinFieldUserEntity twinFieldUserEntity) throws ServiceException {
        CUD cud = twinFieldUserEntity.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinFieldUserEntity draftTwinFieldUserEntity = new DraftTwinFieldUserEntity()
                .setTwinId(twinFieldUserEntity.getTwinId())
                .setCud(cud)
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId());
        switch (cud) {
            case CREATE:
                if (twinFieldUserEntity.getTwinClassFieldId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field is required for new field creation");
                draftTwinFieldUserEntity
                        .setTwinClassFieldId(twinFieldUserEntity.getTwinClassFieldId())
                        .setUserId(twinFieldUserEntity.getUserId());
                break;
            case UPDATE:
                if (twinFieldUserEntity.getId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_class_field.id required for field update");
                draftTwinFieldUserEntity
                        .setTwinFieldUserId(twinFieldUserEntity.getId())
                        .setUserId(twinFieldUserEntity.getUserId());
                break;
        }
        return draftTwinFieldUserEntity;
    }


    public DraftTwinLinkEntity createTwinLinkDraft(DraftEntity draftEntity, TwinLinkEntity twinLinkEntity) throws ServiceException {
        CUD cud = twinLinkEntity.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinLinkEntity draftTwinLinkEntity = new DraftTwinLinkEntity()
                .setCud(cud)
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId());
        switch (cud) {
            case CREATE:
                if (twinLinkEntity.getLinkId() == null || twinLinkEntity.getSrcTwinId() == null || twinLinkEntity.getDstTwinId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "incorrect link");
                draftTwinLinkEntity
                        .setLinkId(twinLinkEntity.getLinkId())
                        .setSrcTwinId(twinLinkEntity.getSrcTwinId())
                        .setDstTwinId(twinLinkEntity.getDstTwinId());
                break;
            case UPDATE:
                if (twinLinkEntity.getId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_link.id required for link update");
                draftTwinLinkEntity
                        .setTwinLinkId(twinLinkEntity.getId())
                        .setLinkId(twinLinkEntity.getLinkId())
                        .setSrcTwinId(twinLinkEntity.getSrcTwinId())
                        .setDstTwinId(twinLinkEntity.getDstTwinId());
                break;
        }
        return draftTwinLinkEntity;
    }

    public DraftTwinAttachmentEntity createTwinAttachmentDraft(DraftEntity draftEntity, TwinAttachmentEntity twinAttachmentEntity) throws ServiceException {
        CUD cud = twinAttachmentEntity.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinAttachmentEntity draftTwinAttachmentEntity = new DraftTwinAttachmentEntity()
                .setTwinId(twinAttachmentEntity.getTwinId()) // not sure that we need it
                .setCud(cud)
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setExternalId(twinAttachmentEntity.getExternalId())
                .setTitle(twinAttachmentEntity.getTitle())
                .setDescription(twinAttachmentEntity.getDescription())
                .setStorageLink(twinAttachmentEntity.getStorageLink())
                .setViewPermissionId(twinAttachmentEntity.getViewPermissionId())
                .setTwinClassFieldId(twinAttachmentEntity.getTwinClassFieldId())
                .setTwinCommentId(twinAttachmentEntity.getTwinCommentId())
                .setTwinflowTransitionId(twinAttachmentEntity.getTwinflowTransitionId());
        if (cud == CUD.UPDATE) {
            if (twinAttachmentEntity.getId() == null)
                throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_attachment.id required for link update");
            draftTwinAttachmentEntity.setTwinAttachmentId(twinAttachmentEntity.getId());
        }
        return draftTwinAttachmentEntity;
    }

    public DraftTwinFieldUserEntity createFieldUserDeleteDraft(DraftEntity draftEntity, UUID twinFieldUserId) throws ServiceException {
        if (twinFieldUserId == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_field_user.id required for field deletion");
        return new DraftTwinFieldUserEntity()
                .setCud(CUD.DELETE)
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTwinFieldUserId(twinFieldUserId);
    }

    public DraftTwinFieldDataListEntity createFieldDataListDeleteDraft(DraftEntity draftEntity, UUID twinFieldDataListId) throws ServiceException {
        if (twinFieldDataListId == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_field_data_list.id required for field deletion");
        return new DraftTwinFieldDataListEntity()
                .setCud(CUD.DELETE)
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTwinFieldDataListId(twinFieldDataListId);
    }

    public DraftTwinLinkEntity createLinkDeleteDraft(DraftEntity draftEntity, UUID twinLinkId) throws ServiceException {
        if (twinLinkId == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_link.id required for link  deletion");
        return new DraftTwinLinkEntity()
                .setCud(CUD.DELETE)
                // .setSrcTwinId() we don't need it here?
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTwinLinkId(twinLinkId);
    }

    public DraftTwinFieldDataListEntity createFieldDraft(DraftEntity draftEntity, TwinFieldDataListEntity twinFieldDataListEntity) throws ServiceException {
        CUD cud = twinFieldDataListEntity.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinFieldDataListEntity draftTwinFieldDataListEntity = new DraftTwinFieldDataListEntity()
                .setTwinId(twinFieldDataListEntity.getTwinId())
                .setCud(cud)
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId());
        switch (cud) {
            case CREATE:
                if (twinFieldDataListEntity.getTwinClassFieldId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field is required for new field creation");
                draftTwinFieldDataListEntity
                        .setTwinClassFieldId(twinFieldDataListEntity.getTwinClassFieldId())
                        .setDataListOptionId(twinFieldDataListEntity.getDataListOptionId());
                break;
            case UPDATE:
                if (twinFieldDataListEntity.getId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field id required for field update");
                draftTwinFieldDataListEntity
                        .setTwinFieldDataListId(twinFieldDataListEntity.getId())
                        .setDataListOptionId(twinFieldDataListEntity.getDataListOptionId());
                break;
        }
        return draftTwinFieldDataListEntity;
    }

    protected void save(DraftCollector draftCollector) throws ServiceException {
        if (!draftCollector.hasChanges())
            return;
        entitySmartService.save(draftCollector.getDraftEntity(), draftRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        saveEntities(draftCollector, DraftTwinPersistEntity.class, draftTwinPersistRepository);
        saveEntities(draftCollector, DraftTwinEraseEntity.class, draftTwinEraseRepository);
        saveEntities(draftCollector, DraftTwinAttachmentEntity.class, draftTwinAttachmentRepository);
        saveEntities(draftCollector, DraftTwinLinkEntity.class, draftTwinLinkRepository);
        saveEntities(draftCollector, DraftTwinTagEntity.class, draftTwinTagRepository);
        saveEntities(draftCollector, DraftTwinMarkerEntity.class, draftTwinMarkerRepository);
        saveEntities(draftCollector, DraftTwinFieldSimpleEntity.class, draftTwinFieldSimpleRepository);
        saveEntities(draftCollector, DraftTwinFieldUserEntity.class, draftTwinFieldUserRepository);
        saveEntities(draftCollector, DraftTwinFieldDataListEntity.class, draftTwinFieldDataListRepository);
        if (!draftCollector.getSaveEntityMap().isEmpty())
            for (Map.Entry<Class<?>, Set<Object>> classChanges : draftCollector.getSaveEntityMap().entrySet()) {
                log.warn("Unsupported entity class[{}] for saving", classChanges.getKey().getSimpleName());
            }
    }

    private <T, K> void saveEntities(DraftCollector draftCollector, Class<T> entityClass, CrudRepository<T, K> repository) {
        Set<Object> entities = draftCollector.getSaveEntityMap().get(entityClass);
        if (entities != null) {
            entitySmartService.saveAllAndLogChanges((Map) entities, repository);
            draftCollector.getSaveEntityMap().remove(entityClass);
        }
    }
}
