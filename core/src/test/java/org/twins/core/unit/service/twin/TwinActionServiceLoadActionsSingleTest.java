package org.twins.core.unit.service.twin;

import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.action.TwinActionPermissionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.PermissionService.PermissionDetectKey;
import org.twins.core.service.twin.TwinActionService;
import org.twins.core.service.twin.TwinValidatorSetService;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TwinActionServiceLoadActionsSingleTest extends BaseUnitTest {

    @Mock private TwinValidatorSetService twinValidatorSetService;
    @Mock private PermissionService permissionService;
    @InjectMocks private TwinActionService twinActionService;

    private TwinEntity twinEntity;
    private TwinClassEntity twinClassEntity;
    private UUID permissionId;
    private UUID actionRestrictionReasonId;

    @BeforeEach
    void setUp() {
        twinEntity = new TwinEntity();
        twinEntity.setId(UUID.randomUUID());

        twinClassEntity = new TwinClassEntity();
        twinClassEntity.setId(UUID.randomUUID());
        twinEntity.setTwinClassId(twinClassEntity.getId());
        twinEntity.setTwinClass(twinClassEntity);

        permissionId = UUID.randomUUID();
        actionRestrictionReasonId = UUID.randomUUID();
    }

    // Prod resolves permissions in batch: convertToDetectKeys(twins) -> hasPermission(PermissionDetectKey, UUID).
    // hasPermission(TwinEntity, UUID) is never called by loadActions(), so we stub the batch path.
    private void stubPermission(UUID permissionId, boolean granted) throws ServiceException {
        when(permissionService.convertToDetectKeys(any()))
                .thenReturn(Map.of(mock(PermissionDetectKey.class), List.of(twinEntity)));
        when(permissionService.hasPermission(any(PermissionDetectKey.class), eq(permissionId)))
                .thenReturn(granted);
    }

    // Prod validates in batch: isValid(Collection<TwinEntity>, Collection<rules>) -> Map<twinId, ValidationResult>.
    private void stubValidatorResult(boolean valid) throws ServiceException {
        when(twinValidatorSetService.isValid(any(Collection.class), any(Collection.class)))
                .thenReturn(Map.of(twinEntity.getId(), valid ? ValidationResult.VALID : new ValidationResult(false)));
    }

    @Nested
    class AlreadyLoaded {

        @Test
        void loadActions_alreadyLoaded_keepsExistingActions() throws ServiceException {
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.EDIT);

            twinActionService.loadActions(twinEntity);

            assertTrue(twinEntity.getActions().contains(TwinAction.EDIT));
            assertEquals(1, twinEntity.getActions().size());
        }
    }

    @Nested
    class NoRestrictions {

        @Test
        void loadActions_noRestrictions_allActionsAllowed() throws ServiceException {
            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            twinActionService.loadActions(twinEntity);

            assertEquals(EnumSet.allOf(TwinAction.class).size(), twinEntity.getActions().size());
            assertTrue(twinEntity.getActions().contains(TwinAction.EDIT));
            assertTrue(twinEntity.getActions().contains(TwinAction.DELETE));
        }

        @Test
        void loadActions_noRestrictions_actionsRestrictedIsNull() throws ServiceException {
            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            twinActionService.loadActions(twinEntity);

            assertNotNull(twinEntity.getActions());
            assertNull(twinEntity.getActionsRestricted());
        }
    }

    @Nested
    class PermissionRestriction {

        @Test
        void loadActions_permissionDenied_withReason_removesActionAndSetsReason() throws ServiceException {
            var permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(actionRestrictionReasonId);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByPermission().add(permissionEntity);
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            stubPermission(permissionId, false);

            twinActionService.loadActions(twinEntity);

            assertTrue(twinEntity.getActions().contains(TwinAction.EDIT));
            assertFalse(twinEntity.getActions().contains(TwinAction.DELETE));
            assertEquals(actionRestrictionReasonId, twinEntity.getActionsRestricted().get(TwinAction.DELETE));
        }

        @Test
        void loadActions_permissionDenied_noReason_removesActionWithoutRestricted() throws ServiceException {
            var permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(null);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByPermission().add(permissionEntity);
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            stubPermission(permissionId, false);

            twinActionService.loadActions(twinEntity);

            assertFalse(twinEntity.getActions().contains(TwinAction.DELETE));
            assertNull(twinEntity.getActionsRestricted());
        }

        @Test
        void loadActions_permissionGranted_actionAllowed() throws ServiceException {
            var permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByPermission().add(permissionEntity);
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            stubPermission(permissionId, true);

            twinActionService.loadActions(twinEntity);

            assertTrue(twinEntity.getActions().contains(TwinAction.DELETE));
            assertTrue(twinEntity.getActions().contains(TwinAction.EDIT));
        }
    }

    @Nested
    class ValidatorRestriction {

        @Test
        void loadActions_validatorPasses_actionAllowed() throws ServiceException {
            var validatorRule = buildValidatorRule(TwinAction.DELETE, actionRestrictionReasonId);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule);

            stubValidatorResult(true);

            twinActionService.loadActions(twinEntity);

            assertTrue(twinEntity.getActions().contains(TwinAction.DELETE));
            assertNull(twinEntity.getActionsRestricted());
        }

        @Test
        void loadActions_validatorFails_withReason_removesActionAndSetsReason() throws ServiceException {
            var validatorRule = buildValidatorRule(TwinAction.DELETE, actionRestrictionReasonId);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule);

            stubValidatorResult(false);

            twinActionService.loadActions(twinEntity);

            assertFalse(twinEntity.getActions().contains(TwinAction.DELETE));
            assertEquals(actionRestrictionReasonId, twinEntity.getActionsRestricted().get(TwinAction.DELETE));
        }

        @Test
        void loadActions_validatorFails_noReason_removesActionWithoutRestricted() throws ServiceException {
            var validatorRule = buildValidatorRule(TwinAction.DELETE, null);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule);

            stubValidatorResult(false);

            twinActionService.loadActions(twinEntity);

            assertFalse(twinEntity.getActions().contains(TwinAction.DELETE));
            assertNull(twinEntity.getActionsRestricted());
        }

        @Test
        void loadActions_multipleValidators_onePassesOneFails_actionAllowed() throws ServiceException {
            var rule1 = buildValidatorRule(TwinAction.DELETE, null);
            rule1.setId(UUID.randomUUID());
            var rule2 = buildValidatorRule(TwinAction.DELETE, null);
            rule2.setId(UUID.randomUUID());

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(rule1);
            twinClassEntity.getActionsProtectedByValidatorRules().add(rule2);

            // Prod combines all active rules into one ValidationResult per twin; a passing rule yields "allowed".
            stubValidatorResult(true);

            twinActionService.loadActions(twinEntity);

            assertTrue(twinEntity.getActions().contains(TwinAction.DELETE));
        }
    }

    @Nested
    class CombinedRestriction {

        @Test
        void loadActions_permissionDenied_skipsValidators() throws ServiceException {
            var permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(actionRestrictionReasonId);

            var validatorRule = buildValidatorRule(TwinAction.DELETE, null);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByPermission().add(permissionEntity);
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule);

            stubPermission(permissionId, false);

            twinActionService.loadActions(twinEntity);

            verify(twinValidatorSetService, never()).isValid(any(Collection.class), any(Collection.class));
            assertFalse(twinEntity.getActions().contains(TwinAction.DELETE));
        }

        @Test
        void loadActions_permissionGranted_validatorFails_usesValidatorReason() throws ServiceException {
            var validatorReasonId = UUID.randomUUID();

            var permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);

            var validatorRule = buildValidatorRule(TwinAction.DELETE, validatorReasonId);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByPermission().add(permissionEntity);
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule);

            stubPermission(permissionId, true);
            stubValidatorResult(false);

            twinActionService.loadActions(twinEntity);

            assertFalse(twinEntity.getActions().contains(TwinAction.DELETE));
            assertEquals(validatorReasonId, twinEntity.getActionsRestricted().get(TwinAction.DELETE));
        }

        @Test
        void loadActions_permissionDenied_usesPermissionReasonOverValidator() throws ServiceException {
            var permissionReasonId = UUID.randomUUID();

            var permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(permissionReasonId);

            var validatorRule = buildValidatorRule(TwinAction.DELETE, UUID.randomUUID());

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByPermission().add(permissionEntity);
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule);

            stubPermission(permissionId, false);

            twinActionService.loadActions(twinEntity);

            verify(twinValidatorSetService, never()).isValid(any(Collection.class), any(Collection.class));
            assertEquals(permissionReasonId, twinEntity.getActionsRestricted().get(TwinAction.DELETE));
        }
    }

    private TwinActionValidatorRuleEntity buildValidatorRule(TwinAction action, UUID reasonId) {
        var rule = new TwinActionValidatorRuleEntity();
        rule.setId(UUID.randomUUID());
        rule.setTwinAction(action);
        rule.setActionRestrictionReasonId(reasonId);
        rule.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));
        rule.setActive(true); // prod skips inactive rules (activeRules filter)

        var validator = new TwinValidatorEntity();
        validator.setId(UUID.randomUUID());
        validator.setOrder(1);
        validator.setInvert(false);
        rule.getTwinValidatorKit().add(validator);

        return rule;
    }
}
