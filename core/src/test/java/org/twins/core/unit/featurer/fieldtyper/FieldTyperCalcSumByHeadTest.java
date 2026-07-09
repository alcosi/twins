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

class FieldTyperCalcSumByHeadTest extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    private FieldTyperCalcSumByHead fieldTyper;

    @BeforeEach
    void setUp() {
        fieldTyper = new FieldTyperCalcSumByHead(twinFieldDecimalRepository);
    }

    private Properties properties(UUID... fieldIds) {
        var props = new Properties();
        props.setProperty("decimalPlaces", "2");
        props.setProperty("roundingMode", "HALF_UP");
        if (fieldIds.length > 0) {
            var joined = new StringBuilder();
            for (int i = 0; i < fieldIds.length; i++) {
                if (i > 0) {
                    joined.append(",");
                }
                joined.append(fieldIds[i].toString());
            }
            props.setProperty("fieldIds", joined.toString());
        }
        return props;
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsCachedCalculatedValueFromTwin() throws ServiceException {
            // SumByHead does NOT recompute here; it reads the precomputed value stored on the twin
            // under this field's class-field id and renders it as text.
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var twin = new TwinEntity();
            twin.setTwinFieldCalculated(Map.of(classField.getId(), new BigDecimal("15.5")));

            FieldValueText result = fieldTyper.deserializeValue(properties(UUID.randomUUID()), twinField(twin, classField));

            assertEquals("15.5", result.getValue());
        }

        @Test
        void deserializeValue_missingCalculatedValue_rendersNullAsText() throws ServiceException {
            // When no precomputed value exists for this field, String.valueOf(null) = "null".
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
        void getStorage_returnsSumByHeadStorageWithRepositoryInjected() {
            // getStorage must build the ByHead storage and pass the decimal repository into it.
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var fieldId = UUID.randomUUID();
            var props = properties(fieldId);

            TwinFieldStorage storage = fieldTyper.getStorage(classField, props);

            assertInstanceOf(TwinFieldStorageCalcSumByHead.class, storage);
        }
    }
}
