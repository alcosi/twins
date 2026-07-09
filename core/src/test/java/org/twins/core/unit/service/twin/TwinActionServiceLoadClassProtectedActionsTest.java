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
import org.twins.core.dao.action.TwinActionPermissionRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleRepository;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.service.twin.TwinActionService;
import org.twins.core.service.validator.TwinValidatorService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TwinActionServiceLoadClassProtectedActionsTest extends BaseUnitTest {

    @Mock private TwinActionPermissionRepository twinActionPermissionRepository;
    @Mock private TwinActionValidatorRuleRepository twinActionValidatorRuleRepository;
    @Mock private TwinValidatorService twinValidatorService;
    @InjectMocks private TwinActionService twinActionService;

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
    class SingleEntity {

        @Test
        void loadClassProtectedActions_alreadyLoaded_skipsRepository() throws ServiceException {
            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            twinActionService.loadClassProtectedActions(twinClassEntity);

            verify(twinActionPermissionRepository, never()).findByTwinClassIdIn(any());
        }

        @Test
        void loadClassProtectedActions_noRestrictions_initializesEmptyKits() throws ServiceException {
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setActionsProtectedByValidatorRules(null);

            when(twinActionPermissionRepository.findByTwinClassIdIn(any())).thenReturn(List.of());
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(any())).thenReturn(List.of());

            twinActionService.loadClassProtectedActions(twinClassEntity);

            assertNotNull(twinClassEntity.getActionsProtectedByPermission());
            assertNotNull(twinClassEntity.getActionsProtectedByValidatorRules());
        }

        @Test
        void loadClassProtectedActions_withPermission_setsPermissionByAction() throws ServiceException {
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setActionsProtectedByValidatorRules(null);

            var permissionEntity = new TwinActionPermissionEntity();
            permissionEntity.setId(UUID.randomUUID());
            permissionEntity.setTwinAction(TwinAction.DELETE);
            permissionEntity.setTwinClassId(twinClassEntity.getId());
            permissionEntity.setPermissionId(permissionId);

            when(twinActionPermissionRepository.findByTwinClassIdIn(any())).thenReturn(List.of(permissionEntity));
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(any())).thenReturn(List.of());

            twinActionService.loadClassProtectedActions(twinClassEntity);

            assertEquals(permissionEntity, twinClassEntity.getActionsProtectedByPermission().get(TwinAction.DELETE));
        }
    }

    @Nested
    class Collection {

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
        void loadClassProtectedActions_emptyCollection_doesNotThrow() {
            assertDoesNotThrow(() -> twinActionService.loadClassProtectedActions(Collections.emptyList()));
        }

        @Test
        void loadClassProtectedActions_someAlreadyLoaded_onlyFetchesMissing() throws ServiceException {
            class1.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            class2.setActionsProtectedByPermission(null);
            class3.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));

            when(twinActionPermissionRepository.findByTwinClassIdIn(any())).thenReturn(List.of());
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(any())).thenReturn(List.of());

            twinActionService.loadClassProtectedActions(List.of(class1, class2, class3));

            verify(twinActionPermissionRepository, times(1)).findByTwinClassIdIn(any());
        }

        @Test
        void loadClassProtectedActions_allNeedLoading_initializesAll() throws ServiceException {
            class1.setActionsProtectedByPermission(null);
            class2.setActionsProtectedByPermission(null);
            class3.setActionsProtectedByPermission(null);

            var permission1 = new TwinActionPermissionEntity();
            permission1.setId(UUID.randomUUID());
            permission1.setTwinAction(TwinAction.DELETE);
            permission1.setTwinClassId(class1.getId());

            var permission2 = new TwinActionPermissionEntity();
            permission2.setId(UUID.randomUUID());
            permission2.setTwinAction(TwinAction.EDIT);
            permission2.setTwinClassId(class2.getId());

            Set<UUID> classIds = new HashSet<>();
            classIds.add(class1.getId());
            classIds.add(class2.getId());
            classIds.add(class3.getId());

            when(twinActionPermissionRepository.findByTwinClassIdIn(classIds))
                    .thenReturn(List.of(permission1, permission2));
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(classIds)).thenReturn(List.of());

            twinActionService.loadClassProtectedActions(List.of(class1, class2, class3));

            assertNotNull(class1.getActionsProtectedByPermission());
            assertNotNull(class2.getActionsProtectedByPermission());
            assertNotNull(class3.getActionsProtectedByPermission());
        }
    }

    @Nested
    class Inheritance {

        @Test
        void loadClassProtectedActions_parentPermission_inheritedByChild() throws ServiceException {
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setActionsProtectedByValidatorRules(null);

            var parentPermission = new TwinActionPermissionEntity();
            parentPermission.setId(UUID.randomUUID());
            parentPermission.setTwinAction(TwinAction.DELETE);
            parentPermission.setTwinClassId(parentClassEntity.getId());
            parentPermission.setPermissionId(permissionId);

            Set<UUID> classIds = new HashSet<>();
            classIds.add(twinClassEntity.getId());
            classIds.add(parentClassEntity.getId());

            when(twinActionPermissionRepository.findByTwinClassIdIn(classIds)).thenReturn(List.of(parentPermission));
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(classIds)).thenReturn(List.of());

            twinActionService.loadClassProtectedActions(twinClassEntity);

            assertEquals(parentPermission, twinClassEntity.getActionsProtectedByPermission().get(TwinAction.DELETE));
        }

        @Test
        void loadClassProtectedActions_childAndParentPermission_childOverridesParent() throws ServiceException {
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setActionsProtectedByValidatorRules(null);

            var parentPermissionId = UUID.randomUUID();
            var childPermissionId = UUID.randomUUID();

            var parentPermission = new TwinActionPermissionEntity();
            parentPermission.setId(UUID.randomUUID());
            parentPermission.setTwinAction(TwinAction.DELETE);
            parentPermission.setTwinClassId(parentClassEntity.getId());
            parentPermission.setPermissionId(parentPermissionId);

            var childPermission = new TwinActionPermissionEntity();
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

            twinActionService.loadClassProtectedActions(twinClassEntity);

            assertEquals(childPermissionId,
                    twinClassEntity.getActionsProtectedByPermission().get(TwinAction.DELETE).getPermissionId());
        }
    }

    @Nested
    class Validators {

        @Test
        void loadClassProtectedActions_withValidator_setsValidatorByAction() throws ServiceException {
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setActionsProtectedByValidatorRules(null);

            var validatorRule = new TwinActionValidatorRuleEntity();
            validatorRule.setId(UUID.randomUUID());
            validatorRule.setTwinAction(TwinAction.DELETE);
            validatorRule.setTwinClassId(twinClassEntity.getId());

            when(twinActionPermissionRepository.findByTwinClassIdIn(any())).thenReturn(List.of());
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(any())).thenReturn(List.of(validatorRule));

            twinActionService.loadClassProtectedActions(twinClassEntity);

            var rules = twinClassEntity.getActionsProtectedByValidatorRules().getGrouped(TwinAction.DELETE);
            assertEquals(1, rules.size());
            assertEquals(validatorRule, rules.get(0));
        }

        @Test
        void loadClassProtectedActions_validatorsForDifferentActions_groupedCorrectly() throws ServiceException {
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setActionsProtectedByValidatorRules(null);

            var deleteValidator = new TwinActionValidatorRuleEntity();
            deleteValidator.setId(UUID.randomUUID());
            deleteValidator.setTwinAction(TwinAction.DELETE);
            deleteValidator.setTwinClassId(twinClassEntity.getId());

            var editValidator = new TwinActionValidatorRuleEntity();
            editValidator.setId(UUID.randomUUID());
            editValidator.setTwinAction(TwinAction.EDIT);
            editValidator.setTwinClassId(twinClassEntity.getId());

            when(twinActionPermissionRepository.findByTwinClassIdIn(any())).thenReturn(List.of());
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(any()))
                    .thenReturn(List.of(deleteValidator, editValidator));

            twinActionService.loadClassProtectedActions(twinClassEntity);

            assertFalse(twinClassEntity.getActionsProtectedByValidatorRules().getGrouped(TwinAction.DELETE).isEmpty());
            assertFalse(twinClassEntity.getActionsProtectedByValidatorRules().getGrouped(TwinAction.EDIT).isEmpty());
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void loadClassProtectedActions_emptyExtendedClassIdSet_initializesEmptyKits() throws ServiceException {
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setExtendedClassIdSet(new HashSet<>());

            when(twinActionPermissionRepository.findByTwinClassIdIn(any())).thenReturn(List.of());
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(any())).thenReturn(List.of());

            twinActionService.loadClassProtectedActions(twinClassEntity);

            assertNotNull(twinClassEntity.getActionsProtectedByPermission());
        }

        @Test
        void loadClassProtectedActions_noMatchingRecords_initializesEmptyKits() throws ServiceException {
            twinClassEntity.setActionsProtectedByPermission(null);
            twinClassEntity.setActionsProtectedByValidatorRules(null);

            when(twinActionPermissionRepository.findByTwinClassIdIn(any())).thenReturn(List.of());
            when(twinActionValidatorRuleRepository.findByTwinClassIdIn(any())).thenReturn(List.of());

            twinActionService.loadClassProtectedActions(twinClassEntity);

            assertTrue(twinClassEntity.getActionsProtectedByPermission().isEmpty());
            assertTrue(twinClassEntity.getActionsProtectedByValidatorRules().isEmpty());
        }
    }
}
