package org.twins.core.service.validator;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.validator.TwinClassFieldActionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinClassFieldActionValidatorRuleRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFieldActionValidatorRuleService extends EntitySecureFindServiceImpl<TwinClassFieldActionValidatorRuleEntity> {
    private final TwinClassFieldActionValidatorRuleRepository repository;

    @Override
    public CrudRepository<TwinClassFieldActionValidatorRuleEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinClassFieldActionValidatorRuleEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldActionValidatorRuleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldActionValidatorRuleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFieldActionValidatorRuleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) {
        return true;
    }

    public List<TwinClassFieldActionValidatorRuleEntity> findByTwinClassFieldIdInOrderByOrder(Set<UUID> twinClassFieldIds) {
        return repository.findByTwinClassFieldIdInOrderByOrder(twinClassFieldIds);
    }
}
