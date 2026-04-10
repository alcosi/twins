package org.twins.core.service.twin;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.service.EntitySmartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.action.TwinActionPermissionEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.enums.action.TwinAction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwinActionServiceCheckAllowedTest {

    @Mock
    private EntitySmartService entitySmartService;

    @Mock
    private TwinRepository twinRepository;


    @Spy
    @InjectMocks
    private TwinActionService twinActionService;

    private TwinEntity twinEntity;
    private UUID twinId;
    private TwinClassEntity twinClassEntity;

    @BeforeEach
    void setUp() {
        twinId = UUID.randomUUID();

        twinEntity = new TwinEntity();
        twinEntity.setId(twinId);

        twinClassEntity = new TwinClassEntity();
        twinClassEntity.setId(UUID.randomUUID());
        twinEntity.setTwinClassId(twinClassEntity.getId());
        twinEntity.setTwinClass(twinClassEntity);
    }

    @Nested
    class CheckAllowedByIdTests {
        @Test
        void testCheckAllowedById_ActionAllowed() throws ServiceException {
            // Given
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.DELETE);

            when(entitySmartService.findById(eq(twinId), eq(twinRepository), any()))
                    .thenReturn(twinEntity);

            // When & Then - should not throw exception
            assertDoesNotThrow(() -> twinActionService.checkAllowed(twinId, TwinAction.DELETE));
        }

        @Test
        void testCheckAllowedById_ActionNotAllowed() throws ServiceException {
            // Given
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.EDIT);
            twinEntity.getActions().add(TwinAction.COMMENT);

            when(entitySmartService.findById(eq(twinId), eq(twinRepository), any()))
                    .thenReturn(twinEntity);

            // When & Then
            ServiceException exception = assertThrows(ServiceException.class,
                    () -> twinActionService.checkAllowed(twinId, TwinAction.DELETE));

            assertNotNull(exception);
            assertTrue(exception.getMessage().contains("DELETE"));
        }
    }

    @Nested
    class CheckAllowedByEntityTests {
        @Test
        void testCheckAllowedByEntity_ActionAllowed() throws ServiceException {
            // Given
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.DELETE);

            doNothing().when(twinActionService).loadActions(any(TwinEntity.class));

            // When & Then - should not throw exception
            assertDoesNotThrow(() -> twinActionService.checkAllowed(twinEntity, TwinAction.DELETE));
        }

        @Test
        void testCheckAllowedByEntity_ActionNotAllowed() throws ServiceException {
            // Given
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.EDIT);

            doNothing().when(twinActionService).loadActions(any(TwinEntity.class));

            // When & Then
            ServiceException exception = assertThrows(ServiceException.class,
                    () -> twinActionService.checkAllowed(twinEntity, TwinAction.DELETE));

            assertNotNull(exception);
            assertTrue(exception.getMessage().contains("DELETE"));
            assertTrue(exception.getMessage().contains(twinEntity.logNormal()));
        }
    }

    @Nested
    class CheckAllowedCollectionTests {
        private TwinEntity twin1;
        private TwinEntity twin2;
        private TwinEntity twin3;

        @BeforeEach
        void setUpTwins() {
            twin1 = new TwinEntity();
            twin1.setId(UUID.randomUUID());
            twin1.setActions(new HashSet<>());
            twin1.getActions().add(TwinAction.DELETE);

            twin2 = new TwinEntity();
            twin2.setId(UUID.randomUUID());
            twin2.setActions(new HashSet<>());
            twin2.getActions().add(TwinAction.DELETE);

            twin3 = new TwinEntity();
            twin3.setId(UUID.randomUUID());
            twin3.setActions(new HashSet<>());
            twin3.getActions().add(TwinAction.EDIT);
        }

        @Test
        void testCheckAllowedCollection_AllAllowed() throws ServiceException {
            // Given
            Collection<TwinEntity> entities = List.of(twin1, twin2);

            doNothing().when(twinActionService).loadActions(any(Collection.class));

            // When & Then - should not throw exception
            assertDoesNotThrow(() -> twinActionService.checkAllowed(entities, TwinAction.DELETE));
        }

        @Test
        void testCheckAllowedCollection_OneNotAllowed() throws ServiceException {
            // Given
            Collection<TwinEntity> entities = List.of(twin1, twin2, twin3);

            doNothing().when(twinActionService).loadActions(any(Collection.class));

            // When & Then
            ServiceException exception = assertThrows(ServiceException.class,
                    () -> twinActionService.checkAllowed(entities, TwinAction.DELETE));

            assertNotNull(exception);
            assertTrue(exception.getMessage().contains(twin3.logNormal()));
        }

        @Test
        void testCheckAllowedCollection_EmptyCollection() throws ServiceException {
            // Given
            Collection<TwinEntity> entities = Collections.emptyList();

            // When & Then - should not throw exception for empty collection
            assertDoesNotThrow(() -> twinActionService.checkAllowed(entities, TwinAction.DELETE));
        }
    }

    @Nested
    class IsAllowedTests {
        @Test
        void testIsAllowed_ActionAllowed() throws ServiceException {
            // Given
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.DELETE);

            doNothing().when(twinActionService).loadActions(any(TwinEntity.class));

            // When
            boolean result = twinActionService.isAllowed(twinEntity, TwinAction.DELETE);

            // Then
            assertTrue(result);
        }

        @Test
        void testIsAllowed_ActionNotAllowed() throws ServiceException {
            // Given
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.EDIT);

            doNothing().when(twinActionService).loadActions(any(TwinEntity.class));

            // When
            boolean result = twinActionService.isAllowed(twinEntity, TwinAction.DELETE);

            // Then
            assertFalse(result);
        }

        @Test
        void testIsAllowed_ActionsNotLoaded_LoadsActions() throws ServiceException {
            // Given
            twinEntity.setActions(null);
            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            // When
            boolean result = twinActionService.isAllowed(twinEntity, TwinAction.DELETE);

            // Then - actions should be loaded
            assertNotNull(twinEntity.getActions());
            assertTrue(result); // All actions are allowed when no restrictions
        }

        @Test
        void testIsAllowed_ActionsAlreadyLoaded_DoesNotReload() throws ServiceException {
            // Given
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.EDIT);
            twinEntity.getActions().add(TwinAction.COMMENT);

            doNothing().when(twinActionService).loadActions(any(TwinEntity.class));

            // When
            boolean result = twinActionService.isAllowed(twinEntity, TwinAction.DELETE);

            // Then
            assertEquals(2, twinEntity.getActions().size());
            assertFalse(result);
        }
    }

    @Nested
    class EdgeCasesTests {
        @Test
        void testCheckAllowed_NullActions() throws ServiceException {
            // Given
            twinEntity.setActions(null);
            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            // When & Then - should load actions and check
            assertDoesNotThrow(() -> twinActionService.checkAllowed(twinEntity, TwinAction.DELETE));
        }

        @Test
        void testIsAllowed_MultipleActions() throws ServiceException {
            // Given
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.EDIT);
            twinEntity.getActions().add(TwinAction.DELETE);
            twinEntity.getActions().add(TwinAction.COMMENT);

            doNothing().when(twinActionService).loadActions(any(TwinEntity.class));

            // When
            boolean editAllowed = twinActionService.isAllowed(twinEntity, TwinAction.EDIT);
            boolean deleteAllowed = twinActionService.isAllowed(twinEntity, TwinAction.DELETE);
            boolean moveAllowed = twinActionService.isAllowed(twinEntity, TwinAction.MOVE);

            // Then
            assertTrue(editAllowed);
            assertTrue(deleteAllowed);
            assertFalse(moveAllowed);
        }
    }
}
