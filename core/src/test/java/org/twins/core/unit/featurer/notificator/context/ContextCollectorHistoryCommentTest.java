package org.twins.core.featurer.notificator.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.history.context.HistoryContextComment;
import org.twins.core.dao.history.context.snapshot.CommentSnapshot;

import java.util.HashMap;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


class ContextCollectorHistoryCommentTest extends BaseUnitTest {

    private final ContextCollectorHistoryComment collector = new ContextCollectorHistoryComment();

    private HistoryEntity history;

    @BeforeEach
    void setUp() {
        var comment = new CommentSnapshot();
        comment.setText("This is a test comment");

        var context = new HistoryContextComment();
        context.setComment(comment);

        history = new HistoryEntity();
        history.setContext(context);
    }

    private Properties props() {
        var props = new Properties();
        props.put("collectCommentKey", "COMMENT");
        return props;
    }

    @Nested
    class CollectComment {

        @Test
        void collectData_putsCommentTextWithDefaultKey() throws Exception {
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props());

            assertEquals("This is a test comment", result.get("COMMENT"));
        }

        @Test
        void collectData_customKey_usedForCollection() throws Exception {
            var props = new Properties();
            props.put("collectCommentKey", "MY_COMMENT");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals("This is a test comment", result.get("MY_COMMENT"));
            assertNull(result.get("COMMENT"));
        }

        @Test
        void collectData_preservesExistingContext() throws Exception {
            var context = new HashMap<String, String>();
            context.put("EXISTING", "value");

            var result = collector.collectData(history, context, props());

            assertEquals("value", result.get("EXISTING"));
            assertEquals("This is a test comment", result.get("COMMENT"));
        }
    }

    @Nested
    class NullHandling {

        @Test
        void collectData_nullContext_throwsNullPointerException() {
            history.setContext(null);
            var context = new HashMap<String, String>();

            assertThrows(NullPointerException.class,
                    () -> collector.collectData(history, context, props()));
        }

        @Test
        void collectData_wrongTypeContext_throwsClassCastException() {
            var wrongContext = new org.twins.core.dao.history.context.HistoryContextStatusChange();
            history.setContext(wrongContext);
            var context = new HashMap<String, String>();

            assertThrows(ClassCastException.class,
                    () -> collector.collectData(history, context, props()));
        }

        @Test
        void collectData_nullComment_throwsNullPointerException() {
            var commentContext = new org.twins.core.dao.history.context.HistoryContextComment();
            commentContext.setComment(null);
            history.setContext(commentContext);
            var context = new HashMap<String, String>();

            assertThrows(NullPointerException.class,
                    () -> collector.collectData(history, context, props()));
        }

        @Test
        void collectData_nullCommentText_putsNullInContext() {
            var comment = new CommentSnapshot();
            comment.setText(null);

            var commentContext = new org.twins.core.dao.history.context.HistoryContextComment();
            commentContext.setComment(comment);
            history.setContext(commentContext);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props());

            assertNull(result.get("COMMENT"));
        }
    }
}
