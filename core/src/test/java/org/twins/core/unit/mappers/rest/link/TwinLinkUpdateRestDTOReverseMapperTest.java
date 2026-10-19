package org.twins.core.unit.mappers.rest.link;

import org.cambium.common.util.UuidUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.twinlink.TwinLinkUpdate;
import org.twins.core.dto.rest.link.TwinLinkUpdateDTOv1;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.link.RelationTwinFieldsConverter;
import org.twins.core.mappers.rest.link.TwinLinkUpdateRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twin.TwinService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Link update mapper: produces TwinLinkUpdate; relation twin field updates are converted against
 * the relation twin itself (id == twin_link id, ID equality), batched per collection via the converter.
 */
@ExtendWith(MockitoExtension.class)
class TwinLinkUpdateRestDTOReverseMapperTest {

    @Mock
    private TwinService twinService;
    @Mock
    private RelationTwinFieldsConverter relationTwinFieldsConverter;

    @InjectMocks
    private TwinLinkUpdateRestDTOReverseMapper twinLinkUpdateRestDTOReverseMapper;

    @BeforeEach
    void setUp() {
        // dstTwin resolution is per-DTO through twinService.findEntitySafe
    }

    private TwinLinkUpdateDTOv1 dto(UUID id, UUID dstTwinId, Map<String, String> fields) {
        TwinLinkUpdateDTOv1 dto = new TwinLinkUpdateDTOv1()
                .setId(id)
                .setDstTwinId(dstTwinId);
        if (fields != null)
            dto.setRelationTwinFields(fields);
        return dto;
    }

    @Test
    void shouldMapEntityAndDelegateRelationTwinFields() throws Exception {
        // given
        UUID twinLinkId = UuidUtils.generate();
        UUID dstTwinId = UuidUtils.generate();
        when(twinService.findEntitySafe(dstTwinId)).thenReturn(new TwinEntity().setId(dstTwinId));
        FieldValue fieldValue = mock(FieldValue.class);
        when(relationTwinFieldsConverter.convertForRelationTwin(eq(twinLinkId), eq(Map.of("amount", "99")), any(MapperContext.class)))
                .thenReturn(List.of(fieldValue));

        // when
        TwinLinkUpdate result = twinLinkUpdateRestDTOReverseMapper.convert(dto(twinLinkId, dstTwinId, Map.of("amount", "99")));

        // then
        assertEquals(twinLinkId, result.getTwinLink().getId());
        assertEquals(dstTwinId, result.getTwinLink().getDstTwinId());
        assertEquals(twinLinkId, result.getTwinLink().getRelationTwinId(), "relationTwinId == id (ID-equality invariant) feeds loadTwin's batch LoadedField");
        assertEquals(List.of(fieldValue), result.getRelationTwinFields());
    }

    @Test
    void shouldBatchPreloadRelationTwinsAndSkipWithoutFields() throws Exception {
        // given: two updates, only one carries fields
        UUID withFieldsId = UuidUtils.generate();
        UUID withoutFieldsId = UuidUtils.generate();
        UUID dstTwinId = UuidUtils.generate();
        when(twinService.findEntitySafe(dstTwinId)).thenReturn(new TwinEntity().setId(dstTwinId));
        when(relationTwinFieldsConverter.convertForRelationTwin(eq(withFieldsId), any(), any(MapperContext.class)))
                .thenReturn(List.of(mock(FieldValue.class)));

        // when
        List<TwinLinkUpdate> result = twinLinkUpdateRestDTOReverseMapper.convertCollection(List.of(
                dto(withFieldsId, dstTwinId, Map.of("amount", "99")),
                dto(withoutFieldsId, dstTwinId, null)), new MapperContext());

        // then: ONE batch preload with only the field-carrying id
        verify(relationTwinFieldsConverter, times(1)).preloadRelationTwins(eq(Set.of(withFieldsId)), any(MapperContext.class));
        assertEquals(2, result.size());
        assertNull(result.get(1).getRelationTwinFields());
        assertNull(result.get(1).getTwinLink().getRelationTwinId(), "no pointer without relationTwinFields — plain updates must not load/throw on a missing relation twin");
    }
}
