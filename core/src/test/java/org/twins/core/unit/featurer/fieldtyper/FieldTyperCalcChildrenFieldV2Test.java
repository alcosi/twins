package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldTyperCalcChildrenFieldV2Test extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    private FieldTyperCalcChildrenFieldV2 fieldTyper;

    @BeforeEach
    void setUp() {
        fieldTyper = new FieldTyperCalcChildrenFieldV2(twinFieldDecimalRepository);
    }

    // V2 persists the computed sum into the twin's own decimal field on serializeValue, then
    // deserializeValue returns whatever was persisted (twinFieldEntity.getValue().toString()).
    // The repository is only touched on the serialize path (getSumResult), not on deserialize.
    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsPersistedValueAsString() throws ServiceException {
            // The persisted BigDecimal is rendered verbatim via .toString() (no scale/round here).
            var twinFieldDecimalEntity = new TwinFieldDecimalEntity();
            twinFieldDecimalEntity.setValue(new BigDecimal("99"));
            var classField = new TwinClassFieldEntity();
            var twinField = new TwinField(null, classField);

            FieldValueText result = fieldTyper.deserializeValue(new Properties(), twinField, twinFieldDecimalEntity);

            assertEquals("99", result.getValue());
        }

        @Test
        void deserializeValue_returnsPersistedDecimalValueAsString() throws ServiceException {
            var twinFieldDecimalEntity = new TwinFieldDecimalEntity();
            twinFieldDecimalEntity.setValue(new BigDecimal("12.50"));
            var classField = new TwinClassFieldEntity();
            var twinField = new TwinField(null, classField);

            FieldValueText result = fieldTyper.deserializeValue(new Properties(), twinField, twinFieldDecimalEntity);

            assertEquals("12.50", result.getValue());
        }
    }
}
