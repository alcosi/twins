package org.twins.core.featurer.classfield.sorter;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;


class FieldSorterByOrderedIdsTest extends BaseUnitTest {

    private final FieldSorterByOrderedIds sorter = new FieldSorterByOrderedIds();

    private UUID fieldId1;
    private UUID fieldId2;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        fieldId1 = UUID.randomUUID();
        fieldId2 = UUID.randomUUID();
    }

    private Properties props(String fieldIds) {
        var props = new Properties();
        if (fieldIds != null)
            props.put("fieldIds", fieldIds);
        return props;
    }

    @Nested
    class CreateSort {

        @Test
        void createSort_returnsNonNullFunction() throws ServiceException {
            var result = sorter.createSort(props(fieldId1 + ", " + fieldId2));

            assertNotNull(result);
        }

        @Test
        void createSort_emptyFieldIds_returnsNonNullFunction() throws ServiceException {
            var result = sorter.createSort(props(""));

            assertNotNull(result);
        }

        @Test
        void createSort_singleFieldId_returnsNonNullFunction() throws ServiceException {
            var result = sorter.createSort(props(fieldId1.toString()));

            assertNotNull(result);
        }
    }

    @Nested
    class SortFunction {

        @Test
        void sortFunction_withNullBaseSpec_returnsNonNullSpecification() throws ServiceException {
            Function<Specification<TwinClassFieldEntity>, Specification<TwinClassFieldEntity>> sortFunction =
                    sorter.createSort(props(fieldId1 + ", " + fieldId2));

            var resultSpec = sortFunction.apply(null);

            assertNotNull(resultSpec);
        }

        @Test
        void sortFunction_withBaseSpec_returnsCombinedSpecification() throws ServiceException {
            Function<Specification<TwinClassFieldEntity>, Specification<TwinClassFieldEntity>> sortFunction =
                    sorter.createSort(props(fieldId1 + ", " + fieldId2));
            Specification<TwinClassFieldEntity> baseSpec = (root, query, cb) -> cb.isNotNull(root);

            var resultSpec = sortFunction.apply(baseSpec);

            assertNotNull(resultSpec);
        }
    }
}
