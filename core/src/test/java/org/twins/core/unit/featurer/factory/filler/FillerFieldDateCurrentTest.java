package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.filler.FillerFieldDateCurrent;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FillerFieldDateCurrentTest extends BaseUnitTest {

    private FillerFieldDateCurrent filler;

    private static final UUID FIELD_ID = UUID.randomUUID();
    private static final TwinClassFieldEntity FIELD = new TwinClassFieldEntity().setId(FIELD_ID);

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldDateCurrent(); // field-injection era: no constructor args, value comes pre-resolved
    }

    private Properties props() {
        var p = new Properties();
        p.setProperty("twinClassFieldId", FIELD_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity().setId(UUID.randomUUID()));
        return new FactoryItem().setOutput(output);
    }

    private FieldValueDate emptyDate() {
        return new FieldValueDate(FIELD, "yyyy-MM-dd");
    }

    private FieldValueDate filledDate(LocalDateTime value) {
        return emptyDate().setDate(value);
    }

    @Nested
    class Fill {
        @Test
        void fill_setsNowWhenEmpty() throws ServiceException {
            var factoryItem = buildFactoryItem();

            filler.fill(props(), factoryItem, null, emptyDate());

            FieldValueDate written = (FieldValueDate) factoryItem.getOutput().getField(FIELD_ID);
            assertNotNull(written);
            assertEquals(LocalDate.now(), written.getDate().toLocalDate());
        }

        @Test
        void fill_skipsWhenAlreadyFilled() throws ServiceException {
            var factoryItem = buildFactoryItem();
            LocalDateTime existing = LocalDateTime.of(2024, 1, 15, 0, 0);

            filler.fill(props(), factoryItem, null, filledDate(existing));

            assertNull(factoryItem.getOutput().getField(FIELD_ID));
        }
    }
}
