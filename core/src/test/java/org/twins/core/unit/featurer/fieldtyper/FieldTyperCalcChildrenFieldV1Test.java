package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumByHead;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FieldTyperCalcChildrenFieldV1Test extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    private FieldTyperCalcChildrenFieldV1 fieldTyper;

    @BeforeEach
    void setUp() {
        fieldTyper = new FieldTyperCalcChildrenFieldV1(twinFieldDecimalRepository);
    }

    // V1 is the "on fly" variant: deserializeValue reads the precomputed value cached on the twin
    // (computed elsewhere via getStorage) and renders it through String.valueOf (so a missing value
    // renders "null", not an NPE). getStorage builds a SumByHead storage keyed on the single child field.
    private Properties properties(UUID childClassFieldId) {
        var props = new Properties();
        props.setProperty("childrenTwinClassFieldId", childClassFieldId.toString());
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
        void deserializeValue_returnsCachedCalculatedValueAsString() throws ServiceException {
            // No scaling here (unlike SumOf*); raw String.valueOf of the cached BigDecimal.
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var twin = new TwinEntity();
            twin.setTwinFieldCalculated(Map.of(classField.getId(), new BigDecimal("42")));

            FieldValueText result = fieldTyper.deserializeValue(properties(UUID.randomUUID()), twinField(twin, classField));

            assertEquals("42", result.getValue());
        }

        @Test
        void deserializeValue_missingCalculatedValue_rendersNullAsText() throws ServiceException {
            // String.valueOf(null) == "null". Guards against NPE on absent precomputed value.
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var twin = new TwinEntity();
            twin.setTwinFieldCalculated(Map.of());

            FieldValueText result = fieldTyper.deserializeValue(properties(UUID.randomUUID()), twinField(twin, classField));

            assertEquals("null", result.getValue());
        }
    }

    @Nested
    class GetStorage {

        @Test
        void getStorage_returnsSumByHeadStorageWithSingleChildField() {
            // V1 sums a SINGLE child class-field; the storage receives Set.of(that one field id).
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties(UUID.randomUUID());

            TwinFieldStorage storage = fieldTyper.getStorage(classField, props);

            assertInstanceOf(TwinFieldStorageCalcSumByHead.class, storage);
        }
    }
}
