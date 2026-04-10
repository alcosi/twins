package org.twins.core.service.twin;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySmartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.action.ActionRestrictionReasonService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwinActionServiceLoadActionRestrictionReasonsTest {

    @Mock
    private ActionRestrictionReasonService actionRestrictionReasonService;

    @InjectMocks
    private TwinActionService twinActionService;

    private TwinEntity twinEntity1;
    private TwinEntity twinEntity2;
    private TwinEntity twinEntity3;
    private ActionRestrictionReasonEntity reason1;
    private ActionRestrictionReasonEntity reason2;

    @BeforeEach
    void setUp() {
        UUID reasonId1 = UUID.randomUUID();
        UUID reasonId2 = UUID.randomUUID();

        twinEntity1 = new TwinEntity();
        twinEntity1.setId(UUID.randomUUID());
        twinEntity1.setActionsRestricted(new HashMap<>());
        twinEntity1.getActionsRestricted().put(TwinAction.DELETE, reasonId1);

        twinEntity2 = new TwinEntity();
        twinEntity2.setId(UUID.randomUUID());
        twinEntity2.setActionsRestricted(new HashMap<>());
        twinEntity2.getActionsRestricted().put(TwinAction.EDIT, reasonId2);

        twinEntity3 = new TwinEntity();
        twinEntity3.setId(UUID.randomUUID());
        twinEntity3.setActionsRestricted(new HashMap<>());
        twinEntity3.getActionsRestricted().put(TwinAction.DELETE, reasonId1);
        twinEntity3.getActionsRestricted().put(TwinAction.EDIT, reasonId2);

        reason1 = new ActionRestrictionReasonEntity();
        reason1.setId(reasonId1);
        reason1.setType("RESTRICTED_BY_PERMISSION");

        reason2 = new ActionRestrictionReasonEntity();
        reason2.setId(reasonId2);
        reason2.setType("RESTRICTED_BY_VALIDATOR");
    }

    @Test
    void testLoadActionRestrictionReasons_EmptyCollection() throws ServiceException {
        // Given
        Collection<TwinEntity> emptyList = Collections.emptyList();

        // When
        twinActionService.loadActionRestrictionReasons(emptyList);

        // Then - no exception thrown
        // Test passes if no exception
    }

    @Test
    void testLoadActionRestrictionReasons_NoActionsRestricted() throws ServiceException {
        // Given
        twinEntity1.setActionsRestricted(null);
        Collection<TwinEntity> entities = List.of(twinEntity1);

        // When
        twinActionService.loadActionRestrictionReasons(entities);

        // Then - actionsRestrictedReasons should remain null
        assertNull(twinEntity1.getActionsRestrictedReasons());
    }

    @Test
    void testLoadActionRestrictionReasons_EmptyActionsRestricted() throws ServiceException {
        // Given
        twinEntity1.setActionsRestricted(new HashMap<>());
        Collection<TwinEntity> entities = List.of(twinEntity1);

        // When
        twinActionService.loadActionRestrictionReasons(entities);

        // Then - actionsRestrictedReasons should remain null
        assertNull(twinEntity1.getActionsRestrictedReasons());
    }

    @Test
    void testLoadActionRestrictionReasons_SingleTwin_SingleReason() throws ServiceException {
        // Given
        Kit<ActionRestrictionReasonEntity, UUID> reasonsKit = new Kit<>(ActionRestrictionReasonEntity::getId);
        reasonsKit.add(reason1);

        when(actionRestrictionReasonService.findEntitiesSafe(any())).thenReturn(reasonsKit);

        Collection<TwinEntity> entities = List.of(twinEntity1);

        // When
        twinActionService.loadActionRestrictionReasons(entities);

        // Then
        assertNotNull(twinEntity1.getActionsRestrictedReasons());
        assertEquals(1, twinEntity1.getActionsRestrictedReasons().size());
        assertEquals(reason1, twinEntity1.getActionsRestrictedReasons().get(TwinAction.DELETE));
    }

    @Test
    void testLoadActionRestrictionReasons_MultipleTwins_DifferentReasons() throws ServiceException {
        // Given
        Kit<ActionRestrictionReasonEntity, UUID> reasonsKit = new Kit<>(ActionRestrictionReasonEntity::getId);
        reasonsKit.add(reason1);
        reasonsKit.add(reason2);

        when(actionRestrictionReasonService.findEntitiesSafe(any())).thenReturn(reasonsKit);

        Collection<TwinEntity> entities = List.of(twinEntity1, twinEntity2);

        // When
        twinActionService.loadActionRestrictionReasons(entities);

        // Then
        assertEquals(reason1, twinEntity1.getActionsRestrictedReasons().get(TwinAction.DELETE));
        assertEquals(reason2, twinEntity2.getActionsRestrictedReasons().get(TwinAction.EDIT));
    }

    @Test
    void testLoadActionRestrictionReasons_SameReasonSharedByTwins() throws ServiceException {
        // Given
        Kit<ActionRestrictionReasonEntity, UUID> reasonsKit = new Kit<>(ActionRestrictionReasonEntity::getId);
        reasonsKit.add(reason1);

        when(actionRestrictionReasonService.findEntitiesSafe(any())).thenReturn(reasonsKit);

        Collection<TwinEntity> entities = List.of(twinEntity1, twinEntity3);

        // When
        twinActionService.loadActionRestrictionReasons(entities);

        // Then
        assertSame(reason1, twinEntity1.getActionsRestrictedReasons().get(TwinAction.DELETE));
        assertSame(reason1, twinEntity3.getActionsRestrictedReasons().get(TwinAction.DELETE));
    }

    @Test
    void testLoadActionRestrictionReasons_ReasonNotFound() throws ServiceException {
        // Given
        Kit<ActionRestrictionReasonEntity, UUID> reasonsKit = new Kit<>(ActionRestrictionReasonEntity::getId);
        // reason2 not added to kit - simulating "not found"

        when(actionRestrictionReasonService.findEntitiesSafe(any())).thenReturn(reasonsKit);

        Collection<TwinEntity> entities = List.of(twinEntity2);

        // When
        twinActionService.loadActionRestrictionReasons(entities);

        // Then - should not add to actionsRestrictedReasons if reason not found
        assertNull(twinEntity2.getActionsRestrictedReasons());
    }

    @Test
    void testLoadActionRestrictionReasons_MultipleActions_OneTwin() throws ServiceException {
        // Given
        Kit<ActionRestrictionReasonEntity, UUID> reasonsKit = new Kit<>(ActionRestrictionReasonEntity::getId);
        reasonsKit.add(reason1);
        reasonsKit.add(reason2);

        when(actionRestrictionReasonService.findEntitiesSafe(any())).thenReturn(reasonsKit);

        Collection<TwinEntity> entities = List.of(twinEntity3);

        // When
        twinActionService.loadActionRestrictionReasons(entities);

        // Then
        assertEquals(2, twinEntity3.getActionsRestrictedReasons().size());
        assertEquals(reason1, twinEntity3.getActionsRestrictedReasons().get(TwinAction.DELETE));
        assertEquals(reason2, twinEntity3.getActionsRestrictedReasons().get(TwinAction.EDIT));
    }

    @Test
    void testLoadActionRestrictionReasons_DoesNotLoadIfAlreadyLoaded() throws ServiceException {
        // Given
        Map<TwinAction, ActionRestrictionReasonEntity> alreadyLoaded = new HashMap<>();
        alreadyLoaded.put(TwinAction.DELETE, reason1);
        twinEntity1.setActionsRestrictedReasons(alreadyLoaded);

        Collection<TwinEntity> entities = List.of(twinEntity1);

        // When
        twinActionService.loadActionRestrictionReasons(entities);

        // Then - should not call findEntitiesSafe because actionsRestrictedReasons is already loaded
        verify(actionRestrictionReasonService, never()).findEntitiesSafe(any());
        assertSame(alreadyLoaded, twinEntity1.getActionsRestrictedReasons());
    }

    @Test
    void testLoadActionRestrictionReasons_ServiceException() throws ServiceException {
        // Given
        when(actionRestrictionReasonService.findEntitiesSafe(any()))
                .thenThrow(new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "Test error"));

        Collection<TwinEntity> entities = List.of(twinEntity1);

        // When & Then
        assertThrows(ServiceException.class, () -> {
            twinActionService.loadActionRestrictionReasons(entities);
        });
    }
}
