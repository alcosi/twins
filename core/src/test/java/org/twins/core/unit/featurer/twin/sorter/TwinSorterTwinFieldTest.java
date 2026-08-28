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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTwin;
import org.twins.core.enums.consts.SystemIds;
import org.hibernate.query.SortDirection;
import org.springframework.data.jpa.domain.Specification;

import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinSorterTwinFieldTest extends BaseUnitTest {

    @Mock
    FieldTyper fieldTyperTwin;

    @Mock
    FieldTyper fieldTyperOther;

    private final TwinSorterTwinField sorter = new TwinSorterTwinField();

    @Nested
    class CreateSort {

        @Test
        void createSort_withKnownNameField_returnsNonNullFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setId(SystemIds.TwinClassField.Base.NAME);

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.ASCENDING);

            assertNotNull(sortFn);
        }

        @Test
        void createSort_withKnownDescriptionField_returnsNonNullFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setId(SystemIds.TwinClassField.Base.DESCRIPTION);

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.DESCENDING);

            assertNotNull(sortFn);
        }

        @Test
        void createSort_withKnownExternalIdField_returnsNonNullFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setId(SystemIds.TwinClassField.Base.EXTERNAL_ID);

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.ASCENDING);

            assertNotNull(sortFn);
        }

        @Test
        void createSort_withKnownOwnerUserField_returnsNonNullFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setId(SystemIds.TwinClassField.Base.OWNER_USER_ID);

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.ASCENDING);

            assertNotNull(sortFn);
        }

        @Test
        void createSort_withKnownAssigneeUserField_returnsNonNullFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setId(SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID);

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.ASCENDING);

            assertNotNull(sortFn);
        }

        @Test
        void createSort_withKnownCreatorUserField_returnsNonNullFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setId(SystemIds.TwinClassField.Base.CREATOR_USER_ID);

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.ASCENDING);

            assertNotNull(sortFn);
        }

        @Test
        void createSort_withKnownHeadField_returnsNonNullFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setId(SystemIds.TwinClassField.Base.HEAD_ID);

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.ASCENDING);

            assertNotNull(sortFn);
        }

        @Test
        void createSort_withKnownStatusField_returnsNonNullFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setId(SystemIds.TwinClassField.Base.STATUS_ID);

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.ASCENDING);

            assertNotNull(sortFn);
        }

        @Test
        void createSort_withKnownCreatedAtField_returnsNonNullFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setId(SystemIds.TwinClassField.Base.CREATED_AT);

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.ASCENDING);

            assertNotNull(sortFn);
        }

        @Test
        void createSort_withKnownIdField_returnsNonNullFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setId(SystemIds.TwinClassField.Base.ID);

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.ASCENDING);

            assertNotNull(sortFn);
        }

        @Test
        void createSort_withKnownTwinClassIdField_returnsNonNullFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setId(SystemIds.TwinClassField.Base.TWIN_CLASS_ID);

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.ASCENDING);

            assertNotNull(sortFn);
        }

        @Test
        void createSort_withUnknownField_returnsNonNullFunction() throws ServiceException {
            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setId(UUID.randomUUID());

            Function<Specification<TwinEntity>, Specification<TwinEntity>> sortFn =
                    sorter.createSort(new Properties(), fieldEntity, SortDirection.ASCENDING);

            assertNotNull(sortFn);
        }
    }

    @Nested
    class CheckCompatibleSorter {

        @Test
        void checkCompatibleSorter_twinStorage_returnsTrue() throws ServiceException {
            when(fieldTyperTwin.getStorageType()).thenReturn(TwinFieldStorageTwin.class);

            assertTrue(sorter.checkCompatibleSorter(fieldTyperTwin));
        }

        @Test
        void checkCompatibleSorter_otherStorage_returnsFalse() throws ServiceException {
            when(fieldTyperOther.getStorageType()).thenReturn(TwinFieldStorageBoolean.class);

            assertFalse(sorter.checkCompatibleSorter(fieldTyperOther));
        }
    }
}
