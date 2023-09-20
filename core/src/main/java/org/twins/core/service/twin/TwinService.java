package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.*;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.BasicSearch;
import org.twins.core.domain.TQL;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.attachment.AttachmentService;
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
public class TwinService {
    final TwinRepository twinRepository;
    final TwinFieldRepository twinFieldRepository;
    final TwinClassFieldRepository twinClassFieldRepository;
    final TwinAliasRepository twinAliasRepository;
    final TwinClassFieldService twinClassFieldService;
    final EntityManager entityManager;
    final EntitySmartService entitySmartService;
    final TwinflowService twinflowService;
    final TwinClassService twinClassService;
    final FeaturerService featurerService;
    final AttachmentService attachmentService;

    public UUID checkTwinId(UUID twinId, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(twinId, "twinId", twinRepository, checkMode);
    }

    public List<TwinEntity> findTwins(ApiUser apiUser, TQL tql) {
        return twinRepository.findByBusinessAccountId(apiUser.getBusinessAccount().getId());
    }

    public List<TwinEntity> findTwins(ApiUser apiUser, BasicSearch basicSearch) {
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
        if (CollectionUtils.isNotEmpty(basicSearch.getSpaceTwinIdList()))
            predicate.add(twin.get(TwinEntity.Fields.headTwinId).in(basicSearch.getSpaceTwinIdList()));
        criteriaQuery.where(predicate.stream().toArray(Predicate[]::new));
        Query query = entityManager.createQuery(criteriaQuery);
        return query.getResultList();
    }

    public List<TwinEntity> findTwinsByClass(UUID twinClassId) {
        return twinRepository.findByTwinClassId(twinClassId);
    }

    public TwinEntity findTwin(ApiUser apiUser, UUID twinId) throws ServiceException {
        return findTwin(apiUser, twinId, EntitySmartService.FindMode.ifEmptyThrows);
    }

    public TwinEntity findTwinByAlias(ApiUser apiUser, String twinAlias) throws ServiceException {
        TwinAliasEntity twinAliasEntity = twinAliasRepository.findByBusinessAccountIdAndAlias(apiUser.getBusinessAccount().getId(), twinAlias);
        if (twinAliasEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_ALIAS_UNKNOWN, "unknown twin alias[" + twinAlias + "]");
        return twinAliasEntity.getTwin();
    }

    public TwinEntity findTwin(ApiUser apiUser, UUID twinId, EntitySmartService.FindMode findMode) throws ServiceException {
        return entitySmartService.findById(twinId, "twinId", twinRepository, findMode);
    }

    public List<TwinFieldEntity> findTwinFields(UUID twinId) {
        return twinFieldRepository.findByTwinId(twinId);
    }

    public TwinFieldEntity findTwinField(UUID twinFieldId) throws ServiceException {
        return entitySmartService.findById(twinFieldId, "twinField", twinFieldRepository, EntitySmartService.FindMode.ifEmptyThrows);
    }

    public TwinFieldEntity findTwinFieldIncludeMissing(UUID twinId, String fieldKey) throws ServiceException {
        TwinFieldEntity twinFieldEntity = twinFieldRepository.findByTwinIdAndTwinClassField_Key(twinId, fieldKey);
        if (twinFieldEntity != null)
            return twinFieldEntity;
        TwinEntity twinEntity = entitySmartService.findById(twinId, "twin", twinRepository, EntitySmartService.FindMode.ifEmptyThrows);
        TwinClassFieldEntity twinClassField = twinClassFieldService.findByTwinClassIdAndKey(twinEntity.twinClassId(), fieldKey);
        if (twinClassField == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_UNKNOWN, "unknown fieldKey[" + fieldKey + "] for twin["
                    + twinId + "] of class[" + twinEntity.twinClass().key() + " : " + twinEntity.twinClassId() + "]");
        twinFieldEntity = new TwinFieldEntity()
                .twinClassField(twinClassField)
                .twinClassFieldId(twinClassField.getId())
                .twin(twinEntity)
                .twinId(twinEntity.id())
                .value("");
        return twinFieldEntity;
    }

    public List<TwinFieldEntity> findTwinFieldsIncludeMissing(TwinEntity twinEntity) {
        Map<UUID, TwinFieldEntity> twinFieldEntityMap = twinFieldRepository.findByTwinId(twinEntity.id()).stream().collect(Collectors.toMap(TwinFieldEntity::twinClassFieldId, Function.identity()));
        List<TwinClassFieldEntity> twinFieldClassEntityList = twinClassFieldService.findTwinClassFields(twinEntity.twinClassId());
        List<TwinFieldEntity> ret = new ArrayList<>();
        for (TwinClassFieldEntity twinClassField : twinFieldClassEntityList) {
            if (twinFieldEntityMap.containsKey(twinClassField.getId()))
                ret.add(twinFieldEntityMap.get(twinClassField.getId()));
            else
                ret.add(new TwinFieldEntity()
                        .twinClassField(twinClassField)
                        .twinClassFieldId(twinClassField.getId())
                        .twin(twinEntity)
                        .twinId(twinEntity.id())
                        .value(""));
        }
        return ret;
    }

    public void updateField(UUID twinFieldId, FieldValue fieldValue) throws ServiceException {
        updateField(entitySmartService.findById(twinFieldId, "twinField", twinFieldRepository, EntitySmartService.FindMode.ifEmptyThrows), fieldValue);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinCreateResult createTwin(TwinEntity twinEntity, List<FieldValue> values, List<String> attachmentsLinks) throws ServiceException {
        TwinflowEntity twinflowEntity = twinflowService.getByTwinClass(twinEntity.twinClassId());
        twinEntity
                .createdAt(Timestamp.from(Instant.now()))
                .headTwinId(twinClassService.checkHeadTwinAllowedForClass(twinEntity.headTwinId(), twinEntity.twinClassId()))
                .twinStatusId(twinflowEntity.initialTwinStatusId());
        twinEntity = twinRepository.save(twinEntity);
        Map<UUID, FieldValue> twinClassFieldValuesMap = values.stream().collect(Collectors.toMap(f -> f.getTwinClassField().getId(), Function.identity()));
        List<TwinClassFieldEntity> twinClassFieldEntityList = twinClassFieldService.findTwinClassFields(twinEntity.twinClassId());
        TwinFieldEntity twinFieldEntity;
        FieldValue fieldValue;
        List<TwinFieldEntity> twinFieldEntityList = new ArrayList<>();
        for (TwinClassFieldEntity twinClassFieldEntity : twinClassFieldEntityList) {
            fieldValue = twinClassFieldValuesMap.get(twinClassFieldEntity.getId());
            if (fieldValue == null)
                if (twinClassFieldEntity.isRequired())
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinClassFieldEntity.logShort() + " is required");
                else
                    continue;
            twinFieldEntity = new TwinFieldEntity()
                    .twinClassField(twinClassFieldEntity)
                    .twinClassFieldId(twinClassFieldEntity.getId())
                    .twin(twinEntity)
                    .twinId(twinEntity.id());
            var fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturer(), FieldTyper.class);
            twinFieldEntityList.add(
                    twinFieldEntity.value(fieldTyper.serializeValue(twinFieldEntity, fieldValue)));
        }
        twinFieldRepository.saveAll(twinFieldEntityList);
        if (CollectionUtils.isNotEmpty(attachmentsLinks)) {
            attachmentService.addAttachments(twinEntity.id(), twinEntity.createdByUserId(), attachmentsLinks);
        }
        return new TwinCreateResult()
                .setCreatedTwin(twinEntity)
                .setAliasEntityList(createTwinAliases(twinEntity));
    }

    public List<TwinAliasEntity> createTwinAliases(TwinEntity twinEntity) {
        twinAliasRepository.createAliasByClass(twinEntity.id());
        TwinEntity spaceTwin = findSpaceForTwin(twinEntity);
        if (spaceTwin != null)
            twinAliasRepository.createAliasBySpace(twinEntity.id(), spaceTwin.id());
        return twinAliasRepository.findAllByTwinId(twinEntity.id());
    }

    public TwinEntity findSpaceForTwin(TwinEntity twinEntity) {
        if (twinEntity.headTwin() == null && twinEntity.headTwinId() != null)
            twinEntity.headTwin(twinRepository.findById(twinEntity.headTwinId()).get()); //todo fix
        return findSpaceForTwin(twinEntity, twinEntity.headTwin(), 10);
    }

    protected TwinEntity findSpaceForTwin(TwinEntity twinEntity, TwinEntity headTwin, int recursionDepth) {
        if (headTwin == null)
            return null;
        else if (headTwin.twinClass().space())
            return headTwin;
        else if (recursionDepth == 0) {
            log.warn("Can not detect space for " + twinEntity);
            return null;
        } else
            return findSpaceForTwin(twinEntity, headTwin.headTwin(), recursionDepth - 1);
    }

    public void updateField(TwinFieldEntity twinFieldEntity, FieldValue fieldValue) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(twinFieldEntity.twinClassField().getFieldTyperFeaturer(), FieldTyper.class);
        twinFieldEntity.value(fieldTyper.serializeValue(twinFieldEntity, fieldValue));
        twinFieldRepository.save(twinFieldEntity);
    }

    @Data
    @Accessors(chain = true)
    public static class TwinCreateResult {
        private TwinEntity createdTwin;
        private List<TwinAliasEntity> aliasEntityList;
    }
}
