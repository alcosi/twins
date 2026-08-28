package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorStatistic;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperStatisticTest extends BaseUnitTest {

    private final FieldTyperStatistic fieldTyper = new FieldTyperStatistic();

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    private Properties properties(UUID statisticFieldId) {
        var props = new Properties();
        props.setProperty("twinStatisticId", statisticFieldId.toString());
        return props;
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_propagatesStatisticFieldId() throws ServiceException {
            // Intended: the descriptor carries the configured twin-statistic field id.
            var statisticFieldId = UUID.randomUUID();
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            var descriptor = fieldTyper.getFieldDescriptor(classField, properties(statisticFieldId));

            assertInstanceOf(FieldDescriptorStatistic.class, descriptor);
            assertEquals(statisticFieldId, ((FieldDescriptorStatistic) descriptor).getTwinStatisticId());
        }
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_rendersStatisticFieldIdAsText() throws ServiceException {
            // Intended: the field is a pointer to a statistic; its value is the statistic field id rendered as text.
            var statisticFieldId = UUID.randomUUID();
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueText result = fieldTyper.deserializeValue(properties(statisticFieldId), twinField(twin, classField));

            assertEquals(statisticFieldId.toString(), result.getValue());
        }
    }

    @Nested
    class CanSerialize {

        @Test
        void canSerialize_isFalse_statisticIsReadOnly() throws ServiceException {
            // Intended: statistic is an immutable derived field; clients must not write it.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            assertFalse(fieldTyper.canSerialize(classField));
        }
    }
}
