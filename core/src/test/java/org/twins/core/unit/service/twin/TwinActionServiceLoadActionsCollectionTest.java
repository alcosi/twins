package org.twins.core.unit.service.twin;

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
import org.twins.core.enums.action.TwinAction;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.PermissionService.PermissionDetectKey;
import org.twins.core.service.twin.TwinActionService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinActionServiceLoadActionsCollectionTest extends BaseUnitTest {

    @Mock private PermissionService permissionService;
    @InjectMocks private TwinActionService twinActionService;

    private TwinEntity twin1;
    private TwinEntity twin2;
    private TwinEntity twin3;
    private TwinClassEntity class1;
    private TwinClassEntity class2;
    private UUID permissionId;
    private UUID restrictionReasonId;

    @BeforeEach
    void setUp() {
        class1 = new TwinClassEntity();
        class1.setId(UUID.randomUUID());

        class2 = new TwinClassEntity();
        class2.setId(UUID.randomUUID());

        twin1 = new TwinEntity();
        twin1.setId(UUID.randomUUID());
        twin1.setTwinClassId(class1.getId());
        twin1.setTwinClass(class1);

        twin2 = new TwinEntity();
        twin2.setId(UUID.randomUUID());
        twin2.setTwinClassId(class1.getId());
        twin2.setTwinClass(class1);

        twin3 = new TwinEntity();
        twin3.setId(UUID.randomUUID());
        twin3.setTwinClassId(class2.getId());
        twin3.setTwinClass(class2);

        permissionId = UUID.randomUUID();
        restrictionReasonId = UUID.randomUUID();
    }

    @Nested
    class EmptyOrAlreadyLoaded {

        @Test
        void loadActions_emptyCollection_doesNotThrow() throws ServiceException {
            twinActionService.loadActions(Collections.emptyList());
        }

        @Test
        void loadActions_allTwinsAlreadyLoaded_keepsExistingActions() throws ServiceException {
            twin1.setActions(new HashSet<>());
            twin1.getActions().add(TwinAction.EDIT);
            twin2.setActions(new HashSet<>());
            twin2.getActions().add(TwinAction.EDIT);

            twinActionService.loadActions(List.of(twin1, twin2));

            assertEquals(1, twin1.getActions().size());
            assertEquals(1, twin2.getActions().size());
        }

        @Test
        void loadActions_someTwinsAlreadyLoaded_onlyLoadsMissing() throws ServiceException {
            twin1.setActions(new HashSet<>());
            twin1.getActions().add(TwinAction.EDIT);
            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            twinActionService.loadActions(List.of(twin1, twin2));

            assertEquals(1, twin1.getActions().size());
            assertNotNull(twin2.getActions());
            assertFalse(twin2.getActions().isEmpty());
        }
    }

    @Nested
    class GroupingByClass {

        @Test
        void loadActions_multipleTwinsSameClass_allGetAllActions() throws ServiceException {
            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            twinActionService.loadActions(List.of(twin1, twin2));

            assertEquals(EnumSet.allOf(TwinAction.class).size(), twin1.getActions().size());
            assertEquals(EnumSet.allOf(TwinAction.class).size(), twin2.getActions().size());
        }

        @Test
        void loadActions_multipleTwinsDifferentClasses_allGetAllActions() throws ServiceException {
            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            class2.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class2.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            twinActionService.loadActions(List.of(twin1, twin2, twin3));

            assertEquals(EnumSet.allOf(TwinAction.class).size(), twin1.getActions().size());
            assertEquals(EnumSet.allOf(TwinAction.class).size(), twin2.getActions().size());
            assertEquals(EnumSet.allOf(TwinAction.class).size(), twin3.getActions().size());
        }
    }

    @Nested
    class PermissionRestriction {

        @Test
        void loadActions_permissionDeniedForSomeTwins_restrictsOnly() throws ServiceException {
            var permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(restrictionReasonId);

            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.getActionsProtectedByPermission().add(permissionEntity);
            class1.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            var key1 = mock(PermissionDetectKey.class);
            var key2 = mock(PermissionDetectKey.class);
            Map<PermissionDetectKey, List<TwinEntity>> keyMap = new HashMap<>();
            keyMap.put(key1, List.of(twin1));
            keyMap.put(key2, List.of(twin2));

            when(permissionService.convertToDetectKeys(List.of(twin1, twin2))).thenReturn(keyMap);
            when(permissionService.hasPermission(key1, permissionId)).thenReturn(true);
            when(permissionService.hasPermission(key2, permissionId)).thenReturn(false);

            twinActionService.loadActions(List.of(twin1, twin2));

            assertTrue(twin1.getActions().contains(TwinAction.DELETE));
            assertFalse(twin2.getActions().contains(TwinAction.DELETE));
            assertEquals(restrictionReasonId, twin2.getActionsRestricted().get(TwinAction.DELETE));
        }

        @Test
        void loadActions_allTwinsPermissionDenied_noneHaveAction() throws ServiceException {
            var permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(restrictionReasonId);

            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.getActionsProtectedByPermission().add(permissionEntity);
            class1.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            var key = mock(PermissionDetectKey.class);
            Map<PermissionDetectKey, List<TwinEntity>> keyMap = new HashMap<>();
            keyMap.put(key, List.of(twin1, twin2));

            when(permissionService.convertToDetectKeys(List.of(twin1, twin2))).thenReturn(keyMap);
            when(permissionService.hasPermission(key, permissionId)).thenReturn(false);

            twinActionService.loadActions(List.of(twin1, twin2));

            assertFalse(twin1.getActions().contains(TwinAction.DELETE));
            assertFalse(twin2.getActions().contains(TwinAction.DELETE));
        }

        @Test
        void loadActions_permissionDeniedWithRestrictionReason_setsReasonOnAll() throws ServiceException {
            var permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(restrictionReasonId);

            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.getActionsProtectedByPermission().add(permissionEntity);
            class1.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            var key = mock(PermissionDetectKey.class);
            Map<PermissionDetectKey, List<TwinEntity>> keyMap = new HashMap<>();
            keyMap.put(key, List.of(twin1, twin2));

            when(permissionService.convertToDetectKeys(List.of(twin1, twin2))).thenReturn(keyMap);
            when(permissionService.hasPermission(key, permissionId)).thenReturn(false);

            twinActionService.loadActions(List.of(twin1, twin2));

            assertEquals(restrictionReasonId, twin1.getActionsRestricted().get(TwinAction.DELETE));
            assertEquals(restrictionReasonId, twin2.getActionsRestricted().get(TwinAction.DELETE));
        }

        @Test
        void loadActions_permissionDeniedWithoutReasonId_doesNotSetRestricted() throws ServiceException {
            var permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(null);

            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.getActionsProtectedByPermission().add(permissionEntity);
            class1.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            var key = mock(PermissionDetectKey.class);
            Map<PermissionDetectKey, List<TwinEntity>> keyMap = new HashMap<>();
            keyMap.put(key, List.of(twin1));

            when(permissionService.convertToDetectKeys(List.of(twin1))).thenReturn(keyMap);
            when(permissionService.hasPermission(key, permissionId)).thenReturn(false);

            twinActionService.loadActions(List.of(twin1));

            assertFalse(twin1.getActions().contains(TwinAction.DELETE));
            assertNull(twin1.getActionsRestricted());
        }
    }

    @Nested
    class NoRestrictions {

        @Test
        void loadActions_noRestrictions_allActionsAllowed() throws ServiceException {
            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            twinActionService.loadActions(List.of(twin1, twin2));

            assertEquals(EnumSet.allOf(TwinAction.class).size(), twin1.getActions().size());
            assertEquals(EnumSet.allOf(TwinAction.class).size(), twin2.getActions().size());
        }
    }
}
