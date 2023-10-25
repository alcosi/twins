package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.*;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinflow.TwinflowService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinService extends EntitySecureFindServiceImpl<TwinEntity> {
    final TwinRepository twinRepository;
    final TwinFieldRepository twinFieldRepository;
    final TwinClassFieldRepository twinClassFieldRepository;
    final TwinBusinessAccountAliasRepository twinBusinessAccountAliasRepository;
    final TwinDomainAliasRepository twinDomainAliasRepository;
    final TwinClassFieldService twinClassFieldService;
    final EntityManager entityManager;
    final EntitySmartService entitySmartService;
    final TwinflowService twinflowService;
    final TwinClassService twinClassService;
    final FeaturerService featurerService;
    final AttachmentService attachmentService;
    @Lazy
    final TwinLinkService twinLinkService;
    @Lazy
    final AuthService authService;
    @Override
    public CrudRepository<TwinEntity, UUID> entityRepository() {
        return twinRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getTwinClass().getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + apiUser.getDomain().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        //todo check permission schema
        return false;
    }

    public List<TwinEntity> findTwins(BasicSearch basicSearch) {
        CriteriaQuery<TwinEntity> criteriaQuery = entityManager.getCriteriaBuilder().createQuery(TwinEntity.class);
        Root<TwinEntity> twin = criteriaQuery.from(TwinEntity.class);
        List<Predicate> predicate = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(basicSearch.getTwinClassIdList()))
            predicate.add(twin.get(TwinEntity.Fields.twinClassId).in(basicSearch.getTwinClassIdList()));
        if (CollectionUtils.isNotEmpty(basicSearch.getAssignerUserIdList()))
            predicate.add(twin.get(TwinEntity.Fields.assignerUserId).in(basicSearch.getAssignerUserIdList()));
        if (CollectionUtils.isNotEmpty(basicSearch.getCreatedByUserIdList()))
            predicate.add(twin.get(TwinEntity.Fields.createdByUserId).in(basicSearch.getCreatedByUserIdList()));
        if (CollectionUtils.isNotEmpty(basicSearch.getStatusIdList()))
            predicate.add(twin.get(TwinEntity.Fields.twinStatusId).in(basicSearch.getStatusIdList()));
        if (CollectionUtils.isNotEmpty(basicSearch.getHeaderTwinIdList()))
            predicate.add(twin.get(TwinEntity.Fields.headTwinId).in(basicSearch.getHeaderTwinIdList()));
        if (CollectionUtils.isNotEmpty(basicSearch.getOwnerUserIdList()))
            predicate.add(twin.get(TwinEntity.Fields.ownerUserId).in(basicSearch.getOwnerUserIdList()));
        if (CollectionUtils.isNotEmpty(basicSearch.getOwnerBusinessAccountIdList()))
            predicate.add(twin.get(TwinEntity.Fields.ownerBusinessAccountId).in(basicSearch.getOwnerBusinessAccountIdList()));
        criteriaQuery.where(predicate.stream().toArray(Predicate[]::new));
        Query query = entityManager.createQuery(criteriaQuery);
        List<TwinEntity> ret = query.getResultList();
        if (ret != null)
            return ret.stream().filter(t -> !isEntityReadDenied(t)).toList();
        return ret;
    }

    public List<TwinEntity> findTwinsByClassId(UUID twinClassId) {
        return findTwins(new BasicSearch().addTwinClassId(twinClassId));
    }

    public TwinEntity findTwinByAlias(ApiUser apiUser, String twinAlias) throws ServiceException {
        if (apiUser.getBusinessAccount() != null) {
            TwinBusinessAccountAliasEntity twinBusinessAccountAliasEntity = twinBusinessAccountAliasRepository.findByBusinessAccountIdAndAlias(apiUser.getBusinessAccount().getId(), twinAlias);
            if (twinBusinessAccountAliasEntity != null)
                return twinBusinessAccountAliasEntity.getTwin();
        }
        TwinDomainAliasEntity twinDomainAliasEntity = twinDomainAliasRepository.findByDomainIdAndAlias(apiUser.getDomain().getId(), twinAlias);
        if (twinDomainAliasEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_ALIAS_UNKNOWN, "unknown twin alias[" + twinAlias + "]");
        return twinDomainAliasEntity.getTwin();
    }

    public List<TwinFieldEntity> findTwinFields(UUID twinId) {
        return twinFieldRepository.findByTwinId(twinId);
    }

    public TwinFieldEntity findTwinField(UUID twinFieldId) throws ServiceException {
        return entitySmartService.findById(twinFieldId, twinFieldRepository, EntitySmartService.FindMode.ifEmptyThrows);
    }

    public TwinFieldEntity findTwinFieldIncludeMissing(UUID twinId, String fieldKey) throws ServiceException {
        TwinFieldEntity twinFieldEntity = twinFieldRepository.findByTwinIdAndTwinClassField_Key(twinId, fieldKey);
        if (twinFieldEntity != null)
            return twinFieldEntity;
        TwinEntity twinEntity = entitySmartService.findById(twinId, twinRepository, EntitySmartService.FindMode.ifEmptyThrows);
        TwinClassFieldEntity twinClassField = twinClassFieldService.findByTwinClassIdAndKeyIncludeParent(twinEntity.getTwinClassId(), fieldKey);
        if (twinClassField == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_UNKNOWN, "unknown fieldKey[" + fieldKey + "] for twin["
                    + twinId + "] of class[" + twinEntity.getTwinClass().getKey() + " : " + twinEntity.getTwinClassId() + "]");
        twinFieldEntity = new TwinFieldEntity()
                .setTwinClassField(twinClassField)
                .setTwinClassFieldId(twinClassField.getId())
                .setTwin(twinEntity)
                .setTwinId(twinEntity.getId())
                .setValue("");
        return twinFieldEntity;
    }

    public List<TwinFieldEntity> findTwinFieldsIncludeMissing(TwinEntity twinEntity) {
        Map<UUID, TwinFieldEntity> twinFieldEntityMap = twinFieldRepository.findByTwinId(twinEntity.getId()).stream().collect(Collectors.toMap(TwinFieldEntity::getTwinClassFieldId, Function.identity()));
        List<TwinClassFieldEntity> twinFieldClassEntityList = twinClassFieldService.findTwinClassFieldsIncludeParent(twinEntity.getTwinClass());
        List<TwinFieldEntity> ret = new ArrayList<>();
        for (TwinClassFieldEntity twinClassField : twinFieldClassEntityList) {
            if (twinFieldEntityMap.containsKey(twinClassField.getId()))
                ret.add(twinFieldEntityMap.get(twinClassField.getId()));
            else
                ret.add(createTwinFieldEntity(twinEntity, twinClassField, ""));
        }
        return ret;
    }

    public TwinFieldEntity createTwinFieldEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity, String value) {
        return new TwinFieldEntity()
                .setTwinClassField(twinClassFieldEntity)
                .setTwinClassFieldId(twinClassFieldEntity.getId())
                .setTwin(twinEntity)
                .setTwinId(twinEntity.getId())
                .setValue(value);
    }

    public void updateField(UUID twinFieldId, FieldValue fieldValue) throws ServiceException {
        updateField(entitySmartService.findById(twinFieldId, twinFieldRepository, EntitySmartService.FindMode.ifEmptyThrows), fieldValue);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinCreateResult createTwin(ApiUser apiUser, TwinEntity twinEntity, List<FieldValue> values, List<TwinAttachmentEntity> attachmentEntityList, List<TwinLinkEntity> linksEntityList) throws ServiceException {
        TwinflowEntity twinflowEntity = twinflowService.getByTwinClass(twinEntity.getTwinClassId());
        TwinClassEntity twinClassEntity = twinClassService.findEntity(twinEntity.getTwinClassId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
        twinEntity
                .setTwinClass(twinClassEntity)
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setHeadTwinId(twinClassService.checkHeadTwinAllowedForClass(twinEntity.getHeadTwinId(), twinClassEntity))
                .setTwinStatusId(twinflowEntity.initialTwinStatusId());
        switch (twinClassEntity.getOwnerType()) {
            case DOMAIN:
                //twin will not be owned neither businessAccount, neither user
                break;
            case DOMAIN_BUSINESS_ACCOUNT:
                if (apiUser.getBusinessAccount() == null)
                    throw new ServiceException(ErrorCodeTwins.BUSINESS_ACCOUNT_UNKNOWN, twinClassEntity.easyLog(EasyLoggable.Level.NORMAL) + " can not be created without businessAccount owner");
                twinEntity
                        .setOwnerBusinessAccountId(apiUser.getBusinessAccount().getId())
                        .setOwnerBusinessAccount(apiUser.getBusinessAccount())
                        .setOwnerUserId(null);
                break;
            case DOMAIN_USER:
                if (apiUser.getUser() == null)
                    throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, twinClassEntity.easyLog(EasyLoggable.Level.NORMAL) + " can not be created without user owner");
                twinEntity
                        .setOwnerUserId(apiUser.getUser().getId())
                        .setOwnerUser(apiUser.getUser())
                        .setOwnerBusinessAccountId(null);
        }
        twinEntity = entitySmartService.save(twinEntity, twinRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        saveTwinFields(twinEntity, values);
        if (CollectionUtils.isNotEmpty(attachmentEntityList)) {
            attachmentService.addAttachments(twinEntity.getId(), apiUser.getUser(), attachmentEntityList);
        }
        if (CollectionUtils.isNotEmpty(linksEntityList))
            twinLinkService.addLinks(twinEntity, linksEntityList);
        return new TwinCreateResult()
                .setCreatedTwin(twinEntity)
                .setBusinessAccountAliasEntityList(createTwinBusinessAccountAliases(twinEntity))
                .setDomainAliasEntityList(createTwinDomainAliases(twinEntity));
    }

    @Transactional
    public void saveTwinFields(TwinEntity twinEntity, List<FieldValue> values) throws ServiceException {
        Map<UUID, FieldValue> twinClassFieldValuesMap = values.stream().collect(Collectors.toMap(f -> f.getTwinClassField().getId(), Function.identity()));
        List<TwinClassFieldEntity> twinClassFieldEntityList = twinClassFieldService.findTwinClassFieldsIncludeParent(twinEntity.getTwinClass());
        TwinFieldEntity twinFieldEntity;
        FieldValue fieldValue;
        List<TwinFieldEntity> twinFieldEntityList = new ArrayList<>();
        for (TwinClassFieldEntity twinClassFieldEntity : twinClassFieldEntityList) {
            fieldValue = twinClassFieldValuesMap.get(twinClassFieldEntity.getId());
            if (fieldValue == null)
                if (twinClassFieldEntity.isRequired())
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinClassFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " is required");
                else
                    continue;
            twinFieldEntity = createTwinFieldEntity(twinEntity, twinClassFieldEntity, null);
            var fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturer(), FieldTyper.class);
            twinFieldEntityList.add(
                    twinFieldEntity.setValue(fieldTyper.serializeValue(twinFieldEntity, fieldValue)));
        }
        entitySmartService.saveAllAndLog(twinFieldEntityList, twinFieldRepository);
    }

    @Transactional
    public void updateTwin(TwinEntity updateTwinEntity, TwinEntity dbTwinEntity, List<FieldValue> values, List<TwinAttachmentEntity> attachmentAddEntityList, List<UUID> attachmentDeleteUUIDList, List<TwinAttachmentEntity> attachmentUpdateEntityList) throws ServiceException {
        ChangesHelper changesHelper = new ChangesHelper();
        if (changesHelper.isChanged("headTwinId", dbTwinEntity.getHeadTwinId(), updateTwinEntity.getHeadTwinId())) {
            dbTwinEntity.setHeadTwinId(twinClassService.checkHeadTwinAllowedForClass(updateTwinEntity.getHeadTwinId(), dbTwinEntity.getTwinClass()));
        }
        if (changesHelper.isChanged("name", dbTwinEntity.getName(), updateTwinEntity.getName())) {
            dbTwinEntity.setName(updateTwinEntity.getName());
        }
        if (changesHelper.isChanged("description", dbTwinEntity.getDescription(), updateTwinEntity.getDescription())) {
            dbTwinEntity.setDescription(updateTwinEntity.getDescription());
        }
        if (changesHelper.isChanged("assignerUser", dbTwinEntity.getAssignerUserId(), updateTwinEntity.getAssignerUserId())) {
            dbTwinEntity.setAssignerUserId(updateTwinEntity.getAssignerUserId());
        }
        entitySmartService.saveAndLogChanges(dbTwinEntity, twinRepository, changesHelper);
        updateTwinFields(dbTwinEntity, values);
        if (CollectionUtils.isNotEmpty(attachmentAddEntityList)) {
            attachmentService.addAttachments(dbTwinEntity.getId(), dbTwinEntity.getCreatedByUser(), attachmentAddEntityList);
        }
        if (CollectionUtils.isNotEmpty(attachmentUpdateEntityList)) {
            attachmentService.updateAttachments(attachmentAddEntityList);
        }
        if (CollectionUtils.isNotEmpty(attachmentDeleteUUIDList)) {
            attachmentService.deleteAttachments(dbTwinEntity.getId(), attachmentDeleteUUIDList);
        }
    }

    @Transactional
    public void updateTwinFields(TwinEntity twinEntity, List<FieldValue> values) throws ServiceException {
        List<TwinFieldEntity> twinFieldEntityList = new ArrayList<>();
        TwinFieldEntity twinFieldEntity;
        ChangesHelper changesHelper = new ChangesHelper();
        for (FieldValue fieldValue : values) {
            twinFieldEntity = findTwinFieldIncludeMissing(twinEntity.getId(), fieldValue.getTwinClassField().getKey());
            FieldTyper fieldTyper = featurerService.getFeaturer(twinFieldEntity.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
            String newValue = fieldTyper.serializeValue(twinFieldEntity, fieldValue);
            if (changesHelper.isChanged("field[" + twinFieldEntity.getTwinClassField().getKey() + "]", twinFieldEntity.getValue(), newValue)) {
                twinFieldEntity.setValue(newValue);
                twinFieldEntityList.add(twinFieldEntity);
            }
        }
        if (changesHelper.hasChanges())
            entitySmartService.saveAllAndLogChanges(twinFieldEntityList, twinFieldRepository, changesHelper);
    }

    public List<TwinBusinessAccountAliasEntity> createTwinBusinessAccountAliases(TwinEntity twinEntity) {
        if (twinEntity.getTwinClass().getOwnerType() != TwinClassEntity.OwnerType.DOMAIN_BUSINESS_ACCOUNT && twinEntity.getTwinClass().getOwnerType() != TwinClassEntity.OwnerType.DOMAIN_BUSINESS_ACCOUNT_USER)
            return new ArrayList<>(); // businessAccountAliases can not be created for this twin
        twinBusinessAccountAliasRepository.createAliasByClass(twinEntity.getId());
        TwinEntity spaceTwin = findSpaceForTwin(twinEntity);
        if (spaceTwin != null) {
            twinBusinessAccountAliasRepository.createAliasBySpace(twinEntity.getId(), spaceTwin.getId());
        }
        return twinBusinessAccountAliasRepository.findAllByTwinId(twinEntity.getId());
    }

    public List<TwinDomainAliasEntity> createTwinDomainAliases(TwinEntity twinEntity) {
        twinDomainAliasRepository.createAliasByClass(twinEntity.getId());
        TwinEntity spaceTwin = findSpaceForTwin(twinEntity);
        if (spaceTwin != null) {
            twinDomainAliasRepository.createAliasBySpace(twinEntity.getId(), spaceTwin.getId());
        }
        return twinDomainAliasRepository.findAllByTwinId(twinEntity.getId());
    }

    public TwinEntity findSpaceForTwin(TwinEntity twinEntity) {
        if (twinEntity.getSpaceTwin() != null)
            return twinEntity.getSpaceTwin();
        if (twinEntity.getHeadTwin() == null && twinEntity.getHeadTwinId() != null)
            twinEntity.setHeadTwin(twinRepository.findById(twinEntity.getHeadTwinId()).get()); //todo fix
        twinEntity.setSpaceTwin(findSpaceForTwin(twinEntity, twinEntity.getHeadTwin(), 10));
        return twinEntity.getSpaceTwin();
    }

    protected TwinEntity findSpaceForTwin(TwinEntity twinEntity, TwinEntity headTwin, int recursionDepth) {
        if (headTwin == null)
            return null;
        else if (headTwin.getTwinClass().isSpace())
            return headTwin;
        else if (recursionDepth == 0) {
            log.warn("Can not detect space for " + twinEntity);
            return null;
        } else
            return findSpaceForTwin(twinEntity, headTwin.getHeadTwin(), recursionDepth - 1);
    }

    public void updateField(TwinFieldEntity twinFieldEntity, FieldValue fieldValue) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(twinFieldEntity.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
        String newValue = fieldTyper.serializeValue(twinFieldEntity, fieldValue);
        ChangesHelper changesHelper = new ChangesHelper();
        if (changesHelper.isChanged("field", twinFieldEntity.getValue(), newValue)) {
            twinFieldEntity.setValue(newValue);
            entitySmartService.saveAndLogChanges(twinFieldEntity, twinFieldRepository, changesHelper);
        }
    }

    public void deleteTwin(UUID twinId) throws ServiceException {
        entitySmartService.deleteAndLog(twinId, twinRepository);// all linked data will be deleted by fk cascading
    }

    @Data
    @Accessors(chain = true)
    public static class TwinCreateResult {
        private TwinEntity createdTwin;
        private List<TwinBusinessAccountAliasEntity> businessAccountAliasEntityList;
        private List<TwinDomainAliasEntity> domainAliasEntityList;
    }
}
