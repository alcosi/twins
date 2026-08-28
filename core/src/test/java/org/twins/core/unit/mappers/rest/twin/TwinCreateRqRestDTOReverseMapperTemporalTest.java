package org.twins.core.unit.mappers.rest.twin;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv2;
import org.twins.core.dto.rest.twin.TwinCreateRqDTOv2;
import org.twins.core.mappers.rest.attachment.AttachmentCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.TwinLinkAddTemporalRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twin.TwinCreateRqRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinFieldAttributeCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapperV2;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TemporalIdContext;
import org.twins.core.service.user.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.isNull;

@MockitoSettings(strictness = Strictness.LENIENT)
class TwinCreateRqRestDTOReverseMapperTemporalTest extends BaseUnitTest {

    @InjectMocks private TwinCreateRqRestDTOReverseMapper mapper;

    @Mock private TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;
    @Mock private AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;
    @Mock private TwinFieldAttributeCreateRestDTOReverseMapper twinFieldAttributeCreateRestDTOReverseMapper;
    @Mock private TwinLinkAddTemporalRestDTOReverseMapper twinLinkAddTemporalRestDTOReverseMapper;
    @Mock private UserService userService;
    @Mock private AuthService authService;
    @Mock private TemporalIdContext temporalIdContext;
    @Mock private ApiUser apiUser;
    @Mock private UserEntity userEntity;

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
        when(attachmentCreateRestDTOReverseMapper.convertCollection(anyCollection())).thenReturn(Collections.emptyList());
        when(twinLinkAddTemporalRestDTOReverseMapper.convertCollection(anyCollection())).thenReturn(Collections.emptyList());
        when(twinFieldAttributeCreateRestDTOReverseMapper.convertCollection(anyCollection())).thenReturn(Collections.emptyList());
    }

    @Nested
    class CollectTemporalIds {

        @Test
        void collectTemporalIds_uniqueIds_collectsAll() throws ServiceException {
            var dtos = List.of(
                    dto("PROJECT-1"),
                    dto("TASK-1"),
                    dto("TASK-2"),
                    dto(null)
            );

            mapper.collectTemporalIds(dtos);

            verify(temporalIdContext).clear();
            verify(temporalIdContext).put(eq("PROJECT-1"), any());
            verify(temporalIdContext).put(eq("TASK-1"), any());
            verify(temporalIdContext).put(eq("TASK-2"), any());
            verify(temporalIdContext, never()).put(isNull(), any());
        }

        @Test
        void collectTemporalIds_duplicateId_throwsException() {
            var dtos = List.of(dto("PROJECT-1"), dto("TASK-1"), dto("PROJECT-1"));

            when(temporalIdContext.contains("PROJECT-1")).thenReturn(false).thenReturn(true);

            var ex = assertThrows(ServiceException.class, () -> mapper.collectTemporalIds(dtos));

            assertTrue(ex.getMessage().contains("Duplicate temporalId"));
            assertTrue(ex.getMessage().contains("PROJECT-1"));
        }
    }

    @Nested
    class ValidateTemporalIdReferencesAndReplace {

        @Test
        void validateTemporalIdReferencesAndReplace_headTwinRef_replacesWithResolvedUuid() throws ServiceException {
            var resolved = UUID.randomUUID();
            when(temporalIdContext.resolve("B")).thenReturn(resolved);

            var dto = dto("A");
            dto.setHeadTwinId("temporalId:B");

            mapper.validateTemporalIdReferencesAndReplace(List.of(dto));

            assertEquals(resolved.toString(), dto.getHeadTwinId());
        }

        @Test
        void validateTemporalIdReferencesAndReplace_regularUuid_keepsOriginal() throws ServiceException {
            var original = UUID.randomUUID();
            var d = dto("A");
            d.setHeadTwinId(original.toString());

            mapper.validateTemporalIdReferencesAndReplace(List.of(d));

            assertEquals(original.toString(), d.getHeadTwinId());
            verify(temporalIdContext, never()).resolve(anyString());
        }

        @Test
        void validateTemporalIdReferencesAndReplace_missingRef_throwsException() {
            when(temporalIdContext.resolve("NONEXISTENT")).thenReturn(null);

            var d = dto("A");
            d.setHeadTwinId("temporalId:NONEXISTENT");

            var ex = assertThrows(ServiceException.class,
                    () -> mapper.validateTemporalIdReferencesAndReplace(List.of(d)));

            assertTrue(ex.getMessage().contains("not found"));
            assertTrue(ex.getMessage().contains("NONEXISTENT"));
        }

        @Test
        void validateTemporalIdReferencesAndReplace_fieldWithRef_replacesFieldValue() throws ServiceException {
            var resolved = UUID.randomUUID();
            when(temporalIdContext.resolve("B")).thenReturn(resolved);

            var d = dto("A");
            d.setFields(new HashMap<>(Map.of("projectRef", "temporalId:B", "name", "Test")));

            mapper.validateTemporalIdReferencesAndReplace(List.of(d));

            assertEquals(resolved.toString(), d.getFields().get("projectRef"));
            assertEquals("Test", d.getFields().get("name"));
        }

        @Test
        void validateTemporalIdReferencesAndReplace_linkWithRef_replacesLinkDstTwinId() throws ServiceException {
            var resolved = UUID.randomUUID();
            when(temporalIdContext.resolve("B")).thenReturn(resolved);

            var d = dto("A");
            var link = new TwinLinkAddDTOv2();
            link.setLinkId(UUID.randomUUID());
            link.setDstTwinId("temporalId:B");
            d.setLinks(List.of(link));

            mapper.validateTemporalIdReferencesAndReplace(List.of(d));

            assertEquals(resolved.toString(), link.getDstTwinId());
        }
    }

    @Nested
    class BeforeCollectionConversion {

        @Test
        void beforeCollectionConversion_withTemporalIds_collectsAndReplaces() throws Exception {
            var resolved = UUID.randomUUID();
            when(temporalIdContext.contains(anyString())).thenReturn(false);
            when(temporalIdContext.resolve("B")).thenReturn(resolved);

            var dto1 = dto("A");
            dto1.setHeadTwinId("temporalId:B");
            var dto2 = dto("B");

            mapper.beforeCollectionConversion(List.of(dto1, dto2), mapperContext);

            verify(temporalIdContext).clear();
            verify(temporalIdContext).put(eq("A"), any());
            verify(temporalIdContext).put(eq("B"), any());
            assertEquals(resolved.toString(), dto1.getHeadTwinId());
        }

        @Test
        void beforeCollectionConversion_withoutTemporalIds_skipsProcessing() throws Exception {
            var d = dto(null);
            d.setHeadTwinId(UUID.randomUUID().toString());

            mapper.beforeCollectionConversion(List.of(d), mapperContext);

            verify(temporalIdContext, never()).clear();
            verify(temporalIdContext, never()).put(anyString(), any());
        }
    }

    @Nested
    class Mapping {

        @Test
        void map_withTemporalId_resolvesIdInTwinEntity() throws Exception {
            var twinId = UUID.randomUUID();
            var headTwinId = UUID.randomUUID();
            when(temporalIdContext.resolve("PROJECT-1")).thenReturn(twinId);

            var d = dto("PROJECT-1");
            d.setHeadTwinId(headTwinId.toString());

            var twinCreate = new TwinCreate();
            mapper.map(d, twinCreate, mapperContext);

            assertEquals(twinId, twinCreate.getTwinEntity().getId());
            assertEquals(headTwinId, twinCreate.getTwinEntity().getHeadTwinId());
        }

        @Test
        void map_withoutTemporalId_setsNullId() throws Exception {
            when(temporalIdContext.resolve(null)).thenReturn(null);

            var d = dto(null);
            d.setHeadTwinId(UUID.randomUUID().toString());

            var twinCreate = new TwinCreate();
            mapper.map(d, twinCreate, mapperContext);

            assertNull(twinCreate.getTwinEntity().getId());
        }
    }

    private TwinCreateRqDTOv2 dto(String temporalId) {
        var dto = new TwinCreateRqDTOv2();
        dto.setTemporalId(temporalId);
        dto.setClassId(classId);

        return dto;
    }
}
