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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumOfSubtractionsByHead;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FieldTyperCalcSumOfSubtractionsByHeadTest extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    private FieldTyperCalcSumOfSubtractionsByHead fieldTyper;

    @BeforeEach
    void setUp() {
        fieldTyper = new FieldTyperCalcSumOfSubtractionsByHead(twinFieldDecimalRepository);
    }

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
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var twin = new TwinEntity();
            twin.setTwinFieldCalculated(Map.of(classField.getId(), new BigDecimal("-3")));

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertEquals("-3.00", result.getValue());
        }
    }

    @Nested
    class GetStorage {

        @Test
        void getStorage_returnsSumOfSubtractionsByHeadStorageWithRepositoryInjected() {
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties();

            TwinFieldStorage storage = fieldTyper.getStorage(classField, props);

            assertInstanceOf(TwinFieldStorageCalcSumOfSubtractionsByHead.class, storage);
        }
    }
}
