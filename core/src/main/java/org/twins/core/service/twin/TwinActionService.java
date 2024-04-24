package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.Kit;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.action.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.featurer.twin.validator.TwinValidator;
import org.twins.core.service.permission.PermissionService;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinActionService {
    final TwinClassActionRepository twinClassActionRepository;
    final TwinClassActionValidatorRepository twinClassActionValidatorRepository;
    @Lazy
    final PermissionService permissionService;
    @Lazy
    final FeaturerService featurerService;
    public void loadActions(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getActions() != null)
            return;
        loadActions(twinEntity.getTwinClass());
        if (twinEntity.getTwinClass().getProtectedActions().isEmpty()) {
            twinEntity.setActions(EnumSet.allOf(TwinAction.class));
            return;
        }
        twinEntity.setActions(new HashSet<>());
        for (TwinAction twinAction : TwinAction.values()) {
            TwinClassActionEntity protectedTwinAction = twinEntity.getTwinClass().getProtectedActions().get(twinAction);
            if (protectedTwinAction == null) {
                twinEntity.getActions().add(twinAction);
                continue;
            }
            if (protectedTwinAction.getPermissionId() != null && !permissionService.hasPermission(twinEntity, protectedTwinAction.getPermissionId()))
                continue;
            List<TwinClassActionValidatorEntity> twinClassActionValidatorEntityList = twinClassActionValidatorRepository.findByTwinClassActionId(protectedTwinAction.getId());
            if (CollectionUtils.isEmpty(twinClassActionValidatorEntityList)) {
                twinEntity.getActions().add(twinAction);
                continue;
            }
            boolean isValid = true;
            for (TwinClassActionValidatorEntity twinClassActionValidatorEntity : twinClassActionValidatorEntityList) {
                TwinValidator twinValidator = featurerService.getFeaturer(twinClassActionValidatorEntity.getTwinValidatorFeaturer(), TwinValidator.class);
                TwinValidator.ValidationResult validationResult = twinValidator.isValid(twinClassActionValidatorEntity.getTwinValidatorParams(), twinEntity, twinClassActionValidatorEntity.isInvert());
                if (!validationResult.isValid()) {
                    log.error(validationResult.getMessage());
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                twinEntity.getActions().add(twinAction);
            }
        }
    }

    public void loadActions(TwinClassEntity twinClassEntity) {
        if (twinClassEntity.getProtectedActions() != null)
            return;
        twinClassEntity.setProtectedActions(new Kit<>(twinClassActionRepository.findByTwinClassId(twinClassEntity.getId()), TwinClassActionEntity::getTwinAction));
    }
}
