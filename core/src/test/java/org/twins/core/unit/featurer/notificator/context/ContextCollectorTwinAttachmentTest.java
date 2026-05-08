package org.twins.core.featurer.notificator.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.attachment.AttachmentService;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ContextCollectorTwinAttachmentTest extends BaseUnitTest {

    private ContextCollectorTwinAttachment collector;

    @Mock
    private AttachmentService attachmentService;

    private TwinEntity twin;
    private HistoryEntity history;

    @BeforeEach
    void setUp() {
        collector = new ContextCollectorTwinAttachment(attachmentService);

        twin = new TwinEntity();
        twin.setId(UUID.randomUUID());

        history = new HistoryEntity();
        history.setTwin(twin);
    }

    private Properties props() {
        var props = new Properties();
        props.put("collectKey", "TWIN_ATTACHMENT_URL");
        return props;
    }

    private Properties propsWithFieldId(UUID fieldId) {
        var props = props();
        props.put("twinClassFieldId", fieldId.toString());
        return props;
    }

    @Nested
    class AttachmentFound {

        @Test
        void collectData_attachmentFound_putsUrl() throws Exception {
            var attachment = new TwinAttachmentEntity();
            when(attachmentService.findFirstAttachment(twin, null)).thenReturn(attachment);
            when(attachmentService.getAttachmentUri(attachment)).thenReturn("http://storage/file.png");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props());

            assertEquals("http://storage/file.png", result.get("TWIN_ATTACHMENT_URL"));
        }

        @Test
        void collectData_customKey_usedForCollection() throws Exception {
            var props = new Properties();
            props.put("collectKey", "MY_ATTACHMENT");
            var attachment = new TwinAttachmentEntity();
            when(attachmentService.findFirstAttachment(twin, null)).thenReturn(attachment);
            when(attachmentService.getAttachmentUri(attachment)).thenReturn("http://storage/file.png");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals("http://storage/file.png", result.get("MY_ATTACHMENT"));
            assertNull(result.get("TWIN_ATTACHMENT_URL"));
        }

        @Test
        void collectData_withFieldId_passesFieldIdToService() throws Exception {
            var fieldId = UUID.randomUUID();
            var attachment = new TwinAttachmentEntity();
            when(attachmentService.findFirstAttachment(twin, fieldId)).thenReturn(attachment);
            when(attachmentService.getAttachmentUri(attachment)).thenReturn("http://storage/field-file.png");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, propsWithFieldId(fieldId));

            assertEquals("http://storage/field-file.png", result.get("TWIN_ATTACHMENT_URL"));
            verify(attachmentService).findFirstAttachment(twin, fieldId);
        }
    }

    @Nested
    class AttachmentNotFound {

        @Test
        void collectData_noAttachment_returnsEmptyContext() throws Exception {
            when(attachmentService.findFirstAttachment(twin, null)).thenReturn(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props());

            assertTrue(result.isEmpty());
        }

        @Test
        void collectData_attachmentFoundButNullUrl_returnsEmptyContext() throws Exception {
            var attachment = new TwinAttachmentEntity();
            when(attachmentService.findFirstAttachment(twin, null)).thenReturn(attachment);
            when(attachmentService.getAttachmentUri(attachment)).thenReturn(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class PreserveContext {

        @Test
        void collectData_preservesExistingContext() throws Exception {
            var attachment = new TwinAttachmentEntity();
            when(attachmentService.findFirstAttachment(twin, null)).thenReturn(attachment);
            when(attachmentService.getAttachmentUri(attachment)).thenReturn("http://storage/file.png");
            var context = new HashMap<String, String>();
            context.put("EXISTING", "value");

            var result = collector.collectData(history, context, props());

            assertEquals("value", result.get("EXISTING"));
            assertEquals("http://storage/file.png", result.get("TWIN_ATTACHMENT_URL"));
        }
    }
}
