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
import org.twins.core.featurer.factory.filler.FillerFieldCleaner;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FillerFieldCleanerTest extends BaseUnitTest {

    private FillerFieldCleaner filler;

    private static final UUID FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        filler = new FillerFieldCleaner();
    }

    private Properties props() {
        var p = new Properties();
        p.setProperty("twinClassFieldId", FIELD_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem() {
        var twin = new TwinEntity().setId(UUID.randomUUID());
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        return new FactoryItem().setOutput(output);
    }

    @Nested
    class Fill {

        @Test
        void fill_presentField_isClearedAndAddedToOutput() throws ServiceException {
            // NAME promises: cleaner CLEARS the named field on the output twin.
            var factoryItem = buildFactoryItem();
            var fieldEntity = new TwinClassFieldEntity().setId(FIELD_ID);
            var fieldValue = new FieldValueText(fieldEntity).setValue("v");

            filler.fill(props(), factoryItem, null, fieldValue);

            FieldValue stored = factoryItem.getOutput().getField(FIELD_ID);
            assertSame(fieldValue, stored);
            // After clear(), FieldValueSimple has value=null and state=CLEARED -> isEmpty()==true (isCleared()==true).
            assertTrue(stored.isCleared());
        }

        @Test
        void fill_emptyField_isClearedAndAddedToOutput() throws ServiceException {
            // the lookuper returns a (possibly empty) field value; the cleaner clears it and adds it to output.
            var factoryItem = buildFactoryItem();
            var fieldEntity = new TwinClassFieldEntity().setId(FIELD_ID);
            var fieldValue = new FieldValueText(fieldEntity); // undefined/empty

            filler.fill(props(), factoryItem, null, fieldValue);

            var stored = factoryItem.getOutput().getField(FIELD_ID);
            assertSame(fieldValue, stored);
            assertTrue(stored.isCleared());
        }
    }
}
