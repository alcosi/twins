package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.kit.KitGroupedObj;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.action.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinValidatorEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.exception.ErrorCodeTwins;
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
    final TwinActionPermissionRepository twinActionPermissionRepository;
    final TwinActionValidatorRepository twinActionValidatorRepository;
    private final EntitySmartService entitySmartService;
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
            for (TwinActionValidatorEntity twinActionValidatorEntity : twinEntity.getTwinClass().getActionsProtectedByValidator().getGrouped(twinAction)) {
                boolean isValid = true;
                for (TwinValidatorEntity twinValidatorEntity : twinActionValidatorEntity.getTwinValidators()) {
                    if (!twinValidatorEntity.isActive()) {
                        log.info(twinValidatorEntity.logShort() + " from " + twinActionValidatorEntity.logShort() + " is inactive");
                        continue;
                    }
                    TwinValidator twinValidator = featurerService.getFeaturer(twinValidatorEntity.getTwinValidatorFeaturer(), TwinValidator.class);
                    TwinValidator.ValidationResult validationResult = twinValidator.isValid(twinValidatorEntity.getTwinValidatorParams(), twinEntity, twinValidatorEntity.isInvert());
                    if (!validationResult.isValid()) {
                        log.error(validationResult.getMessage());
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    twinEntity.getActions().add(twinAction);
                    break;
                }
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
            KitGrouped<TwinActionPermissionEntity, UUID, UUID> actionGroupedByClass = new KitGrouped<>(twinClassActionPermissionEntities, TwinActionPermissionEntity::getId, TwinActionPermissionEntity::getTwinClassId);
            for (TwinClassEntity twinClassEntity : needLoadByPermissions.values()) {
                twinClassEntity.setActionsProtectedByPermission(new Kit<>(actionGroupedByClass.getGrouped(twinClassEntity.getId()), TwinActionPermissionEntity::getTwinAction));
            }
        }
        if (!needLoadByValidators.isEmpty()) {
            List<TwinActionValidatorEntity> twinClassActionValidatorEntities = twinActionValidatorRepository.findByTwinClassIdIn(needLoadByPermissions.keySet());
            KitGrouped<TwinActionValidatorEntity, UUID, UUID> actionGroupedByClass = new KitGrouped<>(twinClassActionValidatorEntities, TwinActionValidatorEntity::getId, TwinActionValidatorEntity::getTwinClassId);
            for (TwinClassEntity twinClassEntity : needLoadByValidators.values()) {
                twinClassEntity.setActionsProtectedByValidator(new KitGrouped<>(actionGroupedByClass.getGrouped(twinClassEntity.getId()), TwinActionValidatorEntity::getId, TwinActionValidatorEntity::getTwinAction));
            }
        }
    }

    public void loadActions(Collection<TwinEntity> twinEntityList) throws ServiceException {
        List<TwinEntity> needLoad = new ArrayList<>();
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getActions() == null)
                needLoad.add(twinEntity);
        if (needLoad.isEmpty())
            return;
        KitGroupedObj<TwinEntity, UUID, UUID, TwinClassEntity> groupedByClass = new KitGroupedObj<>(needLoad, TwinEntity::getId, TwinEntity::getTwinClassId, TwinEntity::getTwinClass);
        loadClassProtectedActions(groupedByClass.getGroupingObjectMap().values());
        Map<PermissionService.PermissionDetectKey, List<TwinEntity>> permissionDetectKeys;
        TwinClassEntity twinClassEntity;
        Map<UUID, Set<TwinAction>> twinsActionsForbiddenByPermissions = new HashMap<>();
        Map<UUID, Set<TwinAction>> twinsActionsForbiddenByValidators = new HashMap<>();
        for (Map.Entry<UUID, List<TwinEntity>> entry : groupedByClass.getGroupedMap().entrySet()) { // looping grouped by class
            List<TwinEntity> twinsNeedsValidatorCheck = new ArrayList<>();
            twinClassEntity = groupedByClass.getGroupingObject(entry.getKey());
            for (TwinAction twinAction : TwinAction.values()) {

                if (KitUtils.isNotEmpty(twinClassEntity.getActionsProtectedByPermission())) {
                    TwinActionPermissionEntity classActionPermissionEntity = twinClassEntity.getActionsProtectedByPermission().get(twinAction);
                    if (classActionPermissionEntity != null) {
                        permissionDetectKeys = permissionService.convertToDetectKeys(entry.getValue()); // extract all permission check variants
                        for (Map.Entry<PermissionService.PermissionDetectKey, List<TwinEntity>> samePermissionGroupEntry : permissionDetectKeys.entrySet()) { // looping detected keys
                            if (!permissionService.hasPermission(samePermissionGroupEntry.getKey(), classActionPermissionEntity.getPermissionId())) { // all twins linked to current key will have such action
                                for (TwinEntity twinEntity : samePermissionGroupEntry.getValue()) {
                                    twinsActionsForbiddenByPermissions.computeIfAbsent(twinEntity.getId(), k -> new HashSet<>());
                                    twinsActionsForbiddenByPermissions.get(twinEntity.getId()).add(twinAction);
                                }
                            } else {
                                twinsNeedsValidatorCheck.addAll(samePermissionGroupEntry.getValue());
                            }
                        }
                    }
                }
                if (KitUtils.isNotEmpty(twinClassEntity.getActionsProtectedByValidator())) {
                    for (TwinActionValidatorEntity actionValidatorEntity : twinClassEntity.getActionsProtectedByValidator().getGrouped(twinAction)) {
                        Map<UUID, Boolean> twinByTwinValidatorsIsValid = new HashMap<>();
                        for (TwinValidatorEntity twinValidatorEntity : actionValidatorEntity.getTwinValidators()) {
                            if (!twinValidatorEntity.isActive()) {
                                log.info(twinValidatorEntity.logShort() + " from " + actionValidatorEntity.logShort() + " is inactive");
                                continue;
                            }
                            TwinValidator twinValidator = featurerService.getFeaturer(twinValidatorEntity.getTwinValidatorFeaturer(), TwinValidator.class);
                            TwinValidator.CollectionValidationResult collectionValidationResult = twinValidator.isValid(twinValidatorEntity.getTwinValidatorParams(), twinsNeedsValidatorCheck, twinValidatorEntity.isInvert());
                            for (TwinEntity twinEntity : twinsNeedsValidatorCheck) {
                                TwinValidator.ValidationResult validationResult = collectionValidationResult.getTwinsResults().get(twinEntity.getId());
                                if (validationResult == null) {
                                    log.warn(twinValidatorEntity.logShort() + " from " + actionValidatorEntity.logShort() + " validation result should not be null");
                                    continue;
                                }
                                twinByTwinValidatorsIsValid.computeIfPresent(twinEntity.getId(), (k, v) -> v && validationResult.isValid());
                                twinByTwinValidatorsIsValid.putIfAbsent(twinEntity.getId(), validationResult.isValid());
                            }
                        }
                        List<TwinEntity> nextLoopTwins = new ArrayList<>();
                        for (TwinEntity twinEntity : twinsNeedsValidatorCheck) {
                            twinsActionsForbiddenByValidators.computeIfAbsent(twinEntity.getId(), (k) ->new HashSet<>());
                            twinsActionsForbiddenByValidators.get(twinEntity.getId()).add(twinAction);
                            if(!twinByTwinValidatorsIsValid.get(twinEntity.getId()))
                                nextLoopTwins.add(twinEntity);
                            else {
                                twinsActionsForbiddenByValidators.get(twinEntity.getId()).remove(twinAction);
                            }
                        }
                        twinsNeedsValidatorCheck = nextLoopTwins;
                    }
                }
            }
        }
        Set<TwinAction> forbiddenPermissionActions, forbiddenValidatorActions;
        for (TwinEntity twinEntity : needLoad) {
            twinEntity.setActions(new HashSet<>());
            forbiddenPermissionActions = twinsActionsForbiddenByPermissions.get(twinEntity.getId());
            forbiddenValidatorActions = twinsActionsForbiddenByValidators.get(twinEntity.getId());
            if (forbiddenPermissionActions == null && forbiddenValidatorActions == null)
                twinEntity.getActions().addAll(EnumSet.allOf(TwinAction.class));
            else {
                for(TwinAction action : EnumSet.allOf(TwinAction.class)) {
                    if((forbiddenValidatorActions == null || !forbiddenValidatorActions.contains(action)) && (forbiddenPermissionActions == null || !forbiddenPermissionActions.contains(action)))
                        twinEntity.getActions().add(action);
                }
            }
        }
    }

    public void checkAllowed(UUID twinId, TwinAction action) throws ServiceException {
        checkAllowed(entitySmartService.findById(twinId, twinRepository, EntitySmartService.FindMode.ifEmptyThrows), action);
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
