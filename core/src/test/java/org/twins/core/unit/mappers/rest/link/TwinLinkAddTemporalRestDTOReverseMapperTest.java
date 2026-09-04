package org.twins.core.unit.mappers.rest.link;

import org.cambium.common.exception.ServiceException;
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
import org.twins.core.dto.rest.link.TwinLinkAddDTOv2;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.link.RelationTwinFieldsConverter;
import org.twins.core.mappers.rest.link.TwinLinkAddTemporalRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Temporal (v2) link add mapper: same TwinLinkCreate composition + relation twin field conversion
 * as the v1 mapper (delegated to RelationTwinFieldsConverter), plus temporal dstTwinId parsing.
 */
@ExtendWith(MockitoExtension.class)
class TwinLinkAddTemporalRestDTOReverseMapperTest {

    @Mock
    private AuthService authService;
    @Mock
    private RelationTwinFieldsConverter relationTwinFieldsConverter;

    @InjectMocks
    private TwinLinkAddTemporalRestDTOReverseMapper twinLinkAddTemporalRestDTOReverseMapper;

    private UserEntity user;

    @BeforeEach
    void setUp() throws Exception {
        user = new UserEntity().setId(UUID.randomUUID());
        ApiUser apiUser = mock(ApiUser.class);
        lenient().when(apiUser.getUser()).thenReturn(user);
        lenient().when(authService.getApiUser()).thenReturn(apiUser);
    }

    private TwinLinkAddDTOv2 dto(UUID linkId, String dstTwinId, Map<String, String> fields) {
        TwinLinkAddDTOv2 dto = new TwinLinkAddDTOv2()
                .setLinkId(linkId)
                .setDstTwinId(dstTwinId);
        if (fields != null)
            dto.setRelationTwinFields(fields);
        return dto;
    }

    @Test
    void shouldMapLinkAndDelegateRelationTwinFields() throws Exception {
        // given
        UUID linkId = UuidUtils.generate();
        UUID dstTwinId = UuidUtils.generate();
        FieldValue fieldValue = mock(FieldValue.class);
        when(relationTwinFieldsConverter.convert(eq(linkId), eq(Map.of("amount", "10")), any(MapperContext.class)))
                .thenReturn(List.of(fieldValue));

        // when
        TwinLinkCreate result = twinLinkAddTemporalRestDTOReverseMapper.convert(dto(linkId, dstTwinId.toString(), Map.of("amount", "10")));

        // then
        assertEquals(linkId, result.getTwinLink().getLinkId());
        assertEquals(dstTwinId, result.getTwinLink().getDstTwinId());
        assertEquals(user.getId(), result.getTwinLink().getCreatedByUserId());
        assertEquals(List.of(fieldValue), result.getRelationTwinFields());
    }

    @Test
    void shouldThrowWhenDstTwinIdMissing() throws Exception {
        UUID linkId = UuidUtils.generate();
        ServiceException ex = assertThrows(ServiceException.class,
                () -> twinLinkAddTemporalRestDTOReverseMapper.convert(dto(linkId, " ", null)));
        assertTrue(ex.getMessage().contains("Missed dstTwinId"));
        verifyNoInteractions(relationTwinFieldsConverter);
    }

    @Test
    void shouldThrowWhenDstTwinIdNotParsed() throws Exception {
        UUID linkId = UuidUtils.generate();
        ServiceException ex = assertThrows(ServiceException.class,
                () -> twinLinkAddTemporalRestDTOReverseMapper.convert(dto(linkId, "temporalId:NOT-A-UUID-ONLY", null)));
        assertTrue(ex.getMessage().contains("DstTwinId can not be parsed"));
        verifyNoInteractions(relationTwinFieldsConverter);
    }

    @Test
    void shouldNotTouchConverterWhenNoFieldsProvided() throws Exception {
        UUID linkId = UuidUtils.generate();
        TwinLinkCreate result = twinLinkAddTemporalRestDTOReverseMapper.convert(
                dto(linkId, UUID.randomUUID().toString(), null));
        assertNull(result.getRelationTwinFields());
        verifyNoInteractions(relationTwinFieldsConverter);
    }
}
