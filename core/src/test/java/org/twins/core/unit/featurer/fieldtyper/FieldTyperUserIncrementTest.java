package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.context.HistoryContextUserMultiChange;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldUserEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUser;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.service.history.HistoryItem;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserFilterService;
import org.twins.core.service.user.UserService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

class FieldTyperUserIncrementTest extends BaseUnitTest {

    @Mock
    private UserFilterService userFilterService;

    @Mock
    private UserService userService;

    @Mock
    private TwinService twinService;

    @Mock
    private HistoryService historyService;

    private FieldTyperUserIncrement fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperUserIncrement();
        setField(fieldTyper, "userFilterService", userFilterService);
        setField(fieldTyper, "userService", userService);
        setField(fieldTyper, "twinService", twinService);
        setField(fieldTyper, "historyService", historyService);
        // serializeValue always asks for a history item, even when history collection is disabled.
        lenient().when(historyService.fieldChangeUserMulti(any()))
                .thenReturn(new HistoryItem<>(HistoryType.fieldChanged, new HistoryContextUserMultiChange()));
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

    private Properties properties(UUID filterId, String longListThreshold) {
        var props = new Properties();
        props.setProperty("userFilterUUID", filterId.toString());
        props.setProperty("longListThreshold", longListThreshold);
        return props;
    }

    private TwinFieldUserEntity storedUser(TwinClassFieldEntity classField, UserEntity user) {
        return new TwinFieldUserEntity()
                .setId(UUID.randomUUID())
                .setUser(user)
                .setUserId(user.getId())
                .setTwinClassFieldId(classField.getId());
    }

    private void mockFindEntitiesSafe(UserEntity... users) throws ServiceException {
        when(userService.findEntitiesSafe(any())).thenReturn(new Kit<>(List.of(users), UserEntity::getId));
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_appendsAllUsersWhenNothingStored() throws ServiceException {
            // Intended: with no stored users, every provided user is appended.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID())
                    .setTwinFieldUserKit(new KitGrouped<>(
                            TwinFieldUserEntity::getId,
                            TwinFieldUserEntity::getTwinClassFieldId));
            var u1 = new UserEntity().setId(UUID.randomUUID());
            var u2 = new UserEntity().setId(UUID.randomUUID());
            var value = new FieldValueUser(classField);
            value.add(u1);
            value.add(u2);
            mockFindEntitiesSafe(u1, u2);

            var collector = new TwinChangesCollector(false);
            fieldTyper.serializeValue(properties(UUID.randomUUID(), "0"), twin, value, collector);

            var saved = collector.getSaveEntities(TwinFieldUserEntity.class);
            assertEquals(2, saved.size());
            assertTrue(collector.getDeletes(TwinFieldUserEntity.class).isEmpty());
        }

        @Test
        void serializeValue_skipsAlreadyStoredUsers_noDuplication() throws ServiceException {
            // Intended: users already in storage are not added a second time.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var u1 = new UserEntity().setId(UUID.randomUUID());
            var u2 = new UserEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID())
                    .setTwinFieldUserKit(new KitGrouped<>(
                            List.of(storedUser(classField, u1)),
                            TwinFieldUserEntity::getId,
                            TwinFieldUserEntity::getTwinClassFieldId));
            var value = new FieldValueUser(classField);
            value.add(u1);
            value.add(u2);
            mockFindEntitiesSafe(u1, u2);

            var collector = new TwinChangesCollector(false);
            fieldTyper.serializeValue(properties(UUID.randomUUID(), "0"), twin, value, collector);

            var savedIds = collector.getSaveEntities(TwinFieldUserEntity.class).stream()
                    .map(TwinFieldUserEntity::getUserId)
                    .toList();
            assertEquals(List.of(u2.getId()), savedIds);
        }

        @Test
        void serializeValue_neverRemovesStoredUsersAbsentFromValue() throws ServiceException {
            // Intended: this is the key difference from FieldTyperUser — stored users that are
            // missing from the incoming value are left untouched (append-only, no deletion).
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var u1 = new UserEntity().setId(UUID.randomUUID());
            var u2 = new UserEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID())
                    .setTwinFieldUserKit(new KitGrouped<>(
                            List.of(storedUser(classField, u1)),
                            TwinFieldUserEntity::getId,
                            TwinFieldUserEntity::getTwinClassFieldId));
            var value = new FieldValueUser(classField);
            value.add(u2); // u1 is intentionally not re-sent
            mockFindEntitiesSafe(u2);

