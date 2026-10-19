package org.twins.core.unit.mappers.rest.link;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.link.RelationTwinFieldsConverter;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapperV2;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Relation twin field conversion (shared by the v1 and temporal v2 link add mappers):
 * link resolution goes through the MapperContext batch cache with a single-load fallback.
 */
@ExtendWith(MockitoExtension.class)
class RelationTwinFieldsConverterTest {

    @Mock
    private LinkService linkService;
    @Mock
    private TwinService twinService;
    @Mock
    private TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;

    @InjectMocks
    private RelationTwinFieldsConverter relationTwinFieldsConverter;

    private UUID linkId;
    private UUID classId;
    private MapperContext mapperContext;

    @BeforeEach
    void setUp() {
        linkId = UuidUtils.generate();
        classId = UuidUtils.generate();
        mapperContext = new MapperContext();
    }

    private LinkEntity link(UUID relationTwinClassId) {
        LinkEntity link = new LinkEntity().setId(linkId);
        link.setRelationTwinClassId(relationTwinClassId);
        return link;
    }

    @Test
    void shouldUseCachedLinkAndConvertWithItsClass() throws Exception {
        // given: link preloaded into the batch cache
        when(linkService.findEntitiesSafe(any())).thenReturn(new Kit<>(List.of(link(classId)), LinkEntity::getId));
        relationTwinFieldsConverter.preloadLinks(Set.of(linkId), mapperContext);
        FieldValue fieldValue = mock(FieldValue.class);
        when(twinFieldValueRestDTOReverseMapperV2.mapFields(eq(classId), any())).thenReturn(List.of(fieldValue));

        // when
        List<FieldValue> result = relationTwinFieldsConverter.convert(linkId, Map.of("amount", "10"), mapperContext);

        // then: cached link used, no per-item link load
        verify(linkService, never()).findEntitySafe(any(UUID.class));
        assertEquals(List.of(fieldValue), result);
    }

    @Test
    void shouldFallbackToSingleLoadWhenNoBatchPreloaded() throws Exception {
        // given: no preload — empty cache
        when(linkService.findEntitySafe(linkId)).thenReturn(link(classId));
        FieldValue fieldValue = mock(FieldValue.class);
        when(twinFieldValueRestDTOReverseMapperV2.mapFields(eq(classId), any())).thenReturn(List.of(fieldValue));

        // when
        List<FieldValue> result = relationTwinFieldsConverter.convert(linkId, Map.of("amount", "10"), mapperContext);

        // then
        verify(linkService).findEntitySafe(linkId);
        assertEquals(List.of(fieldValue), result);
    }

    @Test
    void shouldThrowWhenLinkHasNoRelationTwinClass() throws Exception {
        // given: batch-loaded link without a relation twin class
        when(linkService.findEntitiesSafe(any())).thenReturn(new Kit<>(List.of(link(null)), LinkEntity::getId));
        relationTwinFieldsConverter.preloadLinks(Set.of(linkId), mapperContext);

        // when + then
        ServiceException ex = assertThrows(ServiceException.class,
                () -> relationTwinFieldsConverter.convert(linkId, Map.of("amount", "10"), mapperContext));
        assertTrue(ex.getMessage().contains("relationTwinFields"));
        verifyNoInteractions(twinFieldValueRestDTOReverseMapperV2);
    }

    @Test
    void shouldConvertForRelationTwinAgainstItsOwnClass() throws Exception {
        // given: update path — the relation twin itself is preloaded (id == twin_link id, ID equality)
        TwinEntity relationTwin = new TwinEntity().setId(linkId).setTwinClassId(classId);
        when(twinService.findEntitiesSafe(any())).thenReturn(new Kit<>(List.of(relationTwin), TwinEntity::getId));
        relationTwinFieldsConverter.preloadRelationTwins(Set.of(linkId), mapperContext);
        FieldValue fieldValue = mock(FieldValue.class);
        when(twinFieldValueRestDTOReverseMapperV2.mapFields(eq(classId), any())).thenReturn(List.of(fieldValue));

        // when
        List<FieldValue> result = relationTwinFieldsConverter.convertForRelationTwin(linkId, Map.of("amount", "10"), mapperContext);

        // then: the relation twin's own class is used, no single load
        verify(twinService, never()).findEntitySafe(any(UUID.class));
        assertEquals(List.of(fieldValue), result);
    }

    @Test
    void shouldTranslateMissingRelationTwinToLinkIncorrectError() throws Exception {
        // given: no twin with the twin_link id exists (link without a relation twin)
        when(twinService.findEntitySafe(linkId)).thenThrow(new ServiceException(ErrorCodeCommon.UUID_UNKNOWN));

        // when + then
        ServiceException ex = assertThrows(ServiceException.class,
                () -> relationTwinFieldsConverter.convertForRelationTwin(linkId, Map.of("amount", "10"), mapperContext));
        assertTrue(ex.getMessage().contains("has no relation twin"));
        verifyNoInteractions(twinFieldValueRestDTOReverseMapperV2);
    }
}
