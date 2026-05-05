package org.twins.core.featurer.linker;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkerImplTest extends BaseUnitTest {

    private final LinkerImpl linker = new LinkerImpl();

    @Nested
    class ExpandValidLinkedTwinSearch {

        @Test
        void expandValidLinkedTwinSearch_withTwinClass_doesNotModifySearch() throws ServiceException {
            var search = new BasicSearch();

            linker.expandValidLinkedTwinSearch(
                    new Properties(),
                    new TwinClassEntity(),
                    new TwinEntity(),
                    search
            );

            assertNull(search.getStatusIdList());
            assertNull(search.getStatusIdExcludeList());
        }

        @Test
        void expandValidLinkedTwinSearch_withTwinEntity_doesNotModifySearch() {
            var search = new BasicSearch();

            linker.expandValidLinkedTwinSearch(
                    new Properties(),
                    new TwinEntity(),
                    search
            );

            assertNull(search.getStatusIdList());
            assertNull(search.getStatusIdExcludeList());
        }
    }
}
