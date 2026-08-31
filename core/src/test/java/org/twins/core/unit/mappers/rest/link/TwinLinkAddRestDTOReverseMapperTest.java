package org.twins.core.unit.mappers.rest.link;

import org.cambium.common.util.UuidUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.twinlink.TwinLinkCreate;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.link.RelationTwinFieldsConverter;
import org.twins.core.mappers.rest.link.TwinLinkAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;

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
 * v1 link add mapper: produces TwinLinkCreate; relation twin field conversion is delegated to
 * RelationTwinFieldsConverter, with link ids preloaded as ONE batch per collection (no N+1).
 */
@ExtendWith(MockitoExtension.class)
class TwinLinkAddRestDTOReverseMapperTest {

    @Mock
    private AuthService authService;
    @Mock
    private RelationTwinFieldsConverter relationTwinFieldsConverter;

    @InjectMocks
    private TwinLinkAddRestDTOReverseMapper twinLinkAddRestDTOReverseMapper;

    @BeforeEach
    void setUp() throws Exception {
        ApiUser apiUser = mock(ApiUser.class);
        lenient().when(apiUser.getUser()).thenReturn(new UserEntity().setId(UUID.randomUUID()));
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

    @Test
    void shouldDelegateConversionAndBatchPreloadInOneCall() throws Exception {
        // given: two DTOs with fields referencing two distinct links
        UUID linkId1 = UuidUtils.generate();
        UUID linkId2 = UuidUtils.generate();
        FieldValue fieldValue = mock(FieldValue.class);
        when(relationTwinFieldsConverter.convert(eq(linkId1), eq(Map.of("amount", "10")), any(MapperContext.class)))
                .thenReturn(List.of(fieldValue));
        when(relationTwinFieldsConverter.convert(eq(linkId2), eq(Map.of("amount", "20")), any(MapperContext.class)))
                .thenReturn(List.of(fieldValue));

        // when
        List<TwinLinkCreate> result = twinLinkAddRestDTOReverseMapper.convertCollection(List.of(
                dto(linkId1, Map.of("amount", "10")),
                dto(linkId2, Map.of("amount", "20"))), new MapperContext());

        // then: ONE batch preload with both ids, conversion delegated per link
        verify(relationTwinFieldsConverter, times(1)).preloadLinks(eq(Set.of(linkId1, linkId2)), any(MapperContext.class));
        assertEquals(2, result.size());
        assertEquals(List.of(fieldValue), result.get(0).getRelationTwinFields());
        assertEquals(List.of(fieldValue), result.get(1).getRelationTwinFields());
        assertEquals(linkId1, result.get(0).getTwinLink().getLinkId());
        assertEquals(linkId2, result.get(1).getTwinLink().getLinkId());
    }

    @Test
    void shouldNotTouchConverterWhenNoFieldsProvided() throws Exception {
        // given
        UUID linkId = UuidUtils.generate();

        // when
        List<TwinLinkCreate> result = twinLinkAddRestDTOReverseMapper.convertCollection(
                List.of(dto(linkId, null)), new MapperContext());

        // then: no preload, no conversion
        verifyNoInteractions(relationTwinFieldsConverter);
        assertNull(result.get(0).getRelationTwinFields());
        assertEquals(linkId, result.get(0).getTwinLink().getLinkId());
    }
}
