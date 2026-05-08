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
import org.twins.core.service.twin.TwinService;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ContextCollectorHeadTwinAttachmentTest extends BaseUnitTest {

    private ContextCollectorHeadTwinAttachment collector;

    @Mock
    private AttachmentService attachmentService;

    @Mock
    private TwinService twinService;

    private TwinEntity twin;
    private TwinEntity headTwin;
    private HistoryEntity history;

    @BeforeEach
    void setUp() {
        collector = new ContextCollectorHeadTwinAttachment(attachmentService, twinService);

        headTwin = new TwinEntity();
        headTwin.setId(UUID.randomUUID());

        twin = new TwinEntity();
        twin.setId(UUID.randomUUID());
        twin.setHeadTwinId(headTwin.getId());

        history = new HistoryEntity();
        history.setTwin(twin);
    }

    private Properties props() {
        var props = new Properties();
        props.put("collectKey", "HEAD_TWIN_ATTACHMENT_URL");
        return props;
    }

    private Properties propsWithFieldId(UUID fieldId) {
        var props = props();
        props.put("headTwinClassFieldId", fieldId.toString());
        return props;
    }

    @Nested
    class AttachmentFound {

        @Test
        void collectData_headTwinHasAttachment_putsUrl() throws Exception {
            twin.setHeadTwin(headTwin);
            var attachment = new TwinAttachmentEntity();
            when(attachmentService.findFirstAttachment(headTwin, null)).thenReturn(attachment);
            when(attachmentService.getAttachmentUri(attachment)).thenReturn("http://storage/head-file.png");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props());

            assertEquals("http://storage/head-file.png", result.get("HEAD_TWIN_ATTACHMENT_URL"));
            verify(twinService).loadHeadForTwin(twin);
        }

        @Test
        void collectData_customKey_usedForCollection() throws Exception {
            var props = new Properties();
            props.put("collectKey", "MY_HEAD_ATTACHMENT");
            twin.setHeadTwin(headTwin);
            var attachment = new TwinAttachmentEntity();
            when(attachmentService.findFirstAttachment(headTwin, null)).thenReturn(attachment);
            when(attachmentService.getAttachmentUri(attachment)).thenReturn("http://storage/head.png");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props);

            assertEquals("http://storage/head.png", result.get("MY_HEAD_ATTACHMENT"));
            assertNull(result.get("HEAD_TWIN_ATTACHMENT_URL"));
        }

        @Test
        void collectData_withFieldId_passesFieldIdToService() throws Exception {
            var fieldId = UUID.randomUUID();
            twin.setHeadTwin(headTwin);
            var attachment = new TwinAttachmentEntity();
            when(attachmentService.findFirstAttachment(headTwin, fieldId)).thenReturn(attachment);
            when(attachmentService.getAttachmentUri(attachment)).thenReturn("http://storage/field.png");
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, propsWithFieldId(fieldId));

            assertEquals("http://storage/field.png", result.get("HEAD_TWIN_ATTACHMENT_URL"));
            verify(attachmentService).findFirstAttachment(headTwin, fieldId);
        }
    }

    @Nested
    class AttachmentNotFound {

        @Test
        void collectData_noAttachment_returnsEmptyContext() throws Exception {
            twin.setHeadTwin(headTwin);
            when(attachmentService.findFirstAttachment(headTwin, null)).thenReturn(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props());

            assertTrue(result.isEmpty());
        }

        @Test
        void collectData_attachmentFoundButNullUrl_returnsEmptyContext() throws Exception {
            twin.setHeadTwin(headTwin);
            var attachment = new TwinAttachmentEntity();
            when(attachmentService.findFirstAttachment(headTwin, null)).thenReturn(attachment);
            when(attachmentService.getAttachmentUri(attachment)).thenReturn(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props());

            assertTrue(result.isEmpty());
        }

        @Test
        void collectData_nullHeadTwin_returnsEmptyContext() throws Exception {
            twin.setHeadTwin(null);
            var context = new HashMap<String, String>();

            var result = collector.collectData(history, context, props());

            assertTrue(result.isEmpty());
            verify(attachmentService, never()).findFirstAttachment(any(), any());
        }
    }

    @Nested
    class PreserveContext {

        @Test
        void collectData_preservesExistingContext() throws Exception {
            twin.setHeadTwin(headTwin);
            var attachment = new TwinAttachmentEntity();
            when(attachmentService.findFirstAttachment(headTwin, null)).thenReturn(attachment);
            when(attachmentService.getAttachmentUri(attachment)).thenReturn("http://storage/head.png");
            var context = new HashMap<String, String>();
            context.put("EXISTING", "value");

            var result = collector.collectData(history, context, props());

            assertEquals("value", result.get("EXISTING"));
            assertEquals("http://storage/head.png", result.get("HEAD_TWIN_ATTACHMENT_URL"));
        }
    }
}
