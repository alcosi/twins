package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorLinkHead;
import org.twins.core.featurer.fieldtyper.value.FieldValueLinkSingle;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperBaseHeadFieldTest extends BaseUnitTest {

    private final FieldTyperBaseHeadField fieldTyper = new FieldTyperBaseHeadField();

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsHeadTwin() throws ServiceException {
            // Intended: BaseHead reads twin.getHeadTwin() into a single-link field value.
            var headTwin = new TwinEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID()).setHeadTwin(headTwin);
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueLinkSingle result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertSame(headTwin, result.getValue());
        }

        @Test
        void deserializeValue_noHead_returnsNull() throws ServiceException {
            // Intended: a twin with no head yields a present field value whose value is null.
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValueLinkSingle result = fieldTyper.deserializeValue(new Properties(), twinField(twin, classField));

            assertNull(result.getValue());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsLinkHeadDescriptor() throws ServiceException {
            // Intended: BaseHead uses a link/head descriptor (it points at the head twin).
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, new Properties());

            assertInstanceOf(FieldDescriptorLinkHead.class, descriptor);
        }
    }
}
