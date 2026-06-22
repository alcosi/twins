package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcChildrenInStatusCount;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FieldTyperCountChildrenByStatusV1Test extends BaseUnitTest {

    @Mock
    private TwinFieldSimpleRepository twinFieldSimpleRepository;

    private FieldTyperCountChildrenByStatusV1 fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperCountChildrenByStatusV1();
        setField(fieldTyper, "twinFieldSimpleRepository", twinFieldSimpleRepository);
    }

    // V1 is field-injected (@Autowired TwinFieldSimpleRepository), so reflect-in the mock.
    // The autowired field lives on the concrete class itself (not a base), but findField walks up anyway.
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

    private Properties properties() {
        var props = new Properties();
        props.setProperty("childrenTwinStatusIdList", "");
        props.setProperty("exclude", "false");
        return props;
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsCachedCalculatedCountAsString() throws ServiceException {
            // The cached count is a BigDecimal (TwinFieldCalculated map is Map<UUID, BigDecimal>);
            // .toString() renders it verbatim. Here 7 children -> "7".
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var twin = new TwinEntity();
            twin.setTwinFieldCalculated(Map.of(classField.getId(), new BigDecimal("7")));

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertEquals("7", result.getValue());
        }
    }

    @Nested
    class GetStorage {

        @Test
        void getStorage_returnsChildrenInStatusCountStorageWithRepositoryInjected() {
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties();

            TwinFieldStorage storage = fieldTyper.getStorage(classField, props);

            assertInstanceOf(TwinFieldStorageCalcChildrenInStatusCount.class, storage);
        }
    }
}
