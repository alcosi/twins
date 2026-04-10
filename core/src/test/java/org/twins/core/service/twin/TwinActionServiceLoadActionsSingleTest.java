package org.twins.core.service.twin;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.action.TwinActionPermissionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.service.permission.PermissionService;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwinActionServiceLoadActionsSingleTest {

    @Mock
    private TwinValidatorSetService twinValidatorSetService;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private TwinActionService twinActionService;

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

    @Nested
    class AlreadyLoadedTests {
        @Test
        void testLoadActions_AlreadyLoaded() throws ServiceException {
            // Given
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.EDIT);

            // When
            twinActionService.loadActions(twinEntity);

            // Then - should return early, actions should remain unchanged
            assertTrue(twinEntity.getActions().contains(TwinAction.EDIT));
            assertEquals(1, twinEntity.getActions().size());
        }
    }

    @Nested
    class NoRestrictionsTests {
        @Test
        void testLoadActions_NoRestrictions_AllActionsAllowed() throws ServiceException {
            // Given
            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            // When
            twinActionService.loadActions(twinEntity);

            // Then
            assertNotNull(twinEntity.getActions());
            assertEquals(EnumSet.allOf(TwinAction.class).size(), twinEntity.getActions().size());
            assertTrue(twinEntity.getActions().contains(TwinAction.EDIT));
            assertTrue(twinEntity.getActions().contains(TwinAction.DELETE));
        }

        @Test
        void testLoadActions_NoRestrictions_ActionsRestrictedIsNull() throws ServiceException {
            // Given
            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            // When
            twinActionService.loadActions(twinEntity);

            // Then
            assertNotNull(twinEntity.getActions());
            assertNull(twinEntity.getActionsRestricted());
        }
    }

    @Nested
    class PermissionRestrictionTests {
        @Test
        void testLoadActions_PermissionDenied_WithReason() throws ServiceException {
            // Given
            TwinActionPermissionEntity permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(actionRestrictionReasonId);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByPermission().add(permissionEntity);
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            when(permissionService.hasPermission(twinEntity, permissionId)).thenReturn(false);

            // When
            twinActionService.loadActions(twinEntity);

            // Then
            assertTrue(twinEntity.getActions().contains(TwinAction.EDIT));
            assertFalse(twinEntity.getActions().contains(TwinAction.DELETE));
            assertNotNull(twinEntity.getActionsRestricted());
            assertEquals(actionRestrictionReasonId, twinEntity.getActionsRestricted().get(TwinAction.DELETE));
        }

        @Test
        void testLoadActions_PermissionDenied_NoReason() throws ServiceException {
            // Given
            TwinActionPermissionEntity permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(null);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByPermission().add(permissionEntity);
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            when(permissionService.hasPermission(twinEntity, permissionId)).thenReturn(false);

            // When
            twinActionService.loadActions(twinEntity);

            // Then - DELETE should not be in actions, and not in actionsRestricted (no reason)
            assertTrue(twinEntity.getActions().contains(TwinAction.EDIT));
            assertFalse(twinEntity.getActions().contains(TwinAction.DELETE));
            assertNull(twinEntity.getActionsRestricted());
        }

        @Test
        void testLoadActions_PermissionGranted() throws ServiceException {
            // Given
            TwinActionPermissionEntity permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByPermission().add(permissionEntity);
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            when(permissionService.hasPermission(twinEntity, permissionId)).thenReturn(true);

            // When
            twinActionService.loadActions(twinEntity);

            // Then
            assertTrue(twinEntity.getActions().contains(TwinAction.DELETE));
            assertTrue(twinEntity.getActions().contains(TwinAction.EDIT));
        }
    }

    @Nested
    class ValidatorRestrictionTests {
        @Test
        void testLoadActions_ValidatorPasses() throws ServiceException {
            // Given
            TwinActionValidatorRuleEntity validatorRule = new TwinActionValidatorRuleEntity();
            validatorRule.setTwinAction(TwinAction.DELETE);
            validatorRule.setActionRestrictionReasonId(actionRestrictionReasonId);
            validatorRule.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));

            TwinValidatorEntity twinValidatorEntity = new TwinValidatorEntity();
            twinValidatorEntity.setId(UUID.randomUUID());
            twinValidatorEntity.setOrder(1);
            twinValidatorEntity.setInvert(false);
            validatorRule.getTwinValidatorKit().add(twinValidatorEntity);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule);

            when(twinValidatorSetService.isValid(twinEntity, validatorRule)).thenReturn(true);

            // When
            twinActionService.loadActions(twinEntity);

            // Then
            assertTrue(twinEntity.getActions().contains(TwinAction.DELETE));
            assertNull(twinEntity.getActionsRestricted());
        }

        @Test
        void testLoadActions_ValidatorFails_WithReason() throws ServiceException {
            // Given
            TwinActionValidatorRuleEntity validatorRule = new TwinActionValidatorRuleEntity();
            validatorRule.setTwinAction(TwinAction.DELETE);
            validatorRule.setActionRestrictionReasonId(actionRestrictionReasonId);
            validatorRule.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));

            TwinValidatorEntity twinValidatorEntity = new TwinValidatorEntity();
            twinValidatorEntity.setId(UUID.randomUUID());
            twinValidatorEntity.setOrder(1);
            twinValidatorEntity.setInvert(false);
            validatorRule.getTwinValidatorKit().add(twinValidatorEntity);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule);

            when(twinValidatorSetService.isValid(twinEntity, validatorRule)).thenReturn(false);

            // When
            twinActionService.loadActions(twinEntity);

            // Then
            assertFalse(twinEntity.getActions().contains(TwinAction.DELETE));
            assertNotNull(twinEntity.getActionsRestricted());
            assertEquals(actionRestrictionReasonId, twinEntity.getActionsRestricted().get(TwinAction.DELETE));
        }

        @Test
        void testLoadActions_ValidatorFails_NoReason() throws ServiceException {
            // Given
            TwinActionValidatorRuleEntity validatorRule = new TwinActionValidatorRuleEntity();
            validatorRule.setTwinAction(TwinAction.DELETE);
            validatorRule.setActionRestrictionReasonId(null);
            validatorRule.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));

            TwinValidatorEntity twinValidatorEntity = new TwinValidatorEntity();
            twinValidatorEntity.setId(UUID.randomUUID());
            twinValidatorEntity.setOrder(1);
            twinValidatorEntity.setInvert(false);
            validatorRule.getTwinValidatorKit().add(twinValidatorEntity);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule);

            when(twinValidatorSetService.isValid(twinEntity, validatorRule)).thenReturn(false);

            // When
            twinActionService.loadActions(twinEntity);

            // Then
            assertFalse(twinEntity.getActions().contains(TwinAction.DELETE));
            assertNull(twinEntity.getActionsRestricted());
        }

        @Test
        void testLoadActions_MultipleValidators_OnePasses() throws ServiceException {
            // Given
            TwinActionValidatorRuleEntity validatorRule1 = new TwinActionValidatorRuleEntity();
            validatorRule1.setId(UUID.randomUUID());
            validatorRule1.setTwinAction(TwinAction.DELETE);
            validatorRule1.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));

            TwinActionValidatorRuleEntity validatorRule2 = new TwinActionValidatorRuleEntity();
            validatorRule2.setId(UUID.randomUUID());
            validatorRule2.setTwinAction(TwinAction.DELETE);
            validatorRule2.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule1);
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule2);

            // First validator fails, second passes
            when(twinValidatorSetService.isValid(twinEntity, validatorRule1)).thenReturn(false);
            when(twinValidatorSetService.isValid(twinEntity, validatorRule2)).thenReturn(true);

            // When
            twinActionService.loadActions(twinEntity);

            // Then - should be allowed since at least one validator passed
            assertTrue(twinEntity.getActions().contains(TwinAction.DELETE));
        }
    }

    @Nested
    class CombinedRestrictionTests {
        @Test
        void testLoadActions_PermissionDenied_SkipsValidators() throws ServiceException {
            // Given
            TwinActionPermissionEntity permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(actionRestrictionReasonId);

            TwinActionValidatorRuleEntity validatorRule = new TwinActionValidatorRuleEntity();
            validatorRule.setTwinAction(TwinAction.DELETE);
            validatorRule.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByPermission().add(permissionEntity);
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule);

            when(permissionService.hasPermission(twinEntity, permissionId)).thenReturn(false);

            // When
            twinActionService.loadActions(twinEntity);

            // Then - validator should not be called since permission denied
            verify(twinValidatorSetService, never()).isValid(eq(twinEntity), any(TwinActionValidatorRuleEntity.class));
            assertFalse(twinEntity.getActions().contains(TwinAction.DELETE));
        }

        @Test
        void testLoadActions_PermissionGranted_ThenValidatorFails() throws ServiceException {
            // Given
            UUID validatorReasonId = UUID.randomUUID();

            TwinActionPermissionEntity permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);

            TwinActionValidatorRuleEntity validatorRule = new TwinActionValidatorRuleEntity();
            validatorRule.setTwinAction(TwinAction.DELETE);
            validatorRule.setActionRestrictionReasonId(validatorReasonId);
            validatorRule.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));

            TwinValidatorEntity twinValidatorEntity = new TwinValidatorEntity();
            twinValidatorEntity.setId(UUID.randomUUID());
            twinValidatorEntity.setOrder(1);
            twinValidatorEntity.setInvert(false);
            validatorRule.getTwinValidatorKit().add(twinValidatorEntity);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByPermission().add(permissionEntity);
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule);

            when(permissionService.hasPermission(twinEntity, permissionId)).thenReturn(true);
            when(twinValidatorSetService.isValid(twinEntity, validatorRule)).thenReturn(false);

            // When
            twinActionService.loadActions(twinEntity);

            // Then - validator reason should be used (not permission reason)
            assertFalse(twinEntity.getActions().contains(TwinAction.DELETE));
            assertNotNull(twinEntity.getActionsRestricted());
            assertEquals(validatorReasonId, twinEntity.getActionsRestricted().get(TwinAction.DELETE));
        }

        @Test
        void testLoadActions_PermissionDenied_ValidatorPasses() throws ServiceException {
            // Given
            UUID permissionReasonId = UUID.randomUUID();
            UUID validatorReasonId = UUID.randomUUID();

            TwinActionPermissionEntity permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(permissionReasonId);

            TwinActionValidatorRuleEntity validatorRule = new TwinActionValidatorRuleEntity();
            validatorRule.setId(UUID.randomUUID());
            validatorRule.setTwinAction(TwinAction.DELETE);
            validatorRule.setActionRestrictionReasonId(validatorReasonId);
            validatorRule.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));

            TwinValidatorEntity twinValidatorEntity = new TwinValidatorEntity();
            twinValidatorEntity.setId(UUID.randomUUID());
            twinValidatorEntity.setOrder(1);
            twinValidatorEntity.setInvert(false);
            validatorRule.getTwinValidatorKit().add(twinValidatorEntity);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByPermission().add(permissionEntity);
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule);

            when(permissionService.hasPermission(twinEntity, permissionId)).thenReturn(false);
            // Note: validator stub not needed since permission denies and skips validators

            // When
            twinActionService.loadActions(twinEntity);

            // Then - action should be forbidden (permission priority), permission reason should be used
            // Validator passes but permission denies - permission has priority
            verify(twinValidatorSetService, never()).isValid(eq(twinEntity), any(TwinActionValidatorRuleEntity.class));
            assertFalse(twinEntity.getActions().contains(TwinAction.DELETE));
            assertNotNull(twinEntity.getActionsRestricted());
            assertEquals(permissionReasonId, twinEntity.getActionsRestricted().get(TwinAction.DELETE));
        }

        @Test
        void testLoadActions_BothPermissionAndValidatorDeny() throws ServiceException {
            // Given
            UUID permissionReasonId = UUID.randomUUID();
            UUID validatorReasonId = UUID.randomUUID();

            TwinActionPermissionEntity permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(permissionReasonId);

            TwinActionValidatorRuleEntity validatorRule = new TwinActionValidatorRuleEntity();
            validatorRule.setId(UUID.randomUUID());
            validatorRule.setTwinAction(TwinAction.DELETE);
            validatorRule.setActionRestrictionReasonId(validatorReasonId);
            validatorRule.setTwinValidatorKit(new Kit<>(TwinValidatorEntity::getId));

            TwinValidatorEntity twinValidatorEntity = new TwinValidatorEntity();
            twinValidatorEntity.setId(UUID.randomUUID());
            twinValidatorEntity.setOrder(1);
            twinValidatorEntity.setInvert(false);
            validatorRule.getTwinValidatorKit().add(twinValidatorEntity);

            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByPermission().add(permissionEntity);
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            twinClassEntity.getActionsProtectedByValidatorRules().add(validatorRule);

            when(permissionService.hasPermission(twinEntity, permissionId)).thenReturn(false);
            // Note: validator stub not needed since permission denies and skips validators

            // When
            twinActionService.loadActions(twinEntity);

            // Then - action should be forbidden, permission reason should be used (permission priority)
            verify(twinValidatorSetService, never()).isValid(eq(twinEntity), any(TwinActionValidatorRuleEntity.class));
            assertFalse(twinEntity.getActions().contains(TwinAction.DELETE));
            assertNotNull(twinEntity.getActionsRestricted());
            // Permission reason has priority over validator reason
            assertEquals(permissionReasonId, twinEntity.getActionsRestricted().get(TwinAction.DELETE));
        }
    }
}
