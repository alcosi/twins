package org.twins.core.featurer.twin.sorter;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperSelect;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.hibernate.query.SortDirection;
import org.springframework.data.jpa.domain.Specification;

import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinSorterDataListFieldTest extends BaseUnitTest {

    @Mock
    FieldTyperSelect fieldTyperSelect;

    @Mock
    FieldTyper fieldTyperOther;

    private final TwinSorterDataListField sorter = new TwinSorterDataListField();

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
        void checkCompatibleSorter_selectWithDatalistStorage_returnsTrue() throws ServiceException {
            when(fieldTyperSelect.getStorageType()).thenReturn(TwinFieldStorageDatalist.class);

            assertTrue(sorter.checkCompatibleSorter(fieldTyperSelect));
        }

        @Test
        void checkCompatibleSorter_nonSelectFieldTyper_returnsFalse() throws ServiceException {
            assertFalse(sorter.checkCompatibleSorter(fieldTyperOther));
        }

        @Test
        void checkCompatibleSorter_selectWithWrongStorage_returnsFalse() throws ServiceException {
            doReturn(TwinFieldStorageSimple.class).when(fieldTyperSelect).getStorageType();

            assertFalse(sorter.checkCompatibleSorter(fieldTyperSelect));
        }
    }
}
