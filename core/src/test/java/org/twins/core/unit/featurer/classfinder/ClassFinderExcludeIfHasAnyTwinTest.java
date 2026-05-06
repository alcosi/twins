package org.twins.core.featurer.classfinder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.twin.TwinSearchService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ClassFinderExcludeIfHasAnyTwinTest extends BaseUnitTest {

    @Mock
    private TwinSearchService twinSearchService;

    private ClassFinderExcludeIfHasAnyTwin classFinder;

    @BeforeEach
    void setUp() {
        classFinder = new ClassFinderExcludeIfHasAnyTwin(twinSearchService);
    }

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_emptyTwinClassIdSet_doesNotModifySearch() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("twinClassIdSet", "");
            properties.setProperty("extendsHierarchyCheck", "false");
            var classSearch = new TwinClassSearch();

            classFinder.concatSearch(properties, classSearch);

            assertNull(classSearch.getTwinClassIdList());
            assertNull(classSearch.getTwinClassIdExcludeList());
            verifyNoInteractions(twinSearchService);
        }

        @Test
        void concatSearch_noTwinsFound_includesAllClasses() throws ServiceException {
            var classId1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var classId2 = UUID.fromString("11111111-2222-3333-4444-555555555555");
            var properties = buildProperties(
                    Set.of(classId1, classId2),
                    false
            );
            var classSearch = new TwinClassSearch();

            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.twinClass)))
                    .thenReturn(Collections.emptyMap());

            classFinder.concatSearch(properties, classSearch);

            assertNotNull(classSearch.getTwinClassIdList());
            assertEquals(2, classSearch.getTwinClassIdList().size());
            assertTrue(classSearch.getTwinClassIdList().contains(classId1));
            assertTrue(classSearch.getTwinClassIdList().contains(classId2));
            assertNull(classSearch.getTwinClassIdExcludeList());
        }

        @Test
        void concatSearch_allClassesHaveTwins_excludesAll() throws ServiceException {
            var classId1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var classId2 = UUID.fromString("11111111-2222-3333-4444-555555555555");
            var properties = buildProperties(
                    Set.of(classId1, classId2),
                    false
            );
            var classSearch = new TwinClassSearch();

            var twinClass1 = buildTwinClassWithExtended(classId1);
            twinClass1.setId(UUID.randomUUID());
            var twinClass2 = buildTwinClassWithExtended(classId2);
            twinClass2.setId(UUID.randomUUID());

            Map<Object, Long> countMap = new LinkedHashMap<>();
            countMap.put(twinClass1, 5L);
            countMap.put(twinClass2, 3L);

            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.twinClass)))
                    .thenReturn(countMap);

            classFinder.concatSearch(properties, classSearch);

            assertNull(classSearch.getTwinClassIdList());
            assertNotNull(classSearch.getTwinClassIdExcludeList());
            assertEquals(2, classSearch.getTwinClassIdExcludeList().size());
            assertTrue(classSearch.getTwinClassIdExcludeList().contains(classId1));
            assertTrue(classSearch.getTwinClassIdExcludeList().contains(classId2));
        }

        @Test
        void concatSearch_mixedResults_excludesOnlyClassesWithTwins() throws ServiceException {
            var classIdWithTwins = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var classIdWithoutTwins = UUID.fromString("11111111-2222-3333-4444-555555555555");
            var properties = buildProperties(
                    Set.of(classIdWithTwins, classIdWithoutTwins),
                    false
            );
            var classSearch = new TwinClassSearch();

            var twinClass = buildTwinClassWithExtended(classIdWithTwins);

            Map<Object, Long> countMap = new LinkedHashMap<>();
            countMap.put(twinClass, 10L);

            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.twinClass)))
                    .thenReturn(countMap);

            classFinder.concatSearch(properties, classSearch);

            assertNotNull(classSearch.getTwinClassIdExcludeList());
            assertTrue(classSearch.getTwinClassIdExcludeList().contains(classIdWithTwins));
            assertNotNull(classSearch.getTwinClassIdList());
            assertTrue(classSearch.getTwinClassIdList().contains(classIdWithoutTwins));
        }

        @Test
        void concatSearch_zeroCountTreatsAsNoTwins() throws ServiceException {
            var classId1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var properties = buildProperties(Set.of(classId1), false);
            var classSearch = new TwinClassSearch();

            var twinClass = buildTwinClassWithExtended(classId1);

            Map<Object, Long> countMap = new LinkedHashMap<>();
            countMap.put(twinClass, 0L);

            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.twinClass)))
                    .thenReturn(countMap);

            classFinder.concatSearch(properties, classSearch);

            assertNotNull(classSearch.getTwinClassIdList());
            assertTrue(classSearch.getTwinClassIdList().contains(classId1));
            assertNull(classSearch.getTwinClassIdExcludeList());
        }

        @Test
        void concatSearch_extendsHierarchyCheck_createsHierarchySearch() throws ServiceException {
            var classId1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var classId2 = UUID.fromString("11111111-2222-3333-4444-555555555555");
            var properties = buildProperties(
                    Set.of(classId1, classId2),
                    true
            );
            var classSearch = new TwinClassSearch();

            var twinClass = buildTwinClassWithExtended(classId1);

            Map<Object, Long> countMap = new LinkedHashMap<>();
            countMap.put(twinClass, 5L);

            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.twinClass)))
                    .thenReturn(countMap);

            classFinder.concatSearch(properties, classSearch);

            HierarchySearch hierarchySearch = classSearch.getExtendsHierarchyChildsForTwinClassSearch();
            assertNotNull(hierarchySearch);
            assertEquals(HierarchySearch.INCLUDE_SELF, hierarchySearch.getDepth());
            assertTrue(hierarchySearch.getIdExcludeList().contains(classId1));
            assertTrue(hierarchySearch.getIdList().contains(classId2));
        }

        @Test
        void concatSearch_extendsHierarchyCheck_existingHierarchySearchWithZeroDepth_throwsException() throws ServiceException {
            var classId1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var properties = buildProperties(Set.of(classId1), true);
            var classSearch = new TwinClassSearch();
            classSearch.setExtendsHierarchyChildsForTwinClassSearch(
                    new HierarchySearch().setDepth(2)
            );

            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.twinClass)))
                    .thenReturn(Collections.emptyMap());

            var exception = assertThrows(ServiceException.class, () ->
                    classFinder.concatSearch(properties, classSearch)
            );

            assertEquals(ErrorCodeTwins.CONFIGURATION_IS_INVALID.getCode(), exception.getErrorCode());
        }

        @Test
        void concatSearch_extendsHierarchyCheck_existingHierarchySearchWithIncludeSelfDepth_succeeds() throws ServiceException {
            var classId1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var properties = buildProperties(Set.of(classId1), true);
            var classSearch = new TwinClassSearch();
            classSearch.setExtendsHierarchyChildsForTwinClassSearch(
                    new HierarchySearch().setDepth(HierarchySearch.INCLUDE_SELF)
            );

            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.twinClass)))
                    .thenReturn(Collections.emptyMap());

            classFinder.concatSearch(properties, classSearch);

            HierarchySearch hierarchySearch = classSearch.getExtendsHierarchyChildsForTwinClassSearch();
            assertNotNull(hierarchySearch);
            assertNotNull(hierarchySearch.getIdList());
            assertTrue(hierarchySearch.getIdList().contains(classId1));
        }

        @Test
        void concatSearch_classMatchesMultipleTargetIds_excludesAllMatched() throws ServiceException {
            var classId1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var classId2 = UUID.fromString("11111111-2222-3333-4444-555555555555");
            var properties = buildProperties(
                    Set.of(classId1, classId2),
                    false
            );
            var classSearch = new TwinClassSearch();

            var twinClass = new TwinClassEntity();
            twinClass.setExtendedClassIdSet(new LinkedHashSet<>(Set.of(classId1, classId2)));

            Map<Object, Long> countMap = new LinkedHashMap<>();
            countMap.put(twinClass, 7L);

            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.twinClass)))
                    .thenReturn(countMap);

            classFinder.concatSearch(properties, classSearch);

            assertNotNull(classSearch.getTwinClassIdExcludeList());
            assertEquals(2, classSearch.getTwinClassIdExcludeList().size());
            assertTrue(classSearch.getTwinClassIdExcludeList().contains(classId1));
            assertTrue(classSearch.getTwinClassIdExcludeList().contains(classId2));
        }
    }

    private Properties buildProperties(Set<UUID> twinClassIds, boolean extendsHierarchyCheck) {
        var properties = new Properties();
        var idString = String.join(", ", twinClassIds.stream().map(UUID::toString).toList());
        properties.setProperty("twinClassIdSet", idString);
        properties.setProperty("extendsHierarchyCheck", String.valueOf(extendsHierarchyCheck));
        return properties;
    }

    private TwinClassEntity buildTwinClassWithExtended(UUID extendedClassId) {
        var twinClass = new TwinClassEntity();
        twinClass.setExtendedClassIdSet(new LinkedHashSet<>(Set.of(extendedClassId)));
        return twinClass;
    }
}
