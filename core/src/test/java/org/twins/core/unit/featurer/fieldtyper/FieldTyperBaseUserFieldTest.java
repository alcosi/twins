package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUser;
import org.twins.core.featurer.fieldtyper.value.FieldValueUserSingle;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.twins.core.enums.consts.SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID;
import static org.twins.core.enums.consts.SystemIds.TwinClassField.Base.CREATOR_USER_ID;
import static org.twins.core.enums.consts.SystemIds.TwinClassField.Base.OWNER_USER_ID;

class FieldTyperBaseUserFieldTest extends BaseUnitTest {

    private final FieldTyperBaseUserField fieldTyper = new FieldTyperBaseUserField();

    @Mock
    private TwinService twinService;

    @BeforeEach
    void setUp() throws Exception {
        setField(fieldTyper, "twinService", twinService);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                var field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_assigneeField_returnsAssignerUser() throws ServiceException {
            // Intended: the ASSIGNEE_USER field reads twin.getAssignerUser() into the field value.
            var user = new UserEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID()).setAssignerUser(user);
            var classField = new TwinClassFieldEntity().setId(ASSIGNEE_USER_ID);

            FieldValueUserSingle result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertSame(user, result.getValue());
        }

        @Test
        void deserializeValue_creatorField_returnsCreatedByUser() throws ServiceException {
            // Intended: the CREATOR_USER field reads twin.getCreatedByUser().
            var user = new UserEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID()).setCreatedByUser(user);
            var classField = new TwinClassFieldEntity().setId(CREATOR_USER_ID);

            FieldValueUserSingle result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertSame(user, result.getValue());
        }

        @Test
        void deserializeValue_ownerField_returnsOwnerUser() throws ServiceException {
            // Intended: the OWNER_USER field reads twin.getOwnerUser().
            var user = new UserEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID()).setOwnerUser(user);
            var classField = new TwinClassFieldEntity().setId(OWNER_USER_ID);

            FieldValueUserSingle result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertSame(user, result.getValue());
        }

        @Test
        void deserializeValue_unknownField_throws() {
            // Intended: only ASSIGNEE/CREATOR/OWNER user fields are supported; anything else is incorrect.
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.deserializeValue(new Properties(), twinField(twin, classField)));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT.getCode(), ex.getErrorCode());
        }
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_assigneeField_setsAssignerUserAndId() throws ServiceException {
            // Intended: serializing an ASSIGNEE value writes user + userId onto the twin.
            var userId = UUID.randomUUID();
            var user = new UserEntity().setId(userId);
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var classField = new TwinClassFieldEntity().setId(ASSIGNEE_USER_ID);
            var value = new FieldValueUserSingle(classField).setValue(user);

            fieldTyper.serializeValue(new Properties(), twin, value, new TwinChangesCollector());

            assertSame(user, twin.getAssignerUser());
            assertEquals(userId, twin.getAssignerUserId());
        }

        @Test
        void serializeValue_ownerField_setsOwnerUserAndId() throws ServiceException {
            // Intended: serializing an OWNER value writes user + userId onto the twin.
            var userId = UUID.randomUUID();
            var user = new UserEntity().setId(userId);
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var classField = new TwinClassFieldEntity().setId(OWNER_USER_ID);
            var value = new FieldValueUserSingle(classField).setValue(user);

            fieldTyper.serializeValue(new Properties(), twin, value, new TwinChangesCollector());

            assertSame(user, twin.getOwnerUser());
            assertEquals(userId, twin.getOwnerUserId());
        }

        @Test
        void serializeValue_assigneeNullValue_clearsAssignerUserId() throws ServiceException {
            // Intended: a cleared assignee (value null) must null out both the user and the userId.
            var twin = new TwinEntity()
                    .setId(UUID.randomUUID())
                    .setAssignerUser(new UserEntity().setId(UUID.randomUUID()))
                    .setAssignerUserId(UUID.randomUUID());
            var classField = new TwinClassFieldEntity().setId(ASSIGNEE_USER_ID);
            var value = new FieldValueUserSingle(classField).setValue(null);

            fieldTyper.serializeValue(new Properties(), twin, value, new TwinChangesCollector());

            assertNull(twin.getAssignerUser());
            assertNull(twin.getAssignerUserId());
        }

        @Test
        void serializeValue_creatorField_throwsImmutable() {
            // Intended: the creator is system-managed; serializing it must throw IMMUTABLE, not mutate the twin.
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var classField = new TwinClassFieldEntity().setId(CREATOR_USER_ID);
            var value = new FieldValueUserSingle(classField).setValue(new UserEntity().setId(UUID.randomUUID()));

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(new Properties(), twin, value, new TwinChangesCollector()));

            assertEquals(ErrorCodeTwins.TWIN_FIELD_IMMUTABLE.getCode(), ex.getErrorCode());
        }

        @Test
        void serializeValue_unknownField_throws() {
            // Intended: only ASSIGNEE/OWNER are writable; any other field id is incorrect.
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var value = new FieldValueUserSingle(classField).setValue(new UserEntity().setId(UUID.randomUUID()));

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(new Properties(), twin, value, new TwinChangesCollector()));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT.getCode(), ex.getErrorCode());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsUserDescriptorNotMultiple() throws ServiceException {
            // Intended: BaseUser descriptor is single-valued (multiple=false).
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, new Properties());

            assertInstanceOf(FieldDescriptorUser.class, descriptor);
            assertFalse(((FieldDescriptorUser) descriptor).multiple());
        }
    }
}
