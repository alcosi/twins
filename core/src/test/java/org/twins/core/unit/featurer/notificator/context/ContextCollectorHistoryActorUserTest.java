package org.twins.core.featurer.notificator.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class ContextCollectorHistoryActorUserTest extends BaseUnitTest {

    private final ContextCollectorHistoryActorUser collector = new ContextCollectorHistoryActorUser();

    private UUID actorUserId;
    private UserEntity actorUser;
    private HistoryEntity history;

    @BeforeEach
    void setUp() {
        actorUserId = UUID.randomUUID();
        actorUser = new UserEntity();
        actorUser.setId(actorUserId);
        actorUser.setName("Actor User");
        actorUser.setEmail("actor@test.com");
        actorUser.setAvatar("http://avatar.url/actor.png");

        history = new HistoryEntity();
        history.setActorUser(actorUser);
    }

    private Properties props(boolean collectId, boolean collectName, boolean collectEmail, boolean collectAvatar) {
        var props = new Properties();
        props.put("collectId", String.valueOf(collectId));
        props.put("collectIdKey", "USER_ID");
        props.put("collectName", String.valueOf(collectName));
        props.put("collectNameKey", "USER_NAME");
        props.put("collectEmail", String.valueOf(collectEmail));
        props.put("collectEmailKey", "USER_EMAIL");
        props.put("collectAvatar", String.valueOf(collectAvatar));
        props.put("collectAvatarKey", "USER_AVATAR");
        return props;
    }

    @Nested
    class CollectId {

        @Test
        void collectData_collectIdTrue_putsActorUserId() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, false, false, false));

            assertEquals(actorUserId.toString(), result.get("USER_ID"));
        }

        @Test
        void collectData_collectIdFalse_skipsUserId() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, false));

            assertNull(result.get("USER_ID"));
        }
    }

    @Nested
    class CollectName {

        @Test
        void collectData_collectNameTrue_putsActorUserName() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, true, false, false));

            assertEquals("Actor User", result.get("USER_NAME"));
        }

        @Test
        void collectData_collectNameTrue_nullName_skipsName() throws Exception {
            actorUser.setName(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, true, false, false));

            assertNull(result.get("USER_NAME"));
        }
    }

    @Nested
    class CollectEmail {

        @Test
        void collectData_collectEmailTrue_putsActorUserEmail() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, true, false));

            assertEquals("actor@test.com", result.get("USER_EMAIL"));
        }

        @Test
        void collectData_collectEmailTrue_nullEmail_skipsEmail() throws Exception {
            actorUser.setEmail(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, true, false));

            assertNull(result.get("USER_EMAIL"));
        }
    }

    @Nested
    class CollectAvatar {

        @Test
        void collectData_collectAvatarTrue_putsActorUserAvatar() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, true));

            assertEquals("http://avatar.url/actor.png", result.get("USER_AVATAR"));
        }

        @Test
        void collectData_collectAvatarTrue_nullAvatar_skipsAvatar() throws Exception {
            actorUser.setAvatar(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, true));

            assertNull(result.get("USER_AVATAR"));
        }
    }

    @Nested
    class CollectAll {

        @Test
        void collectData_allEnabled_collectsAllFields() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, true, true, true));

            assertEquals(4, result.size());
            assertEquals(actorUserId.toString(), result.get("USER_ID"));
            assertEquals("Actor User", result.get("USER_NAME"));
            assertEquals("actor@test.com", result.get("USER_EMAIL"));
            assertEquals("http://avatar.url/actor.png", result.get("USER_AVATAR"));
        }

        @Test
        void collectData_noneEnabled_returnsEmptyContext() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, false));

            assertTrue(result.isEmpty());
        }

        @Test
        void collectData_customKeys_usedForCollection() throws Exception {
            var props = props(true, true, true, true);
            props.put("collectIdKey", "ACTOR_ID");
            props.put("collectNameKey", "ACTOR_NAME");
            props.put("collectEmailKey", "ACTOR_EMAIL");
            props.put("collectAvatarKey", "ACTOR_AVATAR");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals(actorUserId.toString(), result.get("ACTOR_ID"));
            assertEquals("Actor User", result.get("ACTOR_NAME"));
            assertEquals("actor@test.com", result.get("ACTOR_EMAIL"));
            assertEquals("http://avatar.url/actor.png", result.get("ACTOR_AVATAR"));
            assertNull(result.get("USER_ID"));
            assertNull(result.get("USER_NAME"));
        }
    }

    @Nested
    class Selectivity {

        @Test
        void collectData_multipleUserTypes_usesOnlyActorUser() throws Exception {
            var createdByUserId = UUID.randomUUID();
            var createdByUser = new UserEntity();
            createdByUser.setId(createdByUserId);
            createdByUser.setName("Creator User");

            var assignerUserId = UUID.randomUUID();
            var assignerUser = new UserEntity();
            assignerUser.setId(assignerUserId);
            assignerUser.setName("Assigner User");

            var twin = new TwinEntity();
            twin.setCreatedByUser(createdByUser);
            twin.setAssignerUser(assignerUser);
            history.setTwin(twin);

            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, true, true, true));

            assertEquals(actorUserId.toString(), result.get("USER_ID"));
            assertEquals("Actor User", result.get("USER_NAME"));
            assertEquals("actor@test.com", result.get("USER_EMAIL"));
            assertEquals("http://avatar.url/actor.png", result.get("USER_AVATAR"));
        }
    }
}
