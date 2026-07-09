package org.twins.core.featurer.twin.sorter;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.springframework.data.jpa.domain.Specification;
import org.hibernate.query.SortDirection;

import java.util.Properties;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class TwinSorterStubTest extends BaseUnitTest {

    @Mock
    FieldTyper fieldTyper;

    private final TwinSorterStub sorter = new TwinSorterStub();

    @Nested
    class CreateSort {

        @Test
        void createSort_returnsIdentityFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.ASCENDING);

            assertNotNull(sortFn);

            var inputSpec = (Specification<TwinEntity>) (root, query, cb) -> null;
            var result = sortFn.apply(inputSpec);

            assertSame(inputSpec, result);
        }

        @Test
        void createSort_withDescendingDirection_returnsIdentityFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.DESCENDING);

            assertNotNull(sortFn);

            var inputSpec = (Specification<TwinEntity>) (root, query, cb) -> null;
            var result = sortFn.apply(inputSpec);

            assertSame(inputSpec, result);
        }
    }

    @Nested
    class CheckCompatibleSorter {

        @Test
        void checkCompatibleSorter_alwaysReturnsTrue() throws ServiceException {
            assertTrue(sorter.checkCompatibleSorter(fieldTyper));
        }
    }
}
