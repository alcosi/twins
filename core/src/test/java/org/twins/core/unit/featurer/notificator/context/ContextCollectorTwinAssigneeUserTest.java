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


class ContextCollectorTwinAssigneeUserTest extends BaseUnitTest {

    private final ContextCollectorTwinAssigneeUser collector = new ContextCollectorTwinAssigneeUser();

    private UUID userId;
    private UserEntity user;
    private TwinEntity twin;
    private HistoryEntity history;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new UserEntity();
        user.setId(userId);
        user.setName("Assignee User");
        user.setEmail("assignee@test.com");
        user.setAvatar("http://avatar.url/assignee.png");

        twin = new TwinEntity();
        twin.setAssignerUser(user);

        history = new HistoryEntity();
        history.setTwin(twin);
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
        void collectData_collectIdTrue_putsUserId() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, false, false, false));

            assertEquals(userId.toString(), result.get("USER_ID"));
        }
    }

    @Nested
    class CollectName {

        @Test
        void collectData_collectNameTrue_putsUserName() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, true, false, false));

            assertEquals("Assignee User", result.get("USER_NAME"));
        }

        @Test
        void collectData_collectNameTrue_nullName_skipsName() throws Exception {
            user.setName(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, true, false, false));

            assertNull(result.get("USER_NAME"));
        }
    }

    @Nested
    class CollectEmail {

        @Test
        void collectData_collectEmailTrue_putsUserEmail() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, true, false));

            assertEquals("assignee@test.com", result.get("USER_EMAIL"));
        }

        @Test
        void collectData_collectEmailTrue_nullEmail_skipsEmail() throws Exception {
            user.setEmail(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, true, false));

            assertNull(result.get("USER_EMAIL"));
        }
    }

    @Nested
    class CollectAvatar {

        @Test
        void collectData_collectAvatarTrue_putsUserAvatar() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(false, false, false, true));

            assertEquals("http://avatar.url/assignee.png", result.get("USER_AVATAR"));
        }

        @Test
        void collectData_collectAvatarTrue_nullAvatar_skipsAvatar() throws Exception {
            user.setAvatar(null);
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
            assertEquals(userId.toString(), result.get("USER_ID"));
            assertEquals("Assignee User", result.get("USER_NAME"));
            assertEquals("assignee@test.com", result.get("USER_EMAIL"));
            assertEquals("http://avatar.url/assignee.png", result.get("USER_AVATAR"));
        }
    }

    @Nested
    class NullTwin {

        @Test
        void collectData_nullTwin_allEnabled_collectsNothingWithoutNpe() throws Exception {
            history.setTwin(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props(true, true, true, true));

            assertTrue(result.isEmpty());
        }
    }
}
