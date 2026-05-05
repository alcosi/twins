package org.twins.core.unit.service.twin;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.action.ActionRestrictionReasonService;
import org.twins.core.service.twin.TwinActionService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TwinActionServiceLoadActionRestrictionReasonsTest extends BaseUnitTest {

    @Mock private ActionRestrictionReasonService actionRestrictionReasonService;
    @InjectMocks private TwinActionService twinActionService;

    private TwinEntity twinEntity1;
    private TwinEntity twinEntity2;
    private TwinEntity twinEntity3;
    private ActionRestrictionReasonEntity reason1;
    private ActionRestrictionReasonEntity reason2;

    @BeforeEach
    void setUp() {
        var reasonId1 = UUID.randomUUID();
        var reasonId2 = UUID.randomUUID();

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

    @Nested
    class Skipped {

        @Test
        void loadActionRestrictionReasons_emptyCollection_skipsQuery() throws ServiceException {
            twinActionService.loadActionRestrictionReasons(Collections.emptyList());

            verify(actionRestrictionReasonService, never()).findEntitiesSafe(any());
        }

        @Test
        void loadActionRestrictionReasons_nullActionsRestricted_skipsQuery() throws ServiceException {
            twinEntity1.setActionsRestricted(null);

            twinActionService.loadActionRestrictionReasons(List.of(twinEntity1));

            assertNull(twinEntity1.getActionsRestrictedReasons());
        }

        @Test
        void loadActionRestrictionReasons_emptyActionsRestricted_skipsQuery() throws ServiceException {
            twinEntity1.setActionsRestricted(new HashMap<>());

            twinActionService.loadActionRestrictionReasons(List.of(twinEntity1));

            assertNull(twinEntity1.getActionsRestrictedReasons());
        }

        @Test
        void loadActionRestrictionReasons_alreadyLoaded_skipsQuery() throws ServiceException {
            var alreadyLoaded = new HashMap<TwinAction, ActionRestrictionReasonEntity>();
            alreadyLoaded.put(TwinAction.DELETE, reason1);
            twinEntity1.setActionsRestrictedReasons(alreadyLoaded);

            twinActionService.loadActionRestrictionReasons(List.of(twinEntity1));

            verify(actionRestrictionReasonService, never()).findEntitiesSafe(any());
            assertSame(alreadyLoaded, twinEntity1.getActionsRestrictedReasons());
        }
    }

    @Nested
    class ReasonMapping {

        @Test
        void loadActionRestrictionReasons_singleTwin_singleReason_populatesMap() throws ServiceException {
            var reasonsKit = new Kit<ActionRestrictionReasonEntity, UUID>(ActionRestrictionReasonEntity::getId);
            reasonsKit.add(reason1);
            when(actionRestrictionReasonService.findEntitiesSafe(any())).thenReturn(reasonsKit);

            twinActionService.loadActionRestrictionReasons(List.of(twinEntity1));

            assertEquals(reason1, twinEntity1.getActionsRestrictedReasons().get(TwinAction.DELETE));
        }

        @Test
        void loadActionRestrictionReasons_multipleTwins_differentReasons_eachPopulated() throws ServiceException {
            var reasonsKit = new Kit<ActionRestrictionReasonEntity, UUID>(ActionRestrictionReasonEntity::getId);
            reasonsKit.add(reason1);
            reasonsKit.add(reason2);
            when(actionRestrictionReasonService.findEntitiesSafe(any())).thenReturn(reasonsKit);

            twinActionService.loadActionRestrictionReasons(List.of(twinEntity1, twinEntity2));

            assertEquals(reason1, twinEntity1.getActionsRestrictedReasons().get(TwinAction.DELETE));
            assertEquals(reason2, twinEntity2.getActionsRestrictedReasons().get(TwinAction.EDIT));
        }

        @Test
        void loadActionRestrictionReasons_sameReasonSharedByTwins_sameInstanceUsed() throws ServiceException {
            var reasonsKit = new Kit<ActionRestrictionReasonEntity, UUID>(ActionRestrictionReasonEntity::getId);
            reasonsKit.add(reason1);
            when(actionRestrictionReasonService.findEntitiesSafe(any())).thenReturn(reasonsKit);

            twinActionService.loadActionRestrictionReasons(List.of(twinEntity1, twinEntity3));

            assertSame(reason1, twinEntity1.getActionsRestrictedReasons().get(TwinAction.DELETE));
            assertSame(reason1, twinEntity3.getActionsRestrictedReasons().get(TwinAction.DELETE));
        }

        @Test
        void loadActionRestrictionReasons_reasonNotFound_doesNotPopulate() throws ServiceException {
            var reasonsKit = new Kit<ActionRestrictionReasonEntity, UUID>(ActionRestrictionReasonEntity::getId);
            when(actionRestrictionReasonService.findEntitiesSafe(any())).thenReturn(reasonsKit);

            twinActionService.loadActionRestrictionReasons(List.of(twinEntity2));

            assertNull(twinEntity2.getActionsRestrictedReasons());
        }

        @Test
        void loadActionRestrictionReasons_multipleActionsOneTwin_allPopulated() throws ServiceException {
            var reasonsKit = new Kit<ActionRestrictionReasonEntity, UUID>(ActionRestrictionReasonEntity::getId);
            reasonsKit.add(reason1);
            reasonsKit.add(reason2);
            when(actionRestrictionReasonService.findEntitiesSafe(any())).thenReturn(reasonsKit);

            twinActionService.loadActionRestrictionReasons(List.of(twinEntity3));

            assertEquals(2, twinEntity3.getActionsRestrictedReasons().size());
            assertEquals(reason1, twinEntity3.getActionsRestrictedReasons().get(TwinAction.DELETE));
            assertEquals(reason2, twinEntity3.getActionsRestrictedReasons().get(TwinAction.EDIT));
        }
    }

    @Test
    void loadActionRestrictionReasons_serviceException_propagates() throws ServiceException {
        when(actionRestrictionReasonService.findEntitiesSafe(any()))
                .thenThrow(new ServiceException(ErrorCodeTwins.ENTITY_INVALID, "Test error"));

        assertThrows(ServiceException.class,
                () -> twinActionService.loadActionRestrictionReasons(List.of(twinEntity1)));
    }
}
