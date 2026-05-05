package org.twins.core.featurer.linker;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkerByStatusTest extends BaseUnitTest {

    private final LinkerByStatus linker = new LinkerByStatus();

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
    class ExpandValidLinkedTwinSearchWithTwinClass {

        @Test
        void expandValidLinkedTwinSearch_excludeFalse_addsToStatusIdList() throws ServiceException {
            var search = new BasicSearch();

            linker.expandValidLinkedTwinSearch(
                    props(statusId1 + "," + statusId2, false),
                    new TwinClassEntity(),
                    new TwinEntity(),
                    search
            );

            assertNull(search.getStatusIdExcludeList());
            assertEquals(2, search.getStatusIdList().size());
            assertTrue(search.getStatusIdList().contains(statusId1));
            assertTrue(search.getStatusIdList().contains(statusId2));
        }

        @Test
        void expandValidLinkedTwinSearch_excludeTrue_addsToStatusIdExcludeList() throws ServiceException {
            var search = new BasicSearch();

            linker.expandValidLinkedTwinSearch(
                    props(statusId1 + "," + statusId2, true),
                    new TwinClassEntity(),
                    new TwinEntity(),
                    search
            );

            assertNull(search.getStatusIdList());
            assertEquals(2, search.getStatusIdExcludeList().size());
            assertTrue(search.getStatusIdExcludeList().contains(statusId1));
            assertTrue(search.getStatusIdExcludeList().contains(statusId2));
        }
    }

    @Nested
    class ExpandValidLinkedTwinSearchWithTwinEntity {

        @Test
        void expandValidLinkedTwinSearch_excludeFalse_addsToStatusIdList() {
            var search = new BasicSearch();

            linker.expandValidLinkedTwinSearch(
                    props(statusId1 + "," + statusId2, false),
                    new TwinEntity(),
                    search
            );

            assertNull(search.getStatusIdExcludeList());
            assertEquals(2, search.getStatusIdList().size());
            assertTrue(search.getStatusIdList().contains(statusId1));
            assertTrue(search.getStatusIdList().contains(statusId2));
        }

        @Test
        void expandValidLinkedTwinSearch_excludeTrue_addsToStatusIdExcludeList() {
            var search = new BasicSearch();

            linker.expandValidLinkedTwinSearch(
                    props(statusId1 + "," + statusId2, true),
                    new TwinEntity(),
                    search
            );

            assertNull(search.getStatusIdList());
            assertEquals(2, search.getStatusIdExcludeList().size());
            assertTrue(search.getStatusIdExcludeList().contains(statusId1));
            assertTrue(search.getStatusIdExcludeList().contains(statusId2));
        }
    }
}
