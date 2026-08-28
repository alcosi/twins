package org.twins.core.featurer.twin.sorter;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageBoolean;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.hibernate.query.SortDirection;
import org.springframework.data.jpa.domain.Specification;

import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinSorterTextFieldTest extends BaseUnitTest {

    @Mock
    FieldTyper fieldTyperSimple;

    @Mock
    FieldTyper fieldTyperOther;

    private final TwinSorterTextField sorter = new TwinSorterTextField();

    @Nested
    class CreateSort {

        @Test
        void createSort_returnsNonNullFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setId(UUID.randomUUID());

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.ASCENDING);

            assertNotNull(sortFn);
        }

        @Test
        void createSort_returnsNonNullFunction_descending() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setId(UUID.randomUUID());

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.DESCENDING);

            assertNotNull(sortFn);
        }
    }

    @Nested
    class CheckCompatibleSorter {

        @Test
        void checkCompatibleSorter_simpleStorage_returnsTrue() throws ServiceException {
            when(fieldTyperSimple.getStorageType()).thenReturn(TwinFieldStorageSimple.class);

            assertTrue(sorter.checkCompatibleSorter(fieldTyperSimple));
        }

        @Test
        void checkCompatibleSorter_otherStorage_returnsFalse() throws ServiceException {
            when(fieldTyperOther.getStorageType()).thenReturn(TwinFieldStorageBoolean.class);

            assertFalse(sorter.checkCompatibleSorter(fieldTyperOther));
        }
    }
}
