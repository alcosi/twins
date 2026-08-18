package org.twins.core.unit.mappers.rest.link;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.link.TwinLinkAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapperV2;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.LinkService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Relation twin initial fields (relationTwinFields): conversion lives HERE, at the mapper layer,
 * with the link lookup batched in beforeCollectionConversion (one query per collection — no N+1).
 */
@ExtendWith(MockitoExtension.class)
class TwinLinkAddRestDTOReverseMapperTest {

    @Mock
    private AuthService authService;
    @Mock
    private LinkService linkService;
    @Mock
    private TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;

    @InjectMocks
    private TwinLinkAddRestDTOReverseMapper twinLinkAddRestDTOReverseMapper;

    private UserEntity user;

    @BeforeEach
    void setUp() throws Exception {
        user = new UserEntity().setId(UUID.randomUUID());
        ApiUser apiUser = mock(ApiUser.class);
        lenient().when(apiUser.getUser()).thenReturn(user);
        lenient().when(authService.getApiUser()).thenReturn(apiUser);
    }

    private TwinLinkAddDTOv1 dto(UUID linkId, Map<String, String> fields) {
        TwinLinkAddDTOv1 dto = new TwinLinkAddDTOv1()
                .setLinkId(linkId)
                .setDstTwinId(UUID.randomUUID());
        if (fields != null)
            dto.setRelationTwinFields(fields);
        return dto;
    }

    private LinkEntity link(UUID linkId, UUID relationTwinClassId) {
        LinkEntity link = new LinkEntity().setId(linkId);
        link.setRelationTwinClassId(relationTwinClassId);
        return link;
    }

    @Test
    void shouldBatchLoadLinksInOneQueryForWholeCollection() throws Exception {
        // given: two DTOs with fields referencing two distinct links
        UUID linkId1 = UuidUtils.generate();
        UUID linkId2 = UuidUtils.generate();
        UUID classId1 = UuidUtils.generate();
        UUID classId2 = UuidUtils.generate();
        when(linkService.findAllByIdIn(any())).thenReturn(List.of(
                link(linkId1, classId1),
                link(linkId2, classId2)));
        FieldValue fieldValue = mock(FieldValue.class);
        when(twinFieldValueRestDTOReverseMapperV2.mapFields(any(UUID.class), any()))
                .thenReturn(List.of(fieldValue));

        // when
        List<TwinLinkEntity> result = twinLinkAddRestDTOReverseMapper.convertCollection(List.of(
                dto(linkId1, Map.of("amount", "10")),
                dto(linkId2, Map.of("amount", "20"))), new MapperContext());

        // then: ONE batch link query, no per-DTO findEntitySafe
        verify(linkService, times(1)).findAllByIdIn(any());
        verify(linkService, never()).findEntitySafe(any(UUID.class));
        verify(twinFieldValueRestDTOReverseMapperV2).mapFields(eq(classId1), eq(Map.of("amount", "10")));
        verify(twinFieldValueRestDTOReverseMapperV2).mapFields(eq(classId2), eq(Map.of("amount", "20")));
        assertEquals(2, result.size());
        assertEquals(List.of(fieldValue), result.get(0).getRelationTwinFields());
        assertEquals(List.of(fieldValue), result.get(1).getRelationTwinFields());
    }

    @Test
    void shouldNotTouchLinksWhenNoFieldsProvided() throws Exception {
        // given
        UUID linkId = UuidUtils.generate();

        // when
        List<TwinLinkEntity> result = twinLinkAddRestDTOReverseMapper.convertCollection(
                List.of(dto(linkId, null)), new MapperContext());

        // then: no link loading at all
        verifyNoInteractions(linkService);
        verifyNoInteractions(twinFieldValueRestDTOReverseMapperV2);
        assertNull(result.get(0).getRelationTwinFields());
    }

    @Test
    void shouldThrowWhenFieldsProvidedButLinkHasNoRelationTwinClass() throws Exception {
        // given: batch-loaded link without a relation twin class
        UUID linkId = UuidUtils.generate();
        when(linkService.findAllByIdIn(any())).thenReturn(List.of(link(linkId, null)));

        // when + then
        ServiceException ex = assertThrows(ServiceException.class,
                () -> twinLinkAddRestDTOReverseMapper.convertCollection(
                        List.of(dto(linkId, Map.of("amount", "10"))), new MapperContext()));
        assertTrue(ex.getMessage().contains("relationTwinFields"));
        verifyNoInteractions(twinFieldValueRestDTOReverseMapperV2);
    }

    @Test
    void shouldFallbackToSingleLoadForSingleConvert() throws Exception {
        // given: single convert() — no beforeCollectionConversion ran, cache is empty
        UUID linkId = UuidUtils.generate();
        UUID classId = UuidUtils.generate();
        when(linkService.findEntitySafe(linkId)).thenReturn(link(linkId, classId));
        when(twinFieldValueRestDTOReverseMapperV2.mapFields(eq(classId), any()))
                .thenReturn(List.of(mock(FieldValue.class)));

        // when
        TwinLinkEntity result = twinLinkAddRestDTOReverseMapper.convert(dto(linkId, Map.of("amount", "10")));

        // then
        verify(linkService).findEntitySafe(linkId);
        verify(linkService, never()).findAllByIdIn(any());
        assertNotNull(result.getRelationTwinFields());
        assertEquals(linkId, result.getLinkId());
        assertEquals(user.getId(), result.getCreatedByUserId());
    }
}
