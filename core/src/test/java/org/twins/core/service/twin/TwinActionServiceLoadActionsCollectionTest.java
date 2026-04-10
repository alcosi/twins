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
import org.twins.core.enums.action.TwinAction;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.PermissionService.PermissionDetectKey;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwinActionServiceLoadActionsCollectionTest {

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private TwinActionService twinActionService;

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
    class EmptyCollectionTests {
        @Test
        void testLoadActions_EmptyCollection() throws ServiceException {
            // Given
            Collection<TwinEntity> entities = Collections.emptyList();

            // When
            twinActionService.loadActions(entities);

            // Then - no exception thrown
            assertTrue(true);
        }

        @Test
        void testLoadActions_AllTwinsAlreadyLoaded() throws ServiceException {
            // Given
            twin1.setActions(new HashSet<>());
            twin1.getActions().add(TwinAction.EDIT);
            twin2.setActions(new HashSet<>());
            twin2.getActions().add(TwinAction.EDIT);

            Collection<TwinEntity> entities = List.of(twin1, twin2);

            // When
            twinActionService.loadActions(entities);

            // Then - should not load, actions remain unchanged
            assertEquals(1, twin1.getActions().size());
            assertEquals(1, twin2.getActions().size());
        }

        @Test
        void testLoadActions_SomeTwinsAlreadyLoaded() throws ServiceException {
            // Given
            twin1.setActions(new HashSet<>());
            twin1.getActions().add(TwinAction.EDIT);
            // twin2 actions are null - needs loading

            Collection<TwinEntity> entities = List.of(twin1, twin2);

            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            // When
            twinActionService.loadActions(entities);

            // Then - twin1 should keep its actions, twin2 should get all actions
            assertEquals(1, twin1.getActions().size());
            assertNotNull(twin2.getActions());
            assertTrue(twin2.getActions().size() > 0);
        }
    }

    @Nested
    class GroupingByClassTests {
        @Test
        void testLoadActions_MultipleTwinsSameClass() throws ServiceException {
            // Given
            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            // No restrictions - all actions should be allowed
            Collection<TwinEntity> entities = List.of(twin1, twin2);

            // When
            twinActionService.loadActions(entities);

            // Then - both twins should have all actions
            assertNotNull(twin1.getActions());
            assertNotNull(twin2.getActions());
            assertTrue(twin1.getActions().contains(TwinAction.DELETE));
            assertTrue(twin2.getActions().contains(TwinAction.DELETE));
            assertEquals(EnumSet.allOf(TwinAction.class).size(), twin1.getActions().size());
            assertEquals(EnumSet.allOf(TwinAction.class).size(), twin2.getActions().size());
        }

        @Test
        void testLoadActions_MultipleTwinsDifferentClasses() throws ServiceException {
            // Given
            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            class2.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class2.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            // No restrictions - all actions should be allowed
            Collection<TwinEntity> entities = List.of(twin1, twin2, twin3);

            // When
            twinActionService.loadActions(entities);

            // Then - all twins should have all actions
            assertNotNull(twin1.getActions());
            assertNotNull(twin2.getActions());
            assertNotNull(twin3.getActions());
            assertEquals(EnumSet.allOf(TwinAction.class).size(), twin1.getActions().size());
            assertEquals(EnumSet.allOf(TwinAction.class).size(), twin2.getActions().size());
            assertEquals(EnumSet.allOf(TwinAction.class).size(), twin3.getActions().size());
        }
    }

    @Nested
    class PermissionRestrictionTests {
        @Test
        void testLoadActions_PermissionDeniedForSomeTwins() throws ServiceException {
            // Given
            TwinActionPermissionEntity permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(restrictionReasonId);

            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.getActionsProtectedByPermission().add(permissionEntity);
            class1.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            // Mock permissionService to return different keys for different twins
            PermissionDetectKey key1 = mock(PermissionDetectKey.class);
            PermissionDetectKey key2 = mock(PermissionDetectKey.class);
            Map<PermissionDetectKey, List<TwinEntity>> keyMap = new HashMap<>();
            keyMap.put(key1, List.of(twin1));
            keyMap.put(key2, List.of(twin2));

            when(permissionService.convertToDetectKeys(List.of(twin1, twin2))).thenReturn(keyMap);
            when(permissionService.hasPermission(key1, permissionId)).thenReturn(true);  // twin1 allowed
            when(permissionService.hasPermission(key2, permissionId)).thenReturn(false); // twin2 denied

            Collection<TwinEntity> entities = List.of(twin1, twin2);

            // When
            twinActionService.loadActions(entities);

            // Then
            assertTrue(twin1.getActions().contains(TwinAction.DELETE));
            assertFalse(twin2.getActions().contains(TwinAction.DELETE));
            assertNotNull(twin2.getActionsRestricted());
            assertEquals(restrictionReasonId, twin2.getActionsRestricted().get(TwinAction.DELETE));
        }

        @Test
        void testLoadActions_AllTwinsPermissionDenied() throws ServiceException {
            // Given
            TwinActionPermissionEntity permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(restrictionReasonId);

            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.getActionsProtectedByPermission().add(permissionEntity);
            class1.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            PermissionDetectKey key = mock(PermissionDetectKey.class);
            Map<PermissionDetectKey, List<TwinEntity>> keyMap = new HashMap<>();
            keyMap.put(key, List.of(twin1, twin2));

            when(permissionService.convertToDetectKeys(List.of(twin1, twin2))).thenReturn(keyMap);
            when(permissionService.hasPermission(key, permissionId)).thenReturn(false);

            Collection<TwinEntity> entities = List.of(twin1, twin2);

            // When
            twinActionService.loadActions(entities);

            // Then
            assertFalse(twin1.getActions().contains(TwinAction.DELETE));
            assertFalse(twin2.getActions().contains(TwinAction.DELETE));
        }
    }

    @Nested
    class CombinedRestrictionTests {
        @Test
        void testLoadActions_PermissionDeniedWithRestrictionReason() throws ServiceException {
            // Given
            TwinActionPermissionEntity permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(restrictionReasonId);

            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.getActionsProtectedByPermission().add(permissionEntity);
            class1.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            PermissionDetectKey key = mock(PermissionDetectKey.class);
            Map<PermissionDetectKey, List<TwinEntity>> keyMap = new HashMap<>();
            keyMap.put(key, List.of(twin1, twin2));

            when(permissionService.convertToDetectKeys(List.of(twin1, twin2))).thenReturn(keyMap);
            when(permissionService.hasPermission(key, permissionId)).thenReturn(false);

            Collection<TwinEntity> entities = List.of(twin1, twin2);

            // When
            twinActionService.loadActions(entities);

            // Then
            assertFalse(twin1.getActions().contains(TwinAction.DELETE));
            assertFalse(twin2.getActions().contains(TwinAction.DELETE));
            assertNotNull(twin1.getActionsRestricted());
            assertNotNull(twin2.getActionsRestricted());
            assertEquals(restrictionReasonId, twin1.getActionsRestricted().get(TwinAction.DELETE));
            assertEquals(restrictionReasonId, twin2.getActionsRestricted().get(TwinAction.DELETE));
        }
    }

    @Nested
    class EmptyRestrictionsTests {
        @Test
        void testLoadActions_NoRestrictions_AllActionsAllowed() throws ServiceException {
            // Given
            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            Collection<TwinEntity> entities = List.of(twin1, twin2);

            // When
            twinActionService.loadActions(entities);

            // Then
            assertNotNull(twin1.getActions());
            assertNotNull(twin2.getActions());
            assertEquals(EnumSet.allOf(TwinAction.class).size(), twin1.getActions().size());
            assertEquals(EnumSet.allOf(TwinAction.class).size(), twin2.getActions().size());
        }

        @Test
        void testLoadActions_EmptyRestrictionReasonsNotSet() throws ServiceException {
            // Given
            TwinActionPermissionEntity permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setPermissionId(permissionId);
            permissionEntity.setActionRestrictionReasonId(null); // no reason

            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class1.getActionsProtectedByPermission().add(permissionEntity);
            class1.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            PermissionDetectKey key = mock(PermissionDetectKey.class);
            Map<PermissionDetectKey, List<TwinEntity>> keyMap = new HashMap<>();
            keyMap.put(key, List.of(twin1));

            when(permissionService.convertToDetectKeys(List.of(twin1))).thenReturn(keyMap);
            when(permissionService.hasPermission(key, permissionId)).thenReturn(false);

            Collection<TwinEntity> entities = List.of(twin1);

            // When
            twinActionService.loadActions(entities);

            // Then
            assertFalse(twin1.getActions().contains(TwinAction.DELETE));
            // actionsRestricted should not be set since there's no restriction reason
            assertNull(twin1.getActionsRestricted());
        }
    }
}
