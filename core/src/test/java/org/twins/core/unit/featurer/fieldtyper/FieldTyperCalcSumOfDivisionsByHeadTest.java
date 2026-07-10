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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumOfDivisionsByHead;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FieldTyperCalcSumOfDivisionsByHeadTest extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    private FieldTyperCalcSumOfDivisionsByHead fieldTyper;

    @BeforeEach
    void setUp() {
        fieldTyper = new FieldTyperCalcSumOfDivisionsByHead(twinFieldDecimalRepository);
    }

    // SumOf* deserializeValue does NOT recompute here: it reads the precomputed value stored on the
    // twin under this field's class-field id, applies scale+round, and renders it as plain text.
    // Contrast with SumByHead (uses String.valueOf -> renders "null" for missing); SumOf goes through
    // scaleAndRound(...).toPlainString(), so a present value is rendered with the configured scale.
    private Properties properties() {
        var props = new Properties();
        props.setProperty("firstFieldId", UUID.randomUUID().toString());
        props.setProperty("secondFieldId", UUID.randomUUID().toString());
        props.setProperty("decimalPlaces", "2");
        props.setProperty("roundingMode", "HALF_UP");
        return props;
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsCachedCalculatedValueScaledAndRounded() throws ServiceException {
            // The cached precomputed value (15.5) is rendered through scaleAndRound -> "15.50"
            // (NOT "15.5" — scaleAndRound forces 2 decimal places).
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var twin = new TwinEntity();
            twin.setTwinFieldCalculated(Map.of(classField.getId(), new BigDecimal("15.5")));

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertEquals("15.50", result.getValue());
        }

        @Test
        void deserializeValue_roundingAppliedToCachedValue() throws ServiceException {
            // 12.345 with scale=2, HALF_UP -> 12.35. Confirms rounding, not truncation.
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var twin = new TwinEntity();
            twin.setTwinFieldCalculated(Map.of(classField.getId(), new BigDecimal("12.345")));

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertEquals("12.35", result.getValue());
        }
    }

    @Nested
    class GetStorage {

        @Test
        void getStorage_returnsSumOfDivisionsByHeadStorageWithRepositoryInjected() {
            // getStorage must build the ByHead division storage and pass the decimal repository into it.
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties();

            TwinFieldStorage storage = fieldTyper.getStorage(classField, props);

            assertInstanceOf(TwinFieldStorageCalcSumOfDivisionsByHead.class, storage);
        }
    }
}
