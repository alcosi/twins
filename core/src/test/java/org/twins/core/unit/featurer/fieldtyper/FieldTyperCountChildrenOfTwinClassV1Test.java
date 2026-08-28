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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcChildrenOfClassCount;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FieldTyperCountChildrenOfTwinClassV1Test extends BaseUnitTest {

    @Mock
    private TwinFieldSimpleRepository twinFieldSimpleRepository;

    private FieldTyperCountChildrenOfTwinClassV1 fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperCountChildrenOfTwinClassV1();
        setField(fieldTyper, "twinFieldSimpleRepository", twinFieldSimpleRepository);
    }

    // Field-injected (@Autowired TwinFieldSimpleRepository); reflect-in the mock.
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

    // twinClassIds is a comma-joined UUID string (FeaturerParamUUIDSet). useExtendsHierarchy toggles
    // whether getStorage builds the lquery (hierarchy) variant or the direct classId-set variant.
    private Properties properties(UUID classId, boolean useExtendsHierarchy) {
        var props = new Properties();
        props.setProperty("twinClassIds", classId.toString());
        props.setProperty("useExtendsHierarchy", String.valueOf(useExtendsHierarchy));
        return props;
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsCachedCalculatedCountAsString() throws ServiceException {
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var twin = new TwinEntity();
            twin.setTwinFieldCalculated(Map.of(classField.getId(), new BigDecimal("15")));

            FieldValueText result = fieldTyper.deserializeValue(properties(UUID.randomUUID(), false), twinField(twin, classField));

            assertEquals("15", result.getValue());
        }
    }

    @Nested
    class GetStorage {

        @Test
        void getStorage_useExtendsHierarchyFalse_returnsClassIdSetStorage() throws ServiceException {
            // Direct match branch: storage built with the classId Set (no lquery).
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties(UUID.randomUUID(), false);

            TwinFieldStorage storage = fieldTyper.getStorage(classField, props);

            assertInstanceOf(TwinFieldStorageCalcChildrenOfClassCount.class, storage);
        }

        @Test
        void getStorage_useExtendsHierarchyTrue_returnsLQueryStorage() throws ServiceException {
            // Hierarchy branch: storage built with an lquery string derived from the class ids.
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties(UUID.randomUUID(), true);

            TwinFieldStorage storage = fieldTyper.getStorage(classField, props);

            assertInstanceOf(TwinFieldStorageCalcChildrenOfClassCount.class, storage);
        }
    }
}
