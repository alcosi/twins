package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twin.TwinFieldRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TQL;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinService {
    final TwinRepository twinRepository;
    final TwinFieldRepository twinFieldRepository;
    final TwinClassFieldService twinClassFieldService;
    final EntityManager entityManager;

    public List<TwinEntity> findTwins(ApiUser apiUser, TQL tql) {
        return twinRepository.findByBusinessAccountId(apiUser.businessAccountId());
    }

    public TwinEntity findTwin(ApiUser apiUser, UUID twinId) {
        return twinRepository.findById(twinId).get();
    }

    public List<TwinFieldEntity> findTwinFields(UUID twinId) {
        return twinFieldRepository.findByTwinId(twinId);
    }

    public List<TwinFieldEntity> findTwinFieldsAll(TwinEntity twinEntity) {
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
}
