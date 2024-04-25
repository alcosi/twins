package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.kit.KitGroupedObj;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.action.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.featurer.twin.validator.TwinValidator;
import org.twins.core.service.permission.PermissionService;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinActionService {
    final TwinClassActionPermissionRepository twinClassActionPermissionRepository;
    final TwinClassActionValidatorRepository twinClassActionValidatorRepository;
    final TwinRepository twinRepository;
    @Lazy
    final PermissionService permissionService;
    @Lazy
    final FeaturerService featurerService;

    public void loadActions(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getActions() != null)
            return;
        loadClassProtectedActions(twinEntity.getTwinClass());
        if (twinEntity.getTwinClass().getActionsProtectedByPermission().isEmpty()) {
            twinEntity.setActions(EnumSet.allOf(TwinAction.class));
            return;
        }
        twinEntity.setActions(new HashSet<>());
        for (TwinAction twinAction : TwinAction.values()) {
            TwinClassActionPermissionEntity twinActionProtectedByPermission = twinEntity.getTwinClass().getActionsProtectedByPermission().get(twinAction);
            if (twinActionProtectedByPermission != null) {
                if (!permissionService.hasPermission(twinEntity, twinActionProtectedByPermission.getPermissionId()))
                    continue; // current action is forbidden
            }
            if (KitUtils.isEmpty(twinEntity.getTwinClass().getActionsProtectedByValidator())) {
                twinEntity.getActions().add(twinAction); // current action is permitted
                continue;
            }
            boolean isValid = true;
            for (TwinClassActionValidatorEntity twinClassActionValidatorEntity : twinEntity.getTwinClass().getActionsProtectedByValidator().getGrouped(twinAction)) {
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

    public void loadClassProtectedActions(TwinClassEntity twinClassEntity) {
        if (twinClassEntity.getActionsProtectedByPermission() == null)
            twinClassEntity.setActionsProtectedByPermission(new Kit<>(
                    twinClassActionPermissionRepository.findByTwinClassId(twinClassEntity.getId()),
                    TwinClassActionPermissionEntity::getTwinAction));
        if (twinClassEntity.getActionsProtectedByValidator() == null)
            twinClassEntity.setActionsProtectedByValidator(new KitGrouped<>(
                    twinClassActionValidatorRepository.findByTwinClassIdOrderByOrder(twinClassEntity.getId()),
                    TwinClassActionValidatorEntity::getId,
                    TwinClassActionValidatorEntity::getTwinAction));
    }

    public void loadClassProtectedActions(Collection<TwinClassEntity> twinClassCollection) {
        Map<UUID, TwinClassEntity> needLoadByPermissions = new HashMap<>();
        Map<UUID, TwinClassEntity> needLoadByValidators = new HashMap<>();
        for (TwinClassEntity twinClassEntity : twinClassCollection) {
            if (twinClassEntity.getActionsProtectedByPermission() == null)
                needLoadByPermissions.put(twinClassEntity.getId(), twinClassEntity);
            if (twinClassEntity.getActionsProtectedByValidator() == null)
                needLoadByValidators.put(twinClassEntity.getId(), twinClassEntity);
        }
        if (!needLoadByPermissions.isEmpty()) {
            List<TwinClassActionPermissionEntity> twinClassActionPermissionEntities = twinClassActionPermissionRepository.findByTwinClassIdIn(needLoadByPermissions.keySet());
            if (!CollectionUtils.isEmpty(twinClassActionPermissionEntities)) {
                KitGrouped<TwinClassActionPermissionEntity, UUID, UUID> actionGroupedByClass = new KitGrouped<>(twinClassActionPermissionEntities, TwinClassActionPermissionEntity::getId, TwinClassActionPermissionEntity::getTwinClassId);
                for (TwinClassEntity twinClassEntity : needLoadByPermissions.values()) {
                    twinClassEntity.setActionsProtectedByPermission(new Kit<>(actionGroupedByClass.getGrouped(twinClassEntity.getId()), TwinClassActionPermissionEntity::getTwinAction));
                }
            }
        }
        if (!needLoadByValidators.isEmpty()) {
            List<TwinClassActionValidatorEntity> twinClassActionValidatorEntities = twinClassActionValidatorRepository.findByTwinClassIdIn(needLoadByPermissions.keySet());
            if (!CollectionUtils.isEmpty(twinClassActionValidatorEntities)) {
                KitGrouped<TwinClassActionValidatorEntity, UUID, UUID> actionGroupedByClass = new KitGrouped<>(twinClassActionValidatorEntities, TwinClassActionValidatorEntity::getId, TwinClassActionValidatorEntity::getTwinClassId);
                for (TwinClassEntity twinClassEntity : needLoadByValidators.values()) {
                    twinClassEntity.setActionsProtectedByValidator(new KitGrouped<>(actionGroupedByClass.getGrouped(twinClassEntity.getId()), TwinClassActionValidatorEntity::getId, TwinClassActionValidatorEntity::getTwinAction));
                }
            }
        }
    }

    public void loadActions(Collection<TwinEntity> twinEntityList) throws ServiceException {
        List<TwinEntity> needLoad = new ArrayList<>();
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getActions() == null)
                needLoad.add(twinEntity);
        KitGroupedObj<TwinEntity, UUID, UUID, TwinClassEntity> groupedByClass = new KitGroupedObj<>(needLoad, TwinEntity::getId, TwinEntity::getTwinClassId, TwinEntity::getTwinClass);
        loadClassProtectedActions(groupedByClass.getGroupingObjectMap().values());
        Map<PermissionService.PermissionDetectKey, List<TwinEntity>> permissionDetectKeys;
        TwinClassEntity twinClassEntity;
        Map<UUID, Set<TwinAction>> twinsForbiddenActions = new HashMap<>();
        for (Map.Entry<UUID, List<TwinEntity>> entry : groupedByClass.getGroupedMap().entrySet()) { // looping grouped by class
            twinClassEntity = groupedByClass.getGroupingObject(entry.getKey());
            for (TwinAction twinAction : TwinAction.values()) {
                if (KitUtils.isNotEmpty(twinClassEntity.getActionsProtectedByPermission())) {
                    permissionDetectKeys = permissionService.convertToDetectKeys(entry.getValue()); // extract all permission check variants
                    for (TwinClassActionPermissionEntity classActionPermissionEntity : twinClassEntity.getActionsProtectedByPermission().getCollection()) { // looping permissions by current class
                        for (Map.Entry<PermissionService.PermissionDetectKey, List<TwinEntity>> samePermissionGroupEntry : permissionDetectKeys.entrySet()) { // looping detected keys
                            if (!permissionService.hasPermission(samePermissionGroupEntry.getKey(), classActionPermissionEntity.getPermissionId())) { // all twins linked to current key will have such action
                                for (TwinEntity twinEntity : samePermissionGroupEntry.getValue()) {
                                    twinsForbiddenActions.computeIfAbsent(twinEntity.getId(), k -> new HashSet<>());
                                    twinsForbiddenActions.get(twinEntity.getId()).add(twinAction);
                                }
                            }
                        }
                    }
                }
                if (KitUtils.isNotEmpty(twinClassEntity.getActionsProtectedByValidator())) {
                    for (TwinClassActionValidatorEntity classActionValidatorEntity : twinClassEntity.getActionsProtectedByValidator().getGrouped(twinAction)) { // looping permissions by current class
                        if (!classActionValidatorEntity.isActive()) {
                            log.info(classActionValidatorEntity.logShort() + " is inactive");
                            continue;
                        }
                        TwinValidator twinValidator = featurerService.getFeaturer(classActionValidatorEntity.getTwinValidatorFeaturer(), TwinValidator.class);
                        TwinValidator.CollectionValidationResult collectionValidationResult = twinValidator.isValid(classActionValidatorEntity.getTwinValidatorParams(), entry.getValue(), classActionValidatorEntity.isInvert());
                        for (TwinEntity twinEntity : entry.getValue()) {
                            TwinValidator.ValidationResult validationResult = collectionValidationResult.getTwinsResults().get(twinEntity.getId());
                            if (validationResult == null) {
                                log.warn(classActionValidatorEntity.logShort() + " validation result should not be null");
                                continue;
                            }
                            if (!validationResult.isValid()) {
                                twinsForbiddenActions.computeIfAbsent(twinEntity.getId(), k -> new HashSet<>());
                                twinsForbiddenActions.get(twinEntity.getId()).add(twinAction);
                            }
                        }
                    }
                }
            }
        }
        Set<TwinAction> forbiddenActions;
        for (TwinEntity twinEntity : needLoad) {
            forbiddenActions = twinsForbiddenActions.get(twinEntity.getId());
            if (forbiddenActions == null)
                twinEntity.setActions(EnumSet.allOf(TwinAction.class));
            else
                twinEntity.setActions(EnumSet.allOf(TwinAction.class).stream().filter(Predicate.not(forbiddenActions::contains)).collect(Collectors.toSet()));
        }
    }
}
