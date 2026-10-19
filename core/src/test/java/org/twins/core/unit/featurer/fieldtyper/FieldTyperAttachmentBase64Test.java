package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorAttachment;
import org.twins.core.featurer.fieldtyper.value.FieldValueAttachment;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.storage.StorageService;
import org.cambium.common.kit.Kit;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

class FieldTyperAttachmentBase64Test extends BaseUnitTest {

    @Mock
    private AttachmentRestrictionService attachmentRestrictionService;
    @Mock
    private AttachmentService attachmentService;
    @Mock
    private AuthService authService;
    @Mock
    private StorageService storageService;

    private FieldTyperAttachmentBase64 fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperAttachmentBase64(attachmentRestrictionService, attachmentService);
        setField(fieldTyper, "attachmentRestrictionService", attachmentRestrictionService);
        setField(fieldTyper, "authService", authService);
        setField(fieldTyper, "storageService", storageService);
        // lenient: only serializeValue calls loadAttachments; getFieldDescriptor/canSerialize do not.
        lenient().doNothing().when(attachmentService).loadAttachments(any(TwinEntity.class));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    private Properties properties() {
        return new Properties();
    }

    private ApiUser apiUserWithStorage(UUID storageId) throws ServiceException {
        var apiUser = mock(ApiUser.class);
        var domain = new DomainEntity().setAttachmentsStorageId(storageId);
        when(apiUser.getDomain()).thenReturn(domain);
        when(apiUser.getUserId()).thenReturn(UUID.randomUUID());
        when(apiUser.getUser()).thenReturn(null);
        return apiUser;
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_noRestriction_returnsSingleAttachmentDescriptor() throws ServiceException {
            // Intended: with no restrictionId, base64 attachment forces exactly one attachment (min=max=1).
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, properties());

            assertInstanceOf(FieldDescriptorAttachment.class, descriptor);
            var attachment = (FieldDescriptorAttachment) descriptor;
            assertEquals(1, attachment.minCount());
            assertEquals(1, attachment.maxCount());
        }
    }

    @Nested
    class CanSerialize {

        @Test
        void canSerialize_returnsTrue() throws ServiceException {
            // Intended: Base64 attachment supports serialization (unlike the Invisible variant).
            var classField = new TwinClassFieldEntity();

            assertTrue(fieldTyper.canSerialize(classField));
        }
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_newAttachment_dataUriStrippedAndDecoded() throws ServiceException {
            // Intended: a data-URI "data:<mime>;base64,<payload>" is split on the first comma;
            // the payload is base64-decoded into bytes, sized, titled, and a NEW attachment is added
            // (no existing attachment -> addAttachment path, not updateAttachment).
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            twin.setAttachmentKit(new Kit<>(List.of(), TwinAttachmentEntity::getId));
            var storageId = UUID.randomUUID();
            var storage = new StorageEntity().setId(storageId);
            var apiUser = apiUserWithStorage(storageId);
            when(authService.getApiUser()).thenReturn(apiUser);
            when(storageService.findEntitySafe(storageId)).thenReturn(storage);
            doNothing().when(attachmentService).addAttachment(any(TwinAttachmentEntity.class), any(TwinChangesCollector.class));
            // "Hello" base64 -> SGVsbG8=
            var value = new FieldValueAttachment(classField)
                    .setName("greeting.txt")
                    .setBase64Content("data:text/plain;base64,SGVsbG8=");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, value, collector);

            var captor = ArgumentCaptor.forClass(TwinAttachmentEntity.class);
            verify(attachmentService).addAttachment(captor.capture(), eq(collector));
            verify(attachmentService, never()).updateAttachment(any(), any());
            TwinAttachmentEntity captured = captor.getValue();
            assertEquals("greeting.txt", captured.getTitle());
            assertEquals(Long.valueOf(5L), captured.getSize());
            assertEquals(storageId, captured.getStorageId());
            assertEquals(twin.getId(), captured.getTwinId());
            assertEquals(classField.getId(), captured.getTwinClassFieldId());
        }

        @Test
        void serializeValue_emptyContent_skipsSerialization() throws ServiceException {
            // Intended: blank base64 content short-circuits -> no attachment service interaction beyond load.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            twin.setAttachmentKit(new Kit<>(List.of(), TwinAttachmentEntity::getId));
            var value = new FieldValueAttachment(classField).setBase64Content("");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, value, collector);

            verify(attachmentService, never()).addAttachment(any(), any());
            verify(attachmentService, never()).updateAttachment(any(), any());
        }
    }
}
