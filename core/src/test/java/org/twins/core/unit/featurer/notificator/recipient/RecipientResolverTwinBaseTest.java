package org.twins.core.featurer.notificator.recipient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.history.context.HistoryContextUserChange;
import org.twins.core.dao.history.context.snapshot.UserSnapshot;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.enums.history.HistoryType;

import java.util.HashSet;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RecipientResolverTwinBaseTest extends BaseUnitTest {

    private RecipientResolverTwinBase resolver;

    @BeforeEach
    void setUp() {
        resolver = new RecipientResolverTwinBase();
    }

    private HistoryEntity buildHistory(HistoryType historyType) {
        var actorUserId = UUID.randomUUID();
        var creatorUserId = UUID.randomUUID();
        var assignerUserId = UUID.randomUUID();
        var twin = new TwinEntity();
        twin.setCreatedByUserId(creatorUserId);
        twin.setAssignerUserId(assignerUserId);
        var history = new HistoryEntity();
        history.setTwin(twin);
        history.setActorUserId(actorUserId);
        history.setHistoryType(historyType);
        return history;
    }

    private HistoryEntity buildAssigneeChangedHistory(String oldUserId, String newUserId) {
        var history = buildHistory(HistoryType.assigneeChanged);
        var context = new HistoryContextUserChange();
        var fromUser = new UserSnapshot();
        fromUser.setUserId(oldUserId);
        var toUser = new UserSnapshot();
        toUser.setUserId(newUserId);
        context.setFromUser(fromUser);
        context.setToUser(toUser);
        history.setContext(context);
        return history;
    }

    private Properties buildProperties(boolean actor, boolean creator, boolean assignee,
                                       boolean oldAssignee, boolean newAssignee) {
        var props = new Properties();
        if (actor) props.setProperty("resolveActor", "true");
        if (creator) props.setProperty("resolveCreator", "true");
        if (assignee) props.setProperty("resolveAssignee", "true");
        if (oldAssignee) props.setProperty("resolveOldAssignee", "true");
        if (newAssignee) props.setProperty("resolveNewAssignee", "true");
        return props;
    }

    @Nested
    class ResolveActor {

        @Test
        void resolve_withActorTrue_addsActorUserId() throws Exception {
            var history = buildHistory(HistoryType.twinCreated);
            var recipientIds = new HashSet<UUID>();
            var props = buildProperties(true, false, false, false, false);

            resolver.resolve(history, recipientIds, props);

            assertEquals(1, recipientIds.size());
            assertTrue(recipientIds.contains(history.getActorUserId()));
        }

        @Test
        void resolve_withActorFalse_doesNotAddActorUserId() throws Exception {
            var history = buildHistory(HistoryType.twinCreated);
            var recipientIds = new HashSet<UUID>();
            var props = buildProperties(false, false, false, false, false);

            resolver.resolve(history, recipientIds, props);

            assertTrue(recipientIds.isEmpty());
        }
    }

    @Nested
    class ResolveCreator {

        @Test
        void resolve_withCreatorTrue_addsCreatorUserId() throws Exception {
            var history = buildHistory(HistoryType.twinCreated);
            var recipientIds = new HashSet<UUID>();
            var props = buildProperties(false, true, false, false, false);

            resolver.resolve(history, recipientIds, props);

            assertEquals(1, recipientIds.size());
            assertTrue(recipientIds.contains(history.getTwin().getCreatedByUserId()));
        }
    }

    @Nested
    class ResolveAssignee {

        @Test
        void resolve_withAssigneeTrue_addsAssignerUserId() throws Exception {
            var history = buildHistory(HistoryType.twinCreated);
            var recipientIds = new HashSet<UUID>();
            var props = buildProperties(false, false, true, false, false);

            resolver.resolve(history, recipientIds, props);

            assertEquals(1, recipientIds.size());
            assertTrue(recipientIds.contains(history.getTwin().getAssignerUserId()));
        }
    }

    @Nested
    class ResolveOldAndNewAssignee {

        @Test
        void resolve_assigneeChanged_withOldAndNewAddsBoth() throws Exception {
            var oldUserId = UUID.randomUUID();
            var newUserId = UUID.randomUUID();
            var history = buildAssigneeChangedHistory(oldUserId.toString(), newUserId.toString());
            var recipientIds = new HashSet<UUID>();
            var props = buildProperties(false, false, false, true, true);

            resolver.resolve(history, recipientIds, props);

            assertEquals(2, recipientIds.size());
            assertTrue(recipientIds.contains(oldUserId));
            assertTrue(recipientIds.contains(newUserId));
        }

        @Test
        void resolve_assigneeChanged_withOldOnly_addsOldOnly() throws Exception {
            var oldUserId = UUID.randomUUID();
            var newUserId = UUID.randomUUID();
            var history = buildAssigneeChangedHistory(oldUserId.toString(), newUserId.toString());
            var recipientIds = new HashSet<UUID>();
            var props = buildProperties(false, false, false, true, false);

            resolver.resolve(history, recipientIds, props);

            assertEquals(1, recipientIds.size());
            assertTrue(recipientIds.contains(oldUserId));
        }

        @Test
        void resolve_assigneeChanged_withNullOldUserId_doesNotAddOld() throws Exception {
            var newUserId = UUID.randomUUID();
            var history = buildAssigneeChangedHistory(null, newUserId.toString());
            var recipientIds = new HashSet<UUID>();
            var props = buildProperties(false, false, false, true, false);

            resolver.resolve(history, recipientIds, props);

            assertTrue(recipientIds.isEmpty());
        }

        @Test
        void resolve_assigneeChanged_withNullNewUserId_doesNotAddNew() throws Exception {
            var oldUserId = UUID.randomUUID();
            var history = buildAssigneeChangedHistory(oldUserId.toString(), null);
            var recipientIds = new HashSet<UUID>();
            var props = buildProperties(false, false, false, false, true);

            resolver.resolve(history, recipientIds, props);

            assertTrue(recipientIds.isEmpty());
        }

        @Test
        void resolve_nonAssigneeChanged_doesNotProcessOldNew() throws Exception {
            var history = buildHistory(HistoryType.twinCreated);
            var recipientIds = new HashSet<UUID>();
            var props = buildProperties(false, false, false, true, true);

            resolver.resolve(history, recipientIds, props);

            assertTrue(recipientIds.isEmpty());
        }
    }

    @Nested
    class ResolveAllFlags {

        @Test
        void resolve_allFlagsEnabled_addsAllUserIds() throws Exception {
            var oldUserId = UUID.randomUUID();
            var newUserId = UUID.randomUUID();
            var history = buildAssigneeChangedHistory(oldUserId.toString(), newUserId.toString());
            var recipientIds = new HashSet<UUID>();
            var props = buildProperties(true, true, true, true, true);

            resolver.resolve(history, recipientIds, props);

            assertEquals(5, recipientIds.size());
            assertTrue(recipientIds.contains(history.getActorUserId()));
            assertTrue(recipientIds.contains(history.getTwin().getCreatedByUserId()));
            assertTrue(recipientIds.contains(history.getTwin().getAssignerUserId()));
            assertTrue(recipientIds.contains(oldUserId));
            assertTrue(recipientIds.contains(newUserId));
        }

        @Test
        void resolve_allFlagsDisabled_addsNothing() throws Exception {
            var history = buildHistory(HistoryType.twinCreated);
            var recipientIds = new HashSet<UUID>();
            var props = buildProperties(false, false, false, false, false);

            resolver.resolve(history, recipientIds, props);

            assertTrue(recipientIds.isEmpty());
        }
    }
}