            var collector = new TwinChangesCollector(false);
            fieldTyper.serializeValue(properties(UUID.randomUUID(), "0"), twin, value, collector);

            // u2 appended, u1 NOT deleted
            var savedIds = collector.getSaveEntities(TwinFieldUserEntity.class).stream()
                    .map(TwinFieldUserEntity::getUserId)
                    .toList();
            assertEquals(List.of(u2.getId()), savedIds);
            assertTrue(collector.getDeletes(TwinFieldUserEntity.class).isEmpty());
        }

        @Test
        void serializeValue_emptyValueAppendsNothingAndRemovesNothing() throws ServiceException {
            // Intended: an empty value means "nothing to append" — nothing is added, nothing is removed.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var u1 = new UserEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID())
                    .setTwinFieldUserKit(new KitGrouped<>(
                            List.of(storedUser(classField, u1)),
                            TwinFieldUserEntity::getId,
                            TwinFieldUserEntity::getTwinClassFieldId));
            var value = new FieldValueUser(classField); // empty
            mockFindEntitiesSafe();

            var collector = new TwinChangesCollector(false);
            fieldTyper.serializeValue(properties(UUID.randomUUID(), "0"), twin, value, collector);

            assertTrue(collector.getSaveEntities(TwinFieldUserEntity.class).isEmpty());
            assertTrue(collector.getDeletes(TwinFieldUserEntity.class).isEmpty());
        }
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_loadsStoredUsersForField() throws ServiceException {
            // Intended: deserialization reads the twin's stored user entities for this field into the value.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var u1 = new UserEntity().setId(UUID.randomUUID());
            var u2 = new UserEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var stored1 = storedUser(classField, u1);
            var stored2 = storedUser(classField, u2);
            twin.setTwinFieldUserKit(new KitGrouped<>(
                    List.of(stored1, stored2),
                    TwinFieldUserEntity::getId,
                    TwinFieldUserEntity::getTwinClassFieldId));

            FieldValueUser result = fieldTyper.deserializeValue(properties(UUID.randomUUID(), "0"), twinField(twin, classField));

            // KitGrouped.getGrouped is backed by a HashMap → order is not guaranteed; assert membership.
            assertEquals(2, result.getItems().size());
            assertTrue(result.getItems().contains(u1));
            assertTrue(result.getItems().contains(u2));
        }

        @Test
        void deserializeValue_noStoredUsers_returnsEmptyValue() throws ServiceException {
            // Intended: a twin with no stored users for the field yields an empty (zero-item) value.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity()
                    .setId(UUID.randomUUID())
                    .setTwinFieldUserKit(KitGrouped.EMPTY);

            FieldValueUser result = fieldTyper.deserializeValue(properties(UUID.randomUUID(), "0"), twinField(twin, classField));

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_shortList_eagerlyLoadsValidUsersAndIsAlwaysMultiple() throws ServiceException {
            // Intended: increment is always multi; when the filter result fits under the threshold the users are loaded inline.
            var filterId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var u1 = new UserEntity().setId(UUID.randomUUID());
            when(userFilterService.countFilterResult(filterId)).thenReturn(1);
            when(userFilterService.findUsers(filterId)).thenReturn(List.of(u1));

            var descriptor = (FieldDescriptorUser) fieldTyper.getFieldDescriptor(classField, properties(filterId, "10"));

            assertTrue(descriptor.multiple()); // no "multiple" param — always true
            assertEquals(1, descriptor.validUsers().size());
            assertSame(u1, descriptor.validUsers().get(0));
            assertNull(descriptor.userFilterId());
        }

        @Test
        void getFieldDescriptor_longList_exposesFilterIdInsteadOfUsers() throws ServiceException {
            // Intended: when the filter result exceeds the threshold, only the filter id is exposed (long list).
            var filterId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            when(userFilterService.countFilterResult(filterId)).thenReturn(100);

            var descriptor = (FieldDescriptorUser) fieldTyper.getFieldDescriptor(classField, properties(filterId, "10"));

            assertTrue(descriptor.multiple());
            assertEquals(filterId, descriptor.userFilterId());
            assertTrue(descriptor.validUsers().isEmpty());
        }
    }
}
