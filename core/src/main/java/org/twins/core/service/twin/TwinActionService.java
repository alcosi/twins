package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.kit.KitGroupedObj;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dao.action.TwinActionPermissionEntity;
import org.twins.core.dao.action.TwinActionPermissionRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleRepository;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.action.ActionRestrictionReasonService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.validator.TwinValidatorService;

import java.util.*;
import java.util.stream.Collectors;

@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinActionService {
    final TwinActionPermissionRepository twinActionPermissionRepository;
    final TwinActionValidatorRuleRepository twinActionValidatorRuleRepository;
    private final EntitySmartService entitySmartService;
    private final ActionRestrictionReasonService actionRestrictionReasonService;
    final TwinRepository twinRepository;
    @Lazy
    final TwinValidatorService twinValidatorService;
    @Lazy
    final TwinValidatorSetService twinValidatorSetService;
    @Lazy
    final PermissionService permissionService;

    //TODO update the method implementation similar to other load methods for singleton classes
    public void loadActions(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getActions() != null)
            return;
        loadClassProtectedActions(twinEntity.getTwinClass());
        if (twinEntity.getTwinClass().getActionsProtectedByPermission().isEmpty() && twinEntity.getTwinClass().getActionsProtectedByValidatorRules().isEmpty()) {
            twinEntity.setActions(EnumSet.allOf(TwinAction.class));
            return;
        }
        twinEntity.setActions(new HashSet<>());
        Map<TwinAction, UUID> actionsRestricted = new HashMap<>();

        for (TwinAction twinAction : TwinAction.values()) {
            TwinActionPermissionEntity twinActionProtectedByPermission = twinEntity.getTwinClass().getActionsProtectedByPermission().get(twinAction);

            if (twinActionProtectedByPermission != null) {
                if (!permissionService.hasPermission(twinEntity, twinActionProtectedByPermission.getPermissionId())) {
                    if (twinActionProtectedByPermission.getActionRestrictionReasonId() != null) {
                        actionsRestricted.put(twinAction, twinActionProtectedByPermission.getActionRestrictionReasonId());
                    }
                    continue; // forbidden by permission, skip validators
                }
            }

            if (CollectionUtils.isEmpty(twinEntity.getTwinClass().getActionsProtectedByValidatorRules().getGrouped(twinAction))) {
                twinEntity.getActions().add(twinAction);
                continue;
            }

            // Check validators - action is allowed if ANY validator passes
            boolean allowedByValidator = false;
            UUID validatorRestrictionReasonId = null;
            for (TwinActionValidatorRuleEntity twinActionValidatorRuleEntity : twinEntity.getTwinClass().getActionsProtectedByValidatorRules().getGrouped(twinAction)) {
                boolean isValid = twinValidatorSetService.isValid(twinEntity, twinActionValidatorRuleEntity);
                if (isValid) {
                    allowedByValidator = true;
                    break; // at least one validator passed
                } else {
                    if (validatorRestrictionReasonId == null && twinActionValidatorRuleEntity.getActionRestrictionReasonId() != null) {
                        validatorRestrictionReasonId = twinActionValidatorRuleEntity.getActionRestrictionReasonId();
                    }
                }
            }

            if (allowedByValidator) {
                twinEntity.getActions().add(twinAction);
            } else if (validatorRestrictionReasonId != null) {
                actionsRestricted.put(twinAction, validatorRestrictionReasonId);
            }
        }

        // Set actionsRestricted if not empty
        if (!actionsRestricted.isEmpty()) {
            twinEntity.setActionsRestricted(actionsRestricted);
        }
    }

    public void loadActionRestrictionReasons(TwinEntity twinEntity) throws ServiceException {
        loadActionRestrictionReasons(Collections.singletonList(twinEntity));
    }

    public void loadActionRestrictionReasons(Collection<TwinEntity> twinEntityList) throws ServiceException {
        List<TwinEntity> needLoad = new ArrayList<>();
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getActionsRestrictedReasons() == null && twinEntity.getActionsRestricted() != null && !twinEntity.getActionsRestricted().isEmpty())
                needLoad.add(twinEntity);
        if (needLoad.isEmpty())
            return;

        Set<UUID> allReasonIds = new HashSet<>();
        for (TwinEntity twinEntity : needLoad) {
            allReasonIds.addAll(twinEntity.getActionsRestricted().values());
        }

        if (allReasonIds.isEmpty())
            return;

        Kit<ActionRestrictionReasonEntity, UUID> reasonsKit = actionRestrictionReasonService.findEntitiesSafe(allReasonIds);

        for (TwinEntity twinEntity : needLoad) {
            Map<TwinAction, ActionRestrictionReasonEntity> actionsRestrictedReasons = new HashMap<>();
            for (Map.Entry<TwinAction, UUID> entry : twinEntity.getActionsRestricted().entrySet()) {
                ActionRestrictionReasonEntity reason = reasonsKit.get(entry.getValue());
                if (reason != null) {
                    actionsRestrictedReasons.put(entry.getKey(), reason);
                }
            }
            if (!actionsRestrictedReasons.isEmpty()) {
                twinEntity.setActionsRestrictedReasons(actionsRestrictedReasons);
            }
        }
    }

    public void loadClassProtectedActions(TwinClassEntity twinClassEntity) throws ServiceException {
        loadClassProtectedActions(Collections.singletonList(twinClassEntity));
    }

    public void loadClassProtectedActions(Collection<TwinClassEntity> srcCollection) throws ServiceException {
        List<TwinClassEntity> needLoadByPermissions = new ArrayList<>();
        List<TwinClassEntity> needLoadByValidators = new ArrayList<>();
        Set<UUID> needLoadByPermissionsClassIds = new HashSet<>();
        Set<UUID> needLoadBysValidatorsClassIds = new HashSet<>();
        for (TwinClassEntity twinClassEntity : srcCollection) {
            if (twinClassEntity.getActionsProtectedByPermission() == null) {
                needLoadByPermissionsClassIds.addAll(twinClassEntity.getExtendedClassIdSet()); // rules are inherited
                needLoadByPermissions.add(twinClassEntity);
            }
            if (twinClassEntity.getActionsProtectedByValidatorRules() == null) {
                needLoadBysValidatorsClassIds.addAll(twinClassEntity.getExtendedClassIdSet()); // rules are inherited
                needLoadByValidators.add(twinClassEntity);
            }
        }
        needLoadByPermissionsClassIds.remove(SystemEntityService.TWIN_CLASS_GLOBAL_ANCESTOR);
        if (!needLoadByPermissions.isEmpty()) {
            List<TwinActionPermissionEntity> twinClassActionPermissionEntities = twinActionPermissionRepository.findByTwinClassIdIn(needLoadByPermissionsClassIds);
            Map<TwinAction, Map<UUID, TwinActionPermissionEntity>> groupedByActionThenByClass = new HashMap<>();
            for (var twinActionPermissionEntity : twinClassActionPermissionEntities) {
                groupedByActionThenByClass.computeIfAbsent(twinActionPermissionEntity.getTwinAction(), k -> new HashMap<>()).put(twinActionPermissionEntity.getTwinClassId(), twinActionPermissionEntity);
            }
            for (TwinClassEntity twinClassEntity : needLoadByPermissions) {
                twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
                for (var action : TwinAction.values()) {
                    if (twinClassEntity.getActionsProtectedByPermission().get(action) != null)
                        continue; // protectedByPermission is already detected
                    if (groupedByActionThenByClass.get(action) == null) {
                        continue; // protectedByPermission is not configured to any class
                    }
                    for (var extendsClassId : twinClassEntity.getExtendedClassIdSet()) { // set order is important
                        var actionPermission = groupedByActionThenByClass.get(action).get(extendsClassId);
                        if (actionPermission != null) {
                            twinClassEntity.getActionsProtectedByPermission().add(actionPermission);
                            break; // no need to go deeper
                        }
                    }
                }
            }
        }
        if (!needLoadByValidators.isEmpty()) {
            List<TwinActionValidatorRuleEntity> twinClassActionValidatorEntities = twinActionValidatorRuleRepository.findByTwinClassIdIn(needLoadBysValidatorsClassIds);
            twinValidatorService.loadValidators(twinClassActionValidatorEntities);
            Map<TwinAction, Map<UUID, TwinActionValidatorRuleEntity>> groupedByActionThenByClass = new HashMap<>();
            for (var twinActionValidatorRuleEntity : twinClassActionValidatorEntities) {
                groupedByActionThenByClass.computeIfAbsent(twinActionValidatorRuleEntity.getTwinAction(), k -> new HashMap<>()).put(twinActionValidatorRuleEntity.getTwinClassId(), twinActionValidatorRuleEntity);
            }
            for (TwinClassEntity twinClassEntity : needLoadByValidators) {
                twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
                for (var action : TwinAction.values()) {
                    if (CollectionUtils.isNotEmpty(twinClassEntity.getActionsProtectedByValidatorRules().getGrouped(action)))
                        continue; // protectedByValidator is already detected
                    if (groupedByActionThenByClass.get(action) == null) {
                        continue; // protectedByValidator is not configured to any class
                    }
                    for (var extendsClassId : twinClassEntity.getExtendedClassIdSet()) { // set order is important
                        var actionPermission = groupedByActionThenByClass.get(action).get(extendsClassId);
                        if (actionPermission != null) {
                            twinClassEntity.getActionsProtectedByValidatorRules().add(actionPermission);
                            break; // no need to go deeper
                        }
                    }
                }
            }
        }
    }

    /**
     * This method loads available actions for each `TwinEntity` object from the provided `twinEntityList`.
     * For each entity, it checks for permission-based and validator-based restrictions on actions.
     * If no restrictions exist for an entity, it is assigned all possible actions.
     * Otherwise, only actions that are not restricted by either permissions or validators are assigned.
     * The processing is grouped by the entity's class (`TwinClassEntity`).
     *
     * @param twinEntityList collection of `TwinEntity` objects for which actions need to be loaded.
     * @throws ServiceException if an error occurs during permission or validation loading.
     */
    public void loadActions(Collection<TwinEntity> twinEntityList) throws ServiceException {
        // List of entities that need action loading (those with null actions)
        List<TwinEntity> needLoad = twinEntityList.stream()
                .filter(t -> t.getActions() == null)
                .toList();
        // If there are no entities requiring action loading, exit the method
        if (needLoad.isEmpty())
            return;

        // Group TwinEntity objects by class, so permissions and validators can be processed by class
        KitGroupedObj<TwinEntity, UUID, UUID, TwinClassEntity> groupedByClass = new KitGroupedObj<>(needLoad, TwinEntity::getId, TwinEntity::getTwinClassId, TwinEntity::getTwinClass);
        // Load class-protected actions for all involved classes
        loadClassProtectedActions(groupedByClass.getGroupingObjectMap().values());

        // Map for storing restriction reasons by twinId and action (null = forbidden without reason, UUID = forbidden with reason)
        Map<UUID, Map<TwinAction, UUID>> twinsActionsRestrictionReasons = new HashMap<>();

        for (Map.Entry<UUID, List<TwinEntity>> entry : groupedByClass.getGroupedMap().entrySet()) {
            TwinClassEntity twinClassEntity = groupedByClass.getGroupingObject(entry.getKey());
            // Check each possible action (TwinAction) for permission and validator protection
            for (TwinAction twinAction : TwinAction.values()) {
                // List of entities needing validator checks FOR THIS ACTION
                List<TwinEntity> twinsNeedsValidatorCheck = checkPermissionRestrictions(
                        entry.getValue(), twinClassEntity, twinAction, twinsActionsRestrictionReasons);
                checkValidatorRestrictions(twinsNeedsValidatorCheck, twinClassEntity, twinAction, twinsActionsRestrictionReasons);
            }
        }

        // Set allowed actions for each TwinEntity based on permissions and validator results
        needLoad.forEach(twin -> setActionsForTwin(twin, twinsActionsRestrictionReasons.get(twin.getId())));
    }

    /**
     * Checks permission restrictions for an action and returns list of twins that need validator checks.
     *
     * @param twins list of twins to check permissions for
     * @param twinClassEntity the twin class containing permission rules
     * @param twinAction the action to check
     * @param twinsActionsRestrictionReasons map to store restriction reasons
     * @return list of twins that passed permission check and need validator validation
     * @throws ServiceException if permission check fails
     */
    private List<TwinEntity> checkPermissionRestrictions(
            List<TwinEntity> twins,
            TwinClassEntity twinClassEntity,
            TwinAction twinAction,
            Map<UUID, Map<TwinAction, UUID>> twinsActionsRestrictionReasons) throws ServiceException {
        // Check if the action is protected by permissions
        TwinActionPermissionEntity permissionEntity = KitUtils.getOrNull(twinClassEntity.getActionsProtectedByPermission(), twinAction);
        if (permissionEntity == null) {
            // Action is not protected by permission, all twins need validator check
            return twins;
        }

        // Convert entities into permission check keys
        List<TwinEntity> twinsNeedsValidatorCheck = new ArrayList<>();
        Map<PermissionService.PermissionDetectKey, List<TwinEntity>> permissionDetectKeys = permissionService.convertToDetectKeys(twins);

        // Loop through permission check keys and verify permission
        for (Map.Entry<PermissionService.PermissionDetectKey, List<TwinEntity>> keyEntry : permissionDetectKeys.entrySet()) {
            // If the permission is denied for an action, mark the action as forbidden for those entities
            if (!permissionService.hasPermission(keyEntry.getKey(), permissionEntity.getPermissionId())) {
                for (TwinEntity twin : keyEntry.getValue()) {
                    twinsActionsRestrictionReasons.computeIfAbsent(twin.getId(), k -> new HashMap<>());
                    // Only set if not already set (permission priority over validator)
                    if (!twinsActionsRestrictionReasons.get(twin.getId()).containsKey(twinAction)) {
                        twinsActionsRestrictionReasons.get(twin.getId()).put(twinAction, permissionEntity.getActionRestrictionReasonId());
                    }
                }
            } else {
                twinsNeedsValidatorCheck.addAll(keyEntry.getValue()); // If permission is granted, add to validator check list
            }
        }
        return twinsNeedsValidatorCheck;
    }

    /**
     * Checks validator restrictions for an action.
     * Twins are validated against rules sequentially; if a rule passes, the action is allowed for that twin.
     *
     * @param twins list of twins to validate
     * @param twinClassEntity the twin class containing validator rules
     * @param twinAction the action to check
     * @param twinsActionsRestrictionReasons map to store/update restriction reasons
     * @throws ServiceException if validation fails
     */
    private void checkValidatorRestrictions(
            List<TwinEntity> twins,
            TwinClassEntity twinClassEntity,
            TwinAction twinAction,
            Map<UUID, Map<TwinAction, UUID>> twinsActionsRestrictionReasons) throws ServiceException {
        if (CollectionUtils.isEmpty(twins) || KitUtils.isEmpty(twinClassEntity.getActionsProtectedByValidatorRules())) {
            return;
        }

        List<TwinActionValidatorRuleEntity> validatorRules = twinClassEntity.getActionsProtectedByValidatorRules().getGrouped(twinAction);
        if (CollectionUtils.isEmpty(validatorRules)) {
            return;
        }

        List<TwinActionValidatorRuleEntity> activeRules = validatorRules.stream().filter(TwinActionValidatorRuleEntity::isActive).toList();

        if (activeRules.isEmpty()) {
            return;
        }

        UUID restrictionReasonId = activeRules.getFirst().getActionRestrictionReasonId();
        Map<UUID, ValidationResult> results = twinValidatorSetService.isValid(twins, activeRules);

        for (TwinEntity twin : twins) {
            ValidationResult result = results.get(twin.getId());
            if (result.isValid()) {
                Map<TwinAction, UUID> restrictions = twinsActionsRestrictionReasons.get(twin.getId());
                if (restrictions != null) {
                    restrictions.remove(twinAction);
                    if (restrictions.isEmpty()) {
                        log.info("Action {} ALLOWED for {}", twinAction, twin.logShort());
                        twinsActionsRestrictionReasons.remove(twin.getId());
                    }
                }
            } else {
                if (!twinsActionsRestrictionReasons.computeIfAbsent(twin.getId(), k -> new HashMap<>()).containsKey(twinAction)) {
                    log.info("Action {} RESTRICTED for {}, reason: {}", twinAction, twin.logShort(), restrictionReasonId);
                    twinsActionsRestrictionReasons.get(twin.getId()).put(twinAction, restrictionReasonId);
                }
            }
        }
    }

    /**
     * Sets allowed actions for a twin based on restriction reasons.
     *
     * @param twin the twin to set actions for
     * @param restrictionReasons map of restricted actions with their reason IDs (null = no restrictions)
     */
    private void setActionsForTwin(TwinEntity twin, Map<TwinAction, UUID> restrictionReasons) {
        // If no forbidden actions, add all possible actions
        if (restrictionReasons == null || restrictionReasons.isEmpty()) {
            twin.setActions(EnumSet.allOf(TwinAction.class));
            return;
        }

        // Add only those actions that are not in the restriction map
        Set<TwinAction> allowedActions = EnumSet.allOf(TwinAction.class).stream()
                .filter(action -> !restrictionReasons.containsKey(action))
                .collect(Collectors.toSet());
        twin.setActions(allowedActions);

        // Only add to actionsRestricted if there's a reason
        Map<TwinAction, UUID> actionsRestricted = restrictionReasons.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (!actionsRestricted.isEmpty()) {
            twin.setActionsRestricted(actionsRestricted);
        }
    }

    public void checkAllowed(UUID twinId, TwinAction action) throws ServiceException {
        checkAllowed(entitySmartService.findById(twinId, twinRepository, EntitySmartService.FindMode.ifEmptyThrows), action);
    }

    public void checkAllowed(TwinEntity twinEntity, TwinAction action) throws ServiceException {
        if (!isAllowed(twinEntity, action))
            throw new ServiceException(ErrorCodeTwins.TWIN_ACTION_NOT_AVAILABLE, "The action[" + action.name() + "] not available for " + twinEntity.logNormal());
    }

    public void checkAllowed(Collection<TwinEntity> twinEntities, TwinAction action) throws ServiceException {
        loadActions(twinEntities);
        for (TwinEntity twinEntity : twinEntities) {
            if (!twinEntity.getActions().contains(action))
                throw new ServiceException(ErrorCodeTwins.TWIN_ACTION_NOT_AVAILABLE, "The action[" + action.name() + "] not available for " + twinEntity.logNormal());
        }
    }

    public boolean isAllowed(TwinEntity twinEntity, TwinAction action) throws ServiceException {
        loadActions(twinEntity);
        return twinEntity.getActions().contains(action);
    }
}
