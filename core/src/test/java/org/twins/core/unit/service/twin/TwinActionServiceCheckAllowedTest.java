package org.twins.core.unit.service.twin;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.service.EntitySmartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.action.TwinActionPermissionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleEntity;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.service.twin.TwinActionService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class TwinActionServiceCheckAllowedTest extends BaseUnitTest {

    @Mock private EntitySmartService entitySmartService;
    @Mock private TwinRepository twinRepository;
    @Spy @InjectMocks private TwinActionService twinActionService;

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
    class CheckAllowedById {

        @Test
        void checkAllowedById_actionAllowed_doesNotThrow() throws ServiceException {
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.DELETE);
            when(entitySmartService.findById(eq(twinId), eq(twinRepository), any()))
                    .thenReturn(twinEntity);

            assertDoesNotThrow(() -> twinActionService.checkAllowed(twinId, TwinAction.DELETE));
        }

        @Test
        void checkAllowedById_actionNotAllowed_throwsWithActionName() throws ServiceException {
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.EDIT);
            twinEntity.getActions().add(TwinAction.COMMENT);
            when(entitySmartService.findById(eq(twinId), eq(twinRepository), any()))
                    .thenReturn(twinEntity);

            var ex = assertThrows(ServiceException.class,
                    () -> twinActionService.checkAllowed(twinId, TwinAction.DELETE));

            assertTrue(ex.getMessage().contains("DELETE"));
        }
    }

    @Nested
    class CheckAllowedByEntity {

        @Test
        void checkAllowedByEntity_actionAllowed_doesNotThrow() throws ServiceException {
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.DELETE);
            doNothing().when(twinActionService).loadActions(any(TwinEntity.class));

            assertDoesNotThrow(() -> twinActionService.checkAllowed(twinEntity, TwinAction.DELETE));
        }

        @Test
        void checkAllowedByEntity_actionNotAllowed_throwsWithTwinInfo() throws ServiceException {
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.EDIT);
            doNothing().when(twinActionService).loadActions(any(TwinEntity.class));

            var ex = assertThrows(ServiceException.class,
                    () -> twinActionService.checkAllowed(twinEntity, TwinAction.DELETE));

            assertTrue(ex.getMessage().contains("DELETE"));
            assertTrue(ex.getMessage().contains(twinEntity.logShort()));
        }
    }

    @Nested
    class CheckAllowedCollection {

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
        void checkAllowedCollection_allAllowed_doesNotThrow() throws ServiceException {
            doNothing().when(twinActionService).loadActions(any(Collection.class));

            assertDoesNotThrow(() -> twinActionService.checkAllowed(List.of(twin1, twin2), TwinAction.DELETE));
        }

        @Test
        void checkAllowedCollection_oneNotAllowed_throwsWithTwinInfo() throws ServiceException {
            doNothing().when(twinActionService).loadActions(any(Collection.class));

            var ex = assertThrows(ServiceException.class,
                    () -> twinActionService.checkAllowed(List.of(twin1, twin2, twin3), TwinAction.DELETE));

            assertTrue(ex.getMessage().contains(twin3.logShort()));
        }

        @Test
        void checkAllowedCollection_emptyCollection_doesNotThrow() throws ServiceException {
            assertDoesNotThrow(() -> twinActionService.checkAllowed(Collections.emptyList(), TwinAction.DELETE));
        }
    }

    @Nested
    class IsAllowed {

        @Test
        void isAllowed_actionAllowed_returnsTrue() throws ServiceException {
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.DELETE);
            doNothing().when(twinActionService).loadActions(any(TwinEntity.class));

            assertTrue(twinActionService.isAllowed(twinEntity, TwinAction.DELETE));
        }

        @Test
        void isAllowed_actionNotAllowed_returnsFalse() throws ServiceException {
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.EDIT);
            doNothing().when(twinActionService).loadActions(any(TwinEntity.class));

            assertFalse(twinActionService.isAllowed(twinEntity, TwinAction.DELETE));
        }

        @Test
        void isAllowed_actionsNotLoaded_loadsAndReturnsTrue() throws ServiceException {
            twinEntity.setActions(null);
            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            var result = twinActionService.isAllowed(twinEntity, TwinAction.DELETE);

            assertNotNull(twinEntity.getActions());
            assertTrue(result);
        }

        @Test
        void isAllowed_actionsAlreadyLoaded_doesNotReload() throws ServiceException {
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.EDIT);
            twinEntity.getActions().add(TwinAction.COMMENT);
            doNothing().when(twinActionService).loadActions(any(TwinEntity.class));

            var result = twinActionService.isAllowed(twinEntity, TwinAction.DELETE);

            assertEquals(2, twinEntity.getActions().size());
            assertFalse(result);
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void checkAllowed_nullActions_loadsAndDoesNotThrow() throws ServiceException {
            twinEntity.setActions(null);
            twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
            twinClassEntity.setActionsProtectedByValidatorRules(
                    new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));

            assertDoesNotThrow(() -> twinActionService.checkAllowed(twinEntity, TwinAction.DELETE));
        }

        @Test
        void isAllowed_multipleActionsLoaded_returnsCorrectPerAction() throws ServiceException {
            twinEntity.setActions(new HashSet<>());
            twinEntity.getActions().add(TwinAction.EDIT);
            twinEntity.getActions().add(TwinAction.DELETE);
            twinEntity.getActions().add(TwinAction.COMMENT);
            doNothing().when(twinActionService).loadActions(any(TwinEntity.class));

            assertTrue(twinActionService.isAllowed(twinEntity, TwinAction.EDIT));
            assertTrue(twinActionService.isAllowed(twinEntity, TwinAction.DELETE));
            assertFalse(twinActionService.isAllowed(twinEntity, TwinAction.MOVE));
        }
    }
}
