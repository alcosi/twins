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
import org.twins.core.dao.action.TwinActionPermissionRepository;
import org.twins.core.dao.validator.TwinActionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.service.validator.TwinValidatorService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwinActionServiceLoadClassProtectedActionsTest {

    @Mock
    private TwinActionPermissionRepository twinActionPermissionRepository;

    @Mock
    private TwinActionValidatorRuleRepository twinActionValidatorRuleRepository;

    @Mock
    private TwinValidatorService twinValidatorService;

    @InjectMocks
    private TwinActionService twinActionService;

    private TwinClassEntity twinClassEntity;
    private TwinClassEntity parentClassEntity;
    private UUID permissionId;

    @BeforeEach
    void setUp() {
        twinClassEntity = new TwinClassEntity();
        twinClassEntity.setId(UUID.randomUUID());
        twinClassEntity.setExtendedClassIdSet(new HashSet<>());
        twinClassEntity.getExtendedClassIdSet().add(twinClassEntity.getId());

        parentClassEntity = new TwinClassEntity();
        parentClassEntity.setId(UUID.randomUUID());
        twinClassEntity.getExtendedClassIdSet().add(parentClassEntity.getId());

        permissionId = UUID.randomUUID();
    }

    @Nested
    class SingleEntityTests {
        @Test
        void testLoadClassProtectedActions_AlreadyLoaded() throws ServiceException {
            // Given
            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            // When
            twinActionService.loadClassProtectedActions(twinClassEntity);

            // Then - should not reload, repository methods should not be called
            verify(twinActionPermissionRepository, never()).findByTwinClassIdIn(any());
        }

        @Test
        void testLoadClassProtectedActions_NoRestrictions() throws ServiceException {
            // Given
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setActionsProtectedByValidatorRules(null);

            when(twinActionPermissionRepository.findByTwinClassIdIn(any())).thenReturn(List.of());
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(any())).thenReturn(List.of());

            // When
            twinActionService.loadClassProtectedActions(twinClassEntity);

            // Then
            assertNotNull(twinClassEntity.getActionsProtectedByPermission());
            assertNotNull(twinClassEntity.getActionsProtectedByValidatorRules());
        }

        @Test
        void testLoadClassProtectedActions_WithPermissions() throws ServiceException {
            // Given
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setActionsProtectedByValidatorRules(null);

            TwinActionPermissionEntity permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setId(UUID.randomUUID());
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setTwinClassId(twinClassEntity.getId());
            permissionEntity.setPermissionId(permissionId);

            when(twinActionPermissionRepository.findByTwinClassIdIn(any())).thenReturn(List.of(permissionEntity));
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(any())).thenReturn(List.of());

            // When
            twinActionService.loadClassProtectedActions(twinClassEntity);

            // Then
            assertNotNull(twinClassEntity.getActionsProtectedByPermission());
            assertEquals(permissionEntity, twinClassEntity.getActionsProtectedByPermission().get(TwinAction.DELETE));
        }
    }

    @Nested
    class CollectionTests {
        private TwinClassEntity class1;
        private TwinClassEntity class2;
        private TwinClassEntity class3;

        @BeforeEach
        void setUpClasses() {
            class1 = new TwinClassEntity();
            class1.setId(UUID.randomUUID());
            class1.setExtendedClassIdSet(new HashSet<>());
            class1.getExtendedClassIdSet().add(class1.getId());

            class2 = new TwinClassEntity();
            class2.setId(UUID.randomUUID());
            class2.setExtendedClassIdSet(new HashSet<>());
            class2.getExtendedClassIdSet().add(class2.getId());

            class3 = new TwinClassEntity();
            class3.setId(UUID.randomUUID());
            class3.setExtendedClassIdSet(new HashSet<>());
            class3.getExtendedClassIdSet().add(class3.getId());
        }

        @Test
        void testLoadClassProtectedActions_EmptyCollection() throws ServiceException {
            // Given
            Collection<TwinClassEntity> entities = Collections.emptyList();

            // When & Then - should not throw exception
            assertDoesNotThrow(() -> twinActionService.loadClassProtectedActions(entities));
        }

        @Test
        void testLoadClassProtectedActions_SomeAlreadyLoaded() throws ServiceException {
            // Given
            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class2.setActionsProtectedByPermission(null);
            class3.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));

            when(twinActionPermissionRepository.findByTwinClassIdIn(any())).thenReturn(List.of());
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(any())).thenReturn(List.of());

            Collection<TwinClassEntity> entities = List.of(class1, class2, class3);

            // When
            twinActionService.loadClassProtectedActions(entities);

            // Then - only class2 should be loaded
            verify(twinActionPermissionRepository, times(1)).findByTwinClassIdIn(any());
        }

        @Test
        void testLoadClassProtectedActions_AllNeedLoading() throws ServiceException {
            // Given
            class1.setActionsProtectedByPermission(null);
            class2.setActionsProtectedByPermission(null);
            class3.setActionsProtectedByPermission(null);

            TwinActionPermissionEntity permission1 = new TwinActionPermissionEntity();
            permission1.setId(UUID.randomUUID());
            permission1.setTwinAction(TwinAction.DELETE);
            permission1.setTwinClassId(class1.getId());

            TwinActionPermissionEntity permission2 = new TwinActionPermissionEntity();
            permission2.setId(UUID.randomUUID());
            permission2.setTwinAction(TwinAction.EDIT);
            permission2.setTwinClassId(class2.getId());

            Set<UUID> classIds = new HashSet<>();
            classIds.add(class1.getId());
            classIds.add(class2.getId());
            classIds.add(class3.getId());

            when(twinActionPermissionRepository.findByTwinClassIdIn(classIds)).thenReturn(List.of(permission1, permission2));
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(classIds)).thenReturn(List.of());

            Collection<TwinClassEntity> entities = List.of(class1, class2, class3);

            // When
            twinActionService.loadClassProtectedActions(entities);

            // Then
            assertNotNull(class1.getActionsProtectedByPermission());
            assertNotNull(class2.getActionsProtectedByPermission());
            assertNotNull(class3.getActionsProtectedByPermission());
        }
    }

    @Nested
    class InheritanceTests {
        @Test
        void testLoadClassProtectedActions_InheritFromParent() throws ServiceException {
            // Given
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setActionsProtectedByValidatorRules(null);

            TwinActionPermissionEntity parentPermission = new TwinActionPermissionEntity();
            parentPermission.setId(UUID.randomUUID());
            parentPermission.setTwinAction(TwinAction.DELETE);
            parentPermission.setTwinClassId(parentClassEntity.getId());
            parentPermission.setPermissionId(permissionId);

            Set<UUID> classIds = new HashSet<>();
            classIds.add(twinClassEntity.getId());
            classIds.add(parentClassEntity.getId());

            when(twinActionPermissionRepository.findByTwinClassIdIn(classIds)).thenReturn(List.of(parentPermission));
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(classIds)).thenReturn(List.of());

            // When
            twinActionService.loadClassProtectedActions(twinClassEntity);

            // Then - should inherit from parent
            assertNotNull(twinClassEntity.getActionsProtectedByPermission());
            assertEquals(parentPermission, twinClassEntity.getActionsProtectedByPermission().get(TwinAction.DELETE));
        }

        @Test
        void testLoadClassProtectedActions_ChildOverridesParent() throws ServiceException {
            // Given
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setActionsProtectedByValidatorRules(null);

            UUID parentPermissionId = UUID.randomUUID();
            UUID childPermissionId = UUID.randomUUID();

            TwinActionPermissionEntity parentPermission = new TwinActionPermissionEntity();
            parentPermission.setId(UUID.randomUUID());
            parentPermission.setTwinAction(TwinAction.DELETE);
            parentPermission.setTwinClassId(parentClassEntity.getId());
            parentPermission.setPermissionId(parentPermissionId);

            TwinActionPermissionEntity childPermission = new TwinActionPermissionEntity();
            childPermission.setId(UUID.randomUUID());
            childPermission.setTwinAction(TwinAction.DELETE);
            childPermission.setTwinClassId(twinClassEntity.getId());
            childPermission.setPermissionId(childPermissionId);

            Set<UUID> classIds = new LinkedHashSet<>();
            classIds.add(twinClassEntity.getId());
            classIds.add(parentClassEntity.getId());
            twinClassEntity.setExtendedClassIdSet(classIds);

            when(twinActionPermissionRepository.findByTwinClassIdIn(classIds))
                    .thenReturn(List.of(parentPermission, childPermission));
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(classIds)).thenReturn(List.of());

            // When
            twinActionService.loadClassProtectedActions(twinClassEntity);

            // Then - child permission should override parent (child is first in LinkedHashSet)
            assertNotNull(twinClassEntity.getActionsProtectedByPermission());
            TwinActionPermissionEntity result = twinClassEntity.getActionsProtectedByPermission().get(TwinAction.DELETE);
            assertNotNull(result);
            // Should get child permission since it's checked first (LinkedHashSet maintains insertion order)
            assertEquals(childPermissionId, result.getPermissionId());
        }
    }

    @Nested
    class ValidatorsTests {
        @Test
        void testLoadClassProtectedActions_WithValidators() throws ServiceException {
            // Given
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setActionsProtectedByValidatorRules(null);

            TwinActionValidatorRuleEntity validatorRule = new TwinActionValidatorRuleEntity();
            validatorRule.setId(UUID.randomUUID());
            validatorRule.setTwinAction(TwinAction.DELETE);
            validatorRule.setTwinClassId(twinClassEntity.getId());

            when(twinActionPermissionRepository.findByTwinClassIdIn(any())).thenReturn(List.of());
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(any())).thenReturn(List.of(validatorRule));

            // When
            twinActionService.loadClassProtectedActions(twinClassEntity);

            // Then
            assertNotNull(twinClassEntity.getActionsProtectedByValidatorRules());
            List<TwinActionValidatorRuleEntity> rules = twinClassEntity.getActionsProtectedByValidatorRules().getGrouped(TwinAction.DELETE);
            assertNotNull(rules);
            assertEquals(1, rules.size());
            assertEquals(validatorRule, rules.get(0));
        }

        @Test
        void testLoadClassProtectedActions_ValidatorsForDifferentActions() throws ServiceException {
            // Given
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setActionsProtectedByValidatorRules(null);

            TwinActionValidatorRuleEntity deleteValidator = new TwinActionValidatorRuleEntity();
            deleteValidator.setId(UUID.randomUUID());
            deleteValidator.setTwinAction(TwinAction.DELETE);
            deleteValidator.setTwinClassId(twinClassEntity.getId());

            TwinActionValidatorRuleEntity editValidator = new TwinActionValidatorRuleEntity();
            editValidator.setId(UUID.randomUUID());
            editValidator.setTwinAction(TwinAction.EDIT);
            editValidator.setTwinClassId(twinClassEntity.getId());

            when(twinActionPermissionRepository.findByTwinClassIdIn(any())).thenReturn(List.of());
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(any())).thenReturn(List.of(deleteValidator, editValidator));

            // When
            twinActionService.loadClassProtectedActions(twinClassEntity);

            // Then
            assertNotNull(twinClassEntity.getActionsProtectedByValidatorRules());
            assertTrue(twinClassEntity.getActionsProtectedByValidatorRules().getGrouped(TwinAction.DELETE).size() > 0);
            assertTrue(twinClassEntity.getActionsProtectedByValidatorRules().getGrouped(TwinAction.EDIT).size() > 0);
        }
    }

    @Nested
    class EdgeCasesTests {
        @Test
        void testLoadClassProtectedActions_EmptyEntities() throws ServiceException {
            // Given
            Collection<TwinClassEntity> entities = Collections.emptyList();

            // When & Then - should handle gracefully
            assertDoesNotThrow(() -> twinActionService.loadClassProtectedActions(entities));
        }

        @Test
        void testLoadClassProtectedActions_EmptyExtendedClassIdSet() throws ServiceException {
            // Given
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setExtendedClassIdSet(new HashSet<>());

            when(twinActionPermissionRepository.findByTwinClassIdIn(any())).thenReturn(List.of());
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(any())).thenReturn(List.of());

            // When
            twinActionService.loadClassProtectedActions(twinClassEntity);

            // Then - should not throw exception
            assertNotNull(twinClassEntity.getActionsProtectedByPermission());
        }

        @Test
        void testLoadClassProtectedActions_NoMatchingRecords() throws ServiceException {
            // Given
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setActionsProtectedByValidatorRules(null);

            when(twinActionPermissionRepository.findByTwinClassIdIn(any())).thenReturn(List.of());
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(any())).thenReturn(List.of());

            // When
            twinActionService.loadClassProtectedActions(twinClassEntity);

            // Then - should initialize empty collections
            assertNotNull(twinClassEntity.getActionsProtectedByPermission());
            assertNotNull(twinClassEntity.getActionsProtectedByValidatorRules());
            assertTrue(twinClassEntity.getActionsProtectedByPermission().isEmpty());
            assertTrue(twinClassEntity.getActionsProtectedByValidatorRules().isEmpty());
        }
    }
}
