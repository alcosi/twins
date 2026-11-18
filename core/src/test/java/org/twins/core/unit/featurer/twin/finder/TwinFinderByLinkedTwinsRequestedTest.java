package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderByLinkedTwinsRequestedTest extends BaseUnitTest {

    private static final UUID TASK_CLASS_ID = UUID.fromString("7c027b60-0f6c-445c-9889-8ee3855d2c59");

    private final TwinRepository twinRepository = Mockito.mock(TwinRepository.class);
    private final TwinFinderByLinkedTwinsRequested finder = new TwinFinderByLinkedTwinsRequested();

    private TwinFinderByLinkedTwinsRequested finderWithRepository() {
        ReflectionTestUtils.setField(finder, "twinRepository", twinRepository);
        return finder;
    }

    private Properties properties(String... extraKeyValues) {
        var properties = new Properties();
        properties.setProperty("paramKey", "twinId");
        properties.setProperty("linkedTwinsLimit", "1000"); // production fills the annotation default via FeaturerService.extractProperties
        for (int i = 0; i < extraKeyValues.length; i += 2)
            properties.setProperty(extraKeyValues[i], extraKeyValues[i + 1]);
        return properties;
    }

    @Nested
    class Concat {

        @Test
        void concat_linkedTwinsOfBothDirections_addedAsTwinIds() throws ServiceException {
            var taskTwinId = UUID.randomUUID();
            var linked1 = UUID.randomUUID();
            var linked2 = UUID.randomUUID();
            Mockito.when(twinRepository.findLinkedTwinIdsByTwinId(taskTwinId, 1001))
                    .thenReturn(Set.of(linked1, linked2));

            var twinSearch = new TwinSearch();

            finderWithRepository().concatSearch(properties(), twinSearch, Map.of("twinId", taskTwinId.toString()));

            assertNotNull(twinSearch.getTwinIdList());
            assertEquals(2, twinSearch.getTwinIdList().size());
            assertTrue(twinSearch.getTwinIdList().contains(linked1));
            assertTrue(twinSearch.getTwinIdList().contains(linked2));
        }

        @Test
        void concat_twinClassIdsConfigured_resolvesByClass() throws ServiceException {
            var taskTwinId = UUID.randomUUID();
            var linkedTask = UUID.randomUUID();
            Mockito.when(twinRepository.findLinkedTwinIdsByTwinIdAndTwinClassIds(taskTwinId, Set.of(TASK_CLASS_ID), 1001))
                    .thenReturn(Set.of(linkedTask));

            var twinSearch = new TwinSearch();

            finderWithRepository().concatSearch(properties("twinClassIds", TASK_CLASS_ID.toString()), twinSearch, Map.of("twinId", taskTwinId.toString()));

            Mockito.verify(twinRepository).findLinkedTwinIdsByTwinIdAndTwinClassIds(taskTwinId, Set.of(TASK_CLASS_ID), 1001);
            Mockito.verify(twinRepository, Mockito.never()).findLinkedTwinIdsByTwinId(Mockito.any(), Mockito.anyInt());
            assertTrue(twinSearch.getTwinIdList().contains(linkedTask));
        }

        @Test
        void concat_noLinkedTwins_addsNullifyMarkerInsteadOfAllTwins() throws ServiceException {
            var taskTwinId = UUID.randomUUID();
            Mockito.when(twinRepository.findLinkedTwinIdsByTwinId(taskTwinId, 1001))
                    .thenReturn(Set.of());

            var twinSearch = new TwinSearch();

            finderWithRepository().concatSearch(properties(), twinSearch, Map.of("twinId", taskTwinId.toString()));

            assertNotNull(twinSearch.getTwinIdList());
            assertTrue(twinSearch.getTwinIdList().contains(UuidUtils.NULLIFY_MARKER));
        }

        @Test
        void concat_noLinkedTwinsOfClass_addsNullifyMarker() throws ServiceException {
            var taskTwinId = UUID.randomUUID();
            Mockito.when(twinRepository.findLinkedTwinIdsByTwinIdAndTwinClassIds(taskTwinId, Set.of(TASK_CLASS_ID), 1001))
                    .thenReturn(Set.of());

            var twinSearch = new TwinSearch();

            finderWithRepository().concatSearch(properties("twinClassIds", TASK_CLASS_ID.toString()), twinSearch, Map.of("twinId", taskTwinId.toString()));

            assertTrue(twinSearch.getTwinIdList().contains(UuidUtils.NULLIFY_MARKER));
        }

        @Test
        void paramKeyAnnotationDefaultIsTwinId() throws Exception {
            // Production path fills missing params from the annotation default (FeaturerService.extractProperties),
            // so an empty twin_finder_params hstore still reads the twin id by the default key.
            var annotation = TwinFinderByLinkedTwinsRequested.class
                    .getField("paramKey").getAnnotation(org.cambium.featurer.annotations.FeaturerParam.class);
            assertEquals("twinId", annotation.defaultValue());
        }

        @Test
        void concat_linkIdsAndClassIdsConfigured_resolvesByLinkAndClass() throws ServiceException {
            var taskTwinId = UUID.randomUUID();
            var linkId = UUID.randomUUID();
            var linkedTask = UUID.randomUUID();
            Mockito.when(twinRepository.findLinkedTwinIdsByTwinIdAndTwinClassIdsAndLinkIds(taskTwinId, Set.of(TASK_CLASS_ID), Set.of(linkId), 1001))
                    .thenReturn(Set.of(linkedTask));

            var twinSearch = new TwinSearch();

            finderWithRepository().concatSearch(
                    properties("twinClassIds", TASK_CLASS_ID.toString(), "linkIds", linkId.toString()),
                    twinSearch,
                    Map.of("twinId", taskTwinId.toString()));

            Mockito.verify(twinRepository).findLinkedTwinIdsByTwinIdAndTwinClassIdsAndLinkIds(taskTwinId, Set.of(TASK_CLASS_ID), Set.of(linkId), 1001);
            assertTrue(twinSearch.getTwinIdList().contains(linkedTask));
        }

        @Test
        void concat_linkIdsOnlyConfigured_resolvesByLink() throws ServiceException {
            var taskTwinId = UUID.randomUUID();
            var linkId = UUID.randomUUID();
            var linkedTwin = UUID.randomUUID();
            Mockito.when(twinRepository.findLinkedTwinIdsByTwinIdAndLinkIds(taskTwinId, Set.of(linkId), 1001))
                    .thenReturn(Set.of(linkedTwin));

            var twinSearch = new TwinSearch();

            finderWithRepository().concatSearch(properties("linkIds", linkId.toString()), twinSearch, Map.of("twinId", taskTwinId.toString()));

            Mockito.verify(twinRepository).findLinkedTwinIdsByTwinIdAndLinkIds(taskTwinId, Set.of(linkId), 1001);
            assertTrue(twinSearch.getTwinIdList().contains(linkedTwin));
        }

        @Test
        void concat_linkIdsNoMatches_addsNullifyMarker() throws ServiceException {
            var taskTwinId = UUID.randomUUID();
            var linkId = UUID.randomUUID();
            Mockito.when(twinRepository.findLinkedTwinIdsByTwinIdAndLinkIds(taskTwinId, Set.of(linkId), 1001))
                    .thenReturn(Set.of());

            var twinSearch = new TwinSearch();

            finderWithRepository().concatSearch(properties("linkIds", linkId.toString()), twinSearch, Map.of("twinId", taskTwinId.toString()));

            assertTrue(twinSearch.getTwinIdList().contains(UuidUtils.NULLIFY_MARKER));
        }

        @Test
        void concat_paramMissedAndNotRequired_doesNotModifySearch() throws ServiceException {
            var twinSearch = new TwinSearch();

            finderWithRepository().concatSearch(properties("required", "false"), twinSearch, Map.of());

            assertNull(twinSearch.getTwinIdList());
        }

        @Test
        void concat_paramMissedAndRequired_throwsException() {
            var twinSearch = new TwinSearch();

            var ex = assertThrows(ServiceException.class,
                    () -> finderWithRepository().concatSearch(properties("required", "true"), twinSearch, Map.of()));
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void concat_invalidUuid_throwsException() {
            var twinSearch = new TwinSearch();

            var ex = assertThrows(ServiceException.class,
                    () -> finderWithRepository().concatSearch(properties(), twinSearch, Map.of("twinId", "not-a-uuid")));
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), ex.getErrorCode());
        }

        @Test
        void concat_overLimit_truncatesWithoutNullifyMarker() throws ServiceException {
            var taskTwinId = UUID.randomUUID();
            var linked = Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
            Mockito.when(twinRepository.findLinkedTwinIdsByTwinId(taskTwinId, 3)) // limit 2 + 1 overflow probe
                    .thenReturn(linked);

            var twinSearch = new TwinSearch();

            finderWithRepository().concatSearch(properties("linkedTwinsLimit", "2"), twinSearch, Map.of("twinId", taskTwinId.toString()));

            assertNotNull(twinSearch.getTwinIdList());
            assertEquals(2, twinSearch.getTwinIdList().size());
            assertTrue(linked.containsAll(twinSearch.getTwinIdList())); // the cap keeps a subset, never the nullify marker
        }

        @Test
        void concat_nonPositiveLimit_throwsException() {
            var twinSearch = new TwinSearch();

            var ex = assertThrows(ServiceException.class,
                    () -> finderWithRepository().concatSearch(properties("linkedTwinsLimit", "0"), twinSearch, Map.of("twinId", UUID.randomUUID().toString())));
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), ex.getErrorCode());
        }
    }
}
