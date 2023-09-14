package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.*;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TQL;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinflow.TwinflowService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
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

    public List<TwinEntity> findTwins(ApiUser apiUser, TQL tql) {
        return twinRepository.findByBusinessAccountId(apiUser.businessAccountId());
    }

    public List<TwinEntity> findTwinsByClass(UUID twinClassId) {
        return twinRepository.findByTwinClassId(twinClassId);
    }

    public TwinEntity findTwin(ApiUser apiUser, UUID twinId) {
        return twinRepository.findById(twinId).get();
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
                .twinClassFieldId(twinClassField.id())
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
            if (twinFieldEntityMap.containsKey(twinClassField.id()))
                ret.add(twinFieldEntityMap.get(twinClassField.id()));
            else
                ret.add(new TwinFieldEntity()
                        .twinClassField(twinClassField)
                        .twinClassFieldId(twinClassField.id())
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
    public TwinCreateResult createTwin(TwinEntity twinEntity, List<FieldValue> values) throws ServiceException {
        TwinflowEntity twinflowEntity = twinflowService.getByTwinClass(twinEntity.twinClassId());
        twinEntity
                .createdAt(Timestamp.from(Instant.now()))
                .headTwinId(twinClassService.checkHeadTwinAllowedForClass(twinEntity.headTwinId(), twinEntity.twinClassId()))
                .twinStatusId(twinflowEntity.initialTwinStatusId());
        twinEntity = twinRepository.save(twinEntity);
        Map<UUID, FieldValue> twinClassFieldValuesMap = values.stream().collect(Collectors.toMap(f -> f.getTwinClassField().id(), Function.identity()));
        List<TwinClassFieldEntity> twinClassFieldEntityList = twinClassFieldService.findTwinClassFields(twinEntity.twinClassId());
        TwinFieldEntity twinFieldEntity;
        FieldValue fieldValue;
        List<TwinFieldEntity> twinFieldEntityList = new ArrayList<>();
        for (TwinClassFieldEntity twinClassFieldEntity : twinClassFieldEntityList) {
            fieldValue = twinClassFieldValuesMap.get(twinClassFieldEntity.id());
            if (fieldValue == null)
                if (twinClassFieldEntity.required())
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinClassFieldEntity.logShort() + " is required");
                else
                    continue;
            twinFieldEntity = new TwinFieldEntity()
                    .twinClassField(twinClassFieldEntity)
                    .twinClassFieldId(twinClassFieldEntity.id())
                    .twin(twinEntity)
                    .twinId(twinEntity.id());
            var fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.fieldTyperFeaturer(), FieldTyper.class);
            twinFieldEntityList.add(
                    twinFieldEntity.value(fieldTyper.serializeValue(twinFieldEntity, fieldValue)));
        }
        twinFieldRepository.saveAll(twinFieldEntityList);

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
        FieldTyper fieldTyper = featurerService.getFeaturer(twinFieldEntity.twinClassField().fieldTyperFeaturer(), FieldTyper.class);
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
