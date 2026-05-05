package org.twins.core.featurer.headhunter;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.headhunter.HeadHunterImpl;

import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;


class HeadHunterImplTest extends BaseUnitTest {

    private final HeadHunterImpl headHunter = new HeadHunterImpl();

    @Nested
    class ExpandValidHeadSearch {

        @Test
        void expandValidHeadSearch_doesNotModifySearch() throws ServiceException {
            var twinClass = new TwinClassEntity();
            var search = new BasicSearch();

            headHunter.expandValidHeadSearch(new Properties(), twinClass, search);

            assertNull(search.getStatusIdList());
            assertNull(search.getStatusIdExcludeList());
        }
    }

    @Nested
    class IsCreatableChildClass {

        @Test
        void isCreatableChildClass_alwaysReturnsTrue() throws ServiceException {
            var twin = new TwinEntity();
            var twinClass = new TwinClassEntity();

            assertTrue(headHunter.isCreatableChildClass(new Properties(), twin, twinClass));
        }
    }
}