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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumByLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.auth.AuthService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FieldTyperCalcSumByLinkTest extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    @Mock
    private AuthService authService;

    private FieldTyperCalcSumByLink fieldTyper;

    @BeforeEach
    void setUp() {
        fieldTyper = new FieldTyperCalcSumByLink(twinFieldDecimalRepository, authService);
    }

    private Properties properties(UUID linkId, UUID... fieldIds) {
        var props = new Properties();
        props.setProperty("decimalPlaces", "2");
        props.setProperty("roundingMode", "HALF_UP");
        props.setProperty("linkId", linkId.toString());
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
            // SumByLink does NOT recompute here; it reads the precomputed value stored on the twin
            // under this field's class-field id and renders it as text.
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var twin = new TwinEntity();
            twin.setTwinFieldCalculated(Map.of(classField.getId(), new BigDecimal("23.0")));

            FieldValueText result = fieldTyper.deserializeValue(
                    properties(UUID.randomUUID(), UUID.randomUUID()),
                    twinField(twin, classField));

            assertEquals("23.0", result.getValue());
        }

        @Test
        void deserializeValue_missingCalculatedValue_rendersNullAsText() throws ServiceException {
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var twin = new TwinEntity();
            twin.setTwinFieldCalculated(Map.of());

            FieldValueText result = fieldTyper.deserializeValue(
                    properties(UUID.randomUUID(), UUID.randomUUID()),
                    twinField(twin, classField));

            assertEquals("null", result.getValue());
        }
    }

    @Nested
    class GetStorage {

        @Test
        void getStorage_returnsSumByLinkStorageWithRepositoryInjected() {
            // getStorage must build the ByLink storage and pass the decimal repository into it.
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var linkId = UUID.randomUUID();
            var fieldId = UUID.randomUUID();
            var props = properties(linkId, fieldId);

            TwinFieldStorage storage = fieldTyper.getStorage(classField, props);

            assertInstanceOf(TwinFieldStorageCalcSumByLink.class, storage);
        }
    }
}
