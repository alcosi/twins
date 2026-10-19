package org.twins.core.featurer.headhunter;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.headhunter.HeadHunterByStatus;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HeadHunterByStatusTest extends BaseUnitTest {

    private final HeadHunterByStatus headHunter = new HeadHunterByStatus();

    private UUID statusId1;
    private UUID statusId2;

    @BeforeEach
    void setUp() {
        statusId1 = UUID.randomUUID();
        statusId2 = UUID.randomUUID();
    }

    private Properties props(String statusIds, boolean exclude) {
        var props = new Properties();
        if (statusIds != null)
            props.put("statusIds", statusIds);
        props.put("excludeStatusInput", String.valueOf(exclude));

        return props;
    }

    @Nested
    class ExpandValidHeadSearch {

        @Test
        void expandValidHeadSearch_excludeFalse_addsToStatusIdList() throws ServiceException {
            var search = new BasicSearch();

            headHunter.expandValidHeadSearch(
                    props(statusId1 + "," + statusId2, false),
                    new TwinClassEntity(),
                    search
            );

            assertNull(search.getStatusIdExcludeList());
            assertEquals(2, search.getStatusIdList().size());
            assertTrue(search.getStatusIdList().contains(statusId1));
            assertTrue(search.getStatusIdList().contains(statusId2));
        }

        @Test
        void expandValidHeadSearch_excludeTrue_addsToStatusIdExcludeList() throws ServiceException {
            var search = new BasicSearch();

            headHunter.expandValidHeadSearch(
                    props(statusId1 + "," + statusId2, true),
                    new TwinClassEntity(),
                    search
            );

            assertNull(search.getStatusIdList());
            assertEquals(2, search.getStatusIdExcludeList().size());
            assertTrue(search.getStatusIdExcludeList().contains(statusId1));
            assertTrue(search.getStatusIdExcludeList().contains(statusId2));
        }

        @Test
        void expandValidHeadSearch_singleStatusId_works() throws ServiceException {
            var search = new BasicSearch();

            headHunter.expandValidHeadSearch(
                    props(statusId1.toString(), false),
                    new TwinClassEntity(),
                    search
            );

            assertEquals(1, search.getStatusIdList().size());
            assertTrue(search.getStatusIdList().contains(statusId1));
        }
    }

    @Nested
    class IsCreatableChildClass {

        @Test
        void isCreatableChildClass_excludeFalse_twinStatusInSet_returnsTrue() throws ServiceException {
            var twin = new TwinEntity();
            twin.setTwinStatusId(statusId1);

            assertTrue(
                    headHunter.isCreatableChildClass(
                            props(statusId1 + "," + statusId2, false),
                            twin,
                            new TwinClassEntity()
                    )
            );
        }

        @Test
        void isCreatableChildClass_excludeFalse_twinStatusNotInSet_returnsFalse() throws ServiceException {
            var twin = new TwinEntity();
            twin.setTwinStatusId(UUID.randomUUID());

            assertFalse(
                    headHunter.isCreatableChildClass(
                            props(statusId1 + "," + statusId2, false),
                            twin,
                            new TwinClassEntity()
                    )
            );
        }

        @Test
        void isCreatableChildClass_excludeTrue_twinStatusInSet_returnsFalse() throws ServiceException {
            var twin = new TwinEntity();
            twin.setTwinStatusId(statusId1);

            assertFalse(
                    headHunter.isCreatableChildClass(
                            props(statusId1 + "," + statusId2, true),
                            twin,
                            new TwinClassEntity()
                    )
            );
        }

        @Test
        void isCreatableChildClass_excludeTrue_twinStatusNotInSet_returnsTrue() throws ServiceException {
            var twin = new TwinEntity();
            twin.setTwinStatusId(UUID.randomUUID());

            assertTrue(
                    headHunter.isCreatableChildClass(
                            props(statusId1 + "," + statusId2, true),
                            twin,
                            new TwinClassEntity()
                    )
            );
        }
    }
}