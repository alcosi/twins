package org.twins.core.mappers.rest.twin;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.twins.core.domain.ApiUser;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv2;
import org.twins.core.dto.rest.twin.TwinCreateRqDTOv2;
import org.twins.core.mappers.rest.attachment.AttachmentCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.TwinLinkAddTemporalRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TemporalIdContext;
import org.twins.core.service.user.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TwinCreateRqRestDTOReverseMapperTemporalTest {

    @InjectMocks
    private TwinCreateRqRestDTOReverseMapper mapper;

    @Mock
    private TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;

    @Mock
    private AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;

    @Mock
    private TwinFieldAttributeCreateRestDTOReverseMapper twinFieldAttributeCreateRestDTOReverseMapper;

    @Mock
    private TwinLinkAddTemporalRestDTOReverseMapper twinLinkAddTemporalRestDTOReverseMapper;

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @Mock
    private TemporalIdContext temporalIdContext;

    @Mock
    private ApiUser apiUser;

    @Mock
    private UserEntity userEntity;

    private MapperContext mapperContext;
    private UUID classId;

    @BeforeEach
    void setUp() throws Exception {
        mapperContext = new MapperContext();
        classId = UUID.randomUUID();

        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getUser()).thenReturn(userEntity);
        when(userEntity.getId()).thenReturn(UUID.randomUUID());
        when(userService.checkId(any(), any())).thenReturn(null);
        when(twinFieldValueRestDTOReverseMapperV2.mapFields(any(), any())).thenReturn(Collections.emptyList());
        when(attachmentCreateRestDTOReverseMapper.convertCollection(any())).thenReturn(Collections.emptyList());
        when(twinLinkAddTemporalRestDTOReverseMapper.convertCollection(any())).thenReturn(Collections.emptyList());
        when(twinFieldAttributeCreateRestDTOReverseMapper.convertCollection(any())).thenReturn(Collections.emptyList());
    }

    @Test
    void collectTemporalIds_WithUniqueIds_CollectsSuccessfully() throws ServiceException {
        List<TwinCreateRqDTOv2> dtos = List.of(
                createDtoWithTemporalId("PROJECT-1"),
                createDtoWithTemporalId("TASK-1"),
                createDtoWithTemporalId("TASK-2"),
                createDtoWithTemporalId(null)
        );

        mapper.collectTemporalIds(dtos);

        verify(temporalIdContext).clear();
        verify(temporalIdContext).contains("PROJECT-1");
        verify(temporalIdContext).put(eq("PROJECT-1"), any());
        verify(temporalIdContext).contains("TASK-1");
        verify(temporalIdContext).put(eq("TASK-1"), any());
        verify(temporalIdContext).contains("TASK-2");
        verify(temporalIdContext).put(eq("TASK-2"), any());
        verify(temporalIdContext, never()).put(isNull(), any());
    }

    @Test
    void collectTemporalIds_WithDuplicateIds_ThrowsException() {
        List<TwinCreateRqDTOv2> dtos = List.of(
                createDtoWithTemporalId("PROJECT-1"),
                createDtoWithTemporalId("TASK-1"),
                createDtoWithTemporalId("PROJECT-1")
        );

        when(temporalIdContext.contains("PROJECT-1")).thenReturn(false).thenReturn(true);

        ServiceException exception = assertThrows(ServiceException.class, () ->
            mapper.collectTemporalIds(dtos)
        );
        assertTrue(exception.getMessage().contains("Duplicate temporalId"));
        assertTrue(exception.getMessage().contains("PROJECT-1"));

        verify(temporalIdContext).put(eq("PROJECT-1"), any());
        verify(temporalIdContext, times(2)).contains("PROJECT-1");
    }

    @Test
    void validateTemporalIdReferencesAndReplace_WithHeadTwinRef_ReplacesCorrectly() throws ServiceException {
        UUID resolvedUuid = UUID.randomUUID();

        when(temporalIdContext.resolve("B")).thenReturn(resolvedUuid);

        TwinCreateRqDTOv2 dto1 = createDtoWithTemporalId("A");
        dto1.setHeadTwinId("temporalId:B");

        mapper.validateTemporalIdReferencesAndReplace(List.of(dto1));

        assertEquals(resolvedUuid.toString(), dto1.getHeadTwinId());
        verify(temporalIdContext).resolve("B");
    }

    @Test
    void validateTemporalIdReferencesAndReplace_WithRegularUuid_KeepsOriginal() throws ServiceException {
        UUID originalUuid = UUID.randomUUID();

        TwinCreateRqDTOv2 dto = createDtoWithTemporalId("A");
        dto.setHeadTwinId(originalUuid.toString());

        mapper.validateTemporalIdReferencesAndReplace(List.of(dto));

        assertEquals(originalUuid.toString(), dto.getHeadTwinId());
        verify(temporalIdContext, never()).resolve(anyString());
    }

    @Test
    void validateTemporalIdReferencesAndReplace_WithMissingTemporalRef_ThrowsException() {
        when(temporalIdContext.resolve("NONEXISTENT")).thenReturn(null);

        TwinCreateRqDTOv2 dto = createDtoWithTemporalId("A");
        dto.setHeadTwinId("temporalId:NONEXISTENT");

        ServiceException exception = assertThrows(ServiceException.class, () ->
            mapper.validateTemporalIdReferencesAndReplace(List.of(dto))
        );
        assertTrue(exception.getMessage().contains("not found"));
        assertTrue(exception.getMessage().contains("NONEXISTENT"));
    }

    @Test
    void validateTemporalIdReferencesAndReplace_WithFields_ReplacesCorrectly() throws ServiceException {
        UUID resolvedUuid = UUID.randomUUID();

        when(temporalIdContext.resolve("B")).thenReturn(resolvedUuid);

        TwinCreateRqDTOv2 dto = createDtoWithTemporalId("A");
        dto.setFields(new HashMap<>(Map.of("projectRef", "temporalId:B", "name", "Test")));

        mapper.validateTemporalIdReferencesAndReplace(List.of(dto));

        assertEquals(resolvedUuid.toString(), dto.getFields().get("projectRef"));
        assertEquals("Test", dto.getFields().get("name"));
    }

    @Test
    void validateTemporalIdReferencesAndReplace_WithLinks_ReplacesCorrectly() throws ServiceException {
        UUID resolvedUuid = UUID.randomUUID();

        when(temporalIdContext.resolve("B")).thenReturn(resolvedUuid);

        TwinCreateRqDTOv2 dto = createDtoWithTemporalId("A");
        TwinLinkAddDTOv2 link = new TwinLinkAddDTOv2();
        link.setLinkId(UUID.randomUUID());
        link.setDstTwinId("temporalId:B");
        dto.setLinks(List.of(link));

        mapper.validateTemporalIdReferencesAndReplace(List.of(dto));

        assertEquals(resolvedUuid.toString(), link.getDstTwinId());
    }

    @Test
    void beforeCollectionConversion_WithTemporalIds_ProcessesCorrectly() throws Exception {
        UUID resolvedUuid = UUID.randomUUID();
        when(temporalIdContext.contains(anyString())).thenReturn(false);
        when(temporalIdContext.resolve("B")).thenReturn(resolvedUuid);

        TwinCreateRqDTOv2 dto1 = createDtoWithTemporalId("A");
        dto1.setHeadTwinId("temporalId:B");
        TwinCreateRqDTOv2 dto2 = createDtoWithTemporalId("B");

        List<TwinCreateRqDTOv2> dtos = List.of(dto1, dto2);

        mapper.beforeCollectionConversion(dtos, mapperContext);

        verify(temporalIdContext).clear();
        verify(temporalIdContext).put(eq("A"), any());
        verify(temporalIdContext).put(eq("B"), any());
        assertEquals(resolvedUuid.toString(), dto1.getHeadTwinId());
    }

    @Test
    void beforeCollectionConversion_WithoutTemporalIds_SkipsProcessing() throws Exception {
        TwinCreateRqDTOv2 dto = createDtoWithTemporalId(null);
        dto.setHeadTwinId(UUID.randomUUID().toString());

        List<TwinCreateRqDTOv2> dtos = List.of(dto);

        mapper.beforeCollectionConversion(dtos, mapperContext);

        verify(temporalIdContext, never()).clear();
        verify(temporalIdContext, never()).put(anyString(), any());
    }

    @Test
    void map_WithTemporalId_ResolvesInTwinEntity() throws Exception {
        UUID twinId = UUID.randomUUID();
        UUID headTwinId = UUID.randomUUID();

        when(temporalIdContext.resolve("PROJECT-1")).thenReturn(twinId);

        TwinCreateRqDTOv2 dto = createDtoWithTemporalId("PROJECT-1");
        dto.setHeadTwinId(headTwinId.toString());

        TwinCreate twinCreate = new TwinCreate();

        mapper.map(dto, twinCreate, mapperContext);

        assertEquals(twinId, twinCreate.getTwinEntity().getId());
        assertEquals(headTwinId, twinCreate.getTwinEntity().getHeadTwinId());
    }

    @Test
    void map_WithoutTemporalId_SetsNullId() throws Exception {
        when(temporalIdContext.resolve(null)).thenReturn(null);

        TwinCreateRqDTOv2 dto = createDtoWithTemporalId(null);
        dto.setHeadTwinId(UUID.randomUUID().toString());

        TwinCreate twinCreate = new TwinCreate();

        mapper.map(dto, twinCreate, mapperContext);

        assertNull(twinCreate.getTwinEntity().getId());
    }

    private TwinCreateRqDTOv2 createDtoWithTemporalId(String temporalId) {
        TwinCreateRqDTOv2 dto = new TwinCreateRqDTOv2();
        dto.setTemporalId(temporalId);
        dto.setClassId(classId);
        return dto;
    }
}
