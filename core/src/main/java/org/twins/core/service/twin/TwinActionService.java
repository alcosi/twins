package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
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
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.twin.validator.TwinValidator;
import org.twins.core.service.permission.PermissionService;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.cambium.common.EasyLoggable.Level.NORMAL;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinActionService {
    final TwinActionPermissionRepository twinActionPermissionRepository;
    final TwinActionValidatorRepository twinActionValidatorRepository;
    final TwinRepository twinRepository;
    @Lazy
    final PermissionService permissionService;
    @Lazy
    final FeaturerService featurerService;

    public void loadActions(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getActions() != null)
            return;
        loadClassProtectedActions(twinEntity.getTwinClass());
        if (twinEntity.getTwinClass().getActionsProtectedByPermission().isEmpty() && twinEntity.getTwinClass().getActionsProtectedByValidator().isEmpty()) {
            twinEntity.setActions(EnumSet.allOf(TwinAction.class));
            return;
        }
        twinEntity.setActions(new HashSet<>());
        for (TwinAction twinAction : TwinAction.values()) {
            TwinActionPermissionEntity twinActionProtectedByPermission = twinEntity.getTwinClass().getActionsProtectedByPermission().get(twinAction);
            if (twinActionProtectedByPermission != null) {
                if (!permissionService.hasPermission(twinEntity, twinActionProtectedByPermission.getPermissionId())) {
                    continue;
                }
            }
            if (KitUtils.isEmpty(twinEntity.getTwinClass().getActionsProtectedByValidator())) {
                twinEntity.getActions().add(twinAction); // current action is permitted
                continue;
            }
            boolean isValid = true;
            for (TwinActionValidatorEntity twinActionValidatorEntity : twinEntity.getTwinClass().getActionsProtectedByValidator().getGrouped(twinAction)) {
                TwinValidator twinValidator = featurerService.getFeaturer(twinActionValidatorEntity.getTwinValidatorFeaturer(), TwinValidator.class);
                TwinValidator.ValidationResult validationResult = twinValidator.isValid(twinActionValidatorEntity.getTwinValidatorParams(), twinEntity, twinActionValidatorEntity.isInvert());
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
                    twinActionPermissionRepository.findByTwinClassId(twinClassEntity.getId()),
                    TwinActionPermissionEntity::getTwinAction));
        if (twinClassEntity.getActionsProtectedByValidator() == null)
            twinClassEntity.setActionsProtectedByValidator(new KitGrouped<>(
                    twinActionValidatorRepository.findByTwinClassIdOrderByOrder(twinClassEntity.getId()),
                    TwinActionValidatorEntity::getId,
                    TwinActionValidatorEntity::getTwinAction));
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
            List<TwinActionPermissionEntity> twinClassActionPermissionEntities = twinActionPermissionRepository.findByTwinClassIdIn(needLoadByPermissions.keySet());
            if (!CollectionUtils.isEmpty(twinClassActionPermissionEntities)) {
                KitGrouped<TwinActionPermissionEntity, UUID, UUID> actionGroupedByClass = new KitGrouped<>(twinClassActionPermissionEntities, TwinActionPermissionEntity::getId, TwinActionPermissionEntity::getTwinClassId);
                for (TwinClassEntity twinClassEntity : needLoadByPermissions.values()) {
                    twinClassEntity.setActionsProtectedByPermission(new Kit<>(actionGroupedByClass.getGrouped(twinClassEntity.getId()), TwinActionPermissionEntity::getTwinAction));
                }
            }
        }
        if (!needLoadByValidators.isEmpty()) {
            List<TwinActionValidatorEntity> twinClassActionValidatorEntities = twinActionValidatorRepository.findByTwinClassIdIn(needLoadByPermissions.keySet());
            if (!CollectionUtils.isEmpty(twinClassActionValidatorEntities)) {
                KitGrouped<TwinActionValidatorEntity, UUID, UUID> actionGroupedByClass = new KitGrouped<>(twinClassActionValidatorEntities, TwinActionValidatorEntity::getId, TwinActionValidatorEntity::getTwinClassId);
                for (TwinClassEntity twinClassEntity : needLoadByValidators.values()) {
                    twinClassEntity.setActionsProtectedByValidator(new KitGrouped<>(actionGroupedByClass.getGrouped(twinClassEntity.getId()), TwinActionValidatorEntity::getId, TwinActionValidatorEntity::getTwinAction));
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
                    TwinActionPermissionEntity classActionPermissionEntity = twinClassEntity.getActionsProtectedByPermission().get(twinAction);
                    if (classActionPermissionEntity != null) {
                        permissionDetectKeys = permissionService.convertToDetectKeys(entry.getValue()); // extract all permission check variants
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
                    for (TwinActionValidatorEntity classActionValidatorEntity : twinClassEntity.getActionsProtectedByValidator().getGrouped(twinAction)) { // looping permissions by current class
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

    public void checkAllowed(TwinEntity twinEntity, TwinAction action) throws ServiceException {
        if (!isAllowed(twinEntity, action))
            throw new ServiceException(ErrorCodeTwins.TWIN_ACTION_NOT_AVAILABLE, "The action[" + action.name() + "] not available for " + twinEntity.logNormal());
    }

    public boolean isAllowed(TwinEntity twinEntity, TwinAction action) throws ServiceException {
        loadActions(twinEntity);
        return twinEntity.getActions().contains(action);
    }
}
