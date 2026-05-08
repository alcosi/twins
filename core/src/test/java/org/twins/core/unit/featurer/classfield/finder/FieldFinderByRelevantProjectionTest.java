package org.twins.core.featurer.classfield.finder;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.enums.projection.ProjectionFieldSelector;
import org.twins.core.service.projection.ProjectionTypeService;
import org.twins.core.service.twin.TwinSearchService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldFinderByRelevantProjectionTest extends BaseUnitTest {

    @Mock
    private ProjectionTypeService projectionTypeService;

    @Mock
    private TwinSearchService twinSearchService;

    private FieldFinderByRelevantProjection finder;

    private UUID groupId;
    private UUID twinClassId;
    private UUID projectionTypeId;

    @BeforeEach
    void setUp() {
        finder = new FieldFinderByRelevantProjection(projectionTypeService, twinSearchService);

        groupId = UUID.randomUUID();
        twinClassId = UUID.randomUUID();
        projectionTypeId = UUID.randomUUID();
    }

    private ProjectionTypeEntity projectionType(UUID id, UUID membershipTwinClassId) {
        var entity = new ProjectionTypeEntity();
        entity.setId(id);
        entity.setMembershipTwinClassId(membershipTwinClassId);
        return entity;
    }

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_foundRelevantProjections_setsFieldProjectionSearch() throws ServiceException {
            var projectionType = projectionType(projectionTypeId, twinClassId);
            var grouped = new KitGrouped<ProjectionTypeEntity, UUID, UUID>(
                    List.of(projectionType),
                    ProjectionTypeEntity::getId,
                    ProjectionTypeEntity::getMembershipTwinClassId
            );

            var twin = new TwinEntity();
            twin.setTwinClassId(twinClassId);

            var properties = new Properties();
            properties.setProperty("projectionTypeGroupIds", groupId.toString());

            when(projectionTypeService.findAndGroupByTwinClassId(Set.of(groupId))).thenReturn(grouped);
            when(twinSearchService.findTwins(any())).thenReturn(List.of(twin));

            var search = new TwinClassFieldSearch();
            finder.concatSearch(properties, search, Map.of());

            assertNotNull(search.getFieldProjectionSearch());
            assertEquals(ProjectionFieldSelector.src, search.getFieldProjectionSearch().getProjectionFieldSelector());
            assertTrue(search.getFieldProjectionSearch().getProjectionTypeIdList().contains(projectionTypeId));
        }

        @Test
        void concatSearch_emptyGroupedProjections_doesNotSetFieldProjectionSearch() throws ServiceException {
            var grouped = new KitGrouped<ProjectionTypeEntity, UUID, UUID>(
                    Collections.emptyList(),
                    ProjectionTypeEntity::getId,
                    ProjectionTypeEntity::getMembershipTwinClassId
            );

            var properties = new Properties();
            properties.setProperty("projectionTypeGroupIds", groupId.toString());

            when(projectionTypeService.findAndGroupByTwinClassId(Set.of(groupId))).thenReturn(grouped);

            var search = new TwinClassFieldSearch();
            finder.concatSearch(properties, search, Map.of());

            assertNull(search.getFieldProjectionSearch());
        }

        @Test
        void concatSearch_noMatchingTwins_emptyProjectionIds() throws ServiceException {
            var otherClassId = UUID.randomUUID();
            var projectionType = projectionType(projectionTypeId, twinClassId);
            var grouped = new KitGrouped<ProjectionTypeEntity, UUID, UUID>(
                    List.of(projectionType),
                    ProjectionTypeEntity::getId,
                    ProjectionTypeEntity::getMembershipTwinClassId
            );

            var twin = new TwinEntity();
            twin.setTwinClassId(otherClassId);

            var properties = new Properties();
            properties.setProperty("projectionTypeGroupIds", groupId.toString());

            when(projectionTypeService.findAndGroupByTwinClassId(Set.of(groupId))).thenReturn(grouped);
            when(twinSearchService.findTwins(any())).thenReturn(List.of(twin));

            var search = new TwinClassFieldSearch();
            finder.concatSearch(properties, search, Map.of());

            assertNotNull(search.getFieldProjectionSearch());
            assertTrue(search.getFieldProjectionSearch().getProjectionTypeIdList().isEmpty());
        }

        @Test
        void concatSearch_multipleProjectionTypes_collectsAllRelevant() throws ServiceException {
            var projectionTypeId2 = UUID.randomUUID();
            var twinClassId2 = UUID.randomUUID();
            var projectionType1 = projectionType(projectionTypeId, twinClassId);
            var projectionType2 = projectionType(projectionTypeId2, twinClassId2);
            var grouped = new KitGrouped<ProjectionTypeEntity, UUID, UUID>(
                    List.of(projectionType1, projectionType2),
                    ProjectionTypeEntity::getId,
                    ProjectionTypeEntity::getMembershipTwinClassId
            );

            var twin1 = new TwinEntity();
            twin1.setTwinClassId(twinClassId);
            var twin2 = new TwinEntity();
            twin2.setTwinClassId(twinClassId2);

            var properties = new Properties();
            properties.setProperty("projectionTypeGroupIds", groupId.toString());

            when(projectionTypeService.findAndGroupByTwinClassId(Set.of(groupId))).thenReturn(grouped);
            when(twinSearchService.findTwins(any())).thenReturn(List.of(twin1, twin2));

            var search = new TwinClassFieldSearch();
            finder.concatSearch(properties, search, Map.of());

            assertEquals(2, search.getFieldProjectionSearch().getProjectionTypeIdList().size());
            assertTrue(search.getFieldProjectionSearch().getProjectionTypeIdList().contains(projectionTypeId));
            assertTrue(search.getFieldProjectionSearch().getProjectionTypeIdList().contains(projectionTypeId2));
        }
    }
}
