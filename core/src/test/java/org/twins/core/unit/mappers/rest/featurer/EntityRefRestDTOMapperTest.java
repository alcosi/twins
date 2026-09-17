package org.twins.core.unit.mappers.rest.featurer;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.exception.ErrorCodeFeaturer;
import org.cambium.common.kit.DuplicateKeyMode;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.mappers.rest.system.EntityRefRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.EntityRef;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.RelatedObject;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.datalist.DataListSubsetService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.permission.PermissionSchemaService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.projection.ProjectionTypeGroupService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinpointer.TwinPointerService;
import org.twins.core.service.twinstatus.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassFreezeService;
import org.twins.core.service.twinclass.TwinClassSchemaService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.twinflow.TwinflowSchemaService;
import org.twins.core.service.user.UserService;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

/**
 * EntityRefRestDTOMapper.resolve: drains postponed EntityRefs grouped by entity class,
 * one findEntitiesSafe per class, and postpones loaded entities into their typed related
 * maps at (at least) SHORT mode. Fail fast: a missing/deleted reference propagates ServiceException.
 */
public class EntityRefRestDTOMapperTest {

    private final TwinClassService twinClassService = mock(TwinClassService.class);
    private final TwinClassFieldService twinClassFieldService = mock(TwinClassFieldService.class);
    private final TwinClassSchemaService twinClassSchemaService = mock(TwinClassSchemaService.class);
    private final TwinflowSchemaService twinflowSchemaService = mock(TwinflowSchemaService.class);
    private final TwinService twinService = mock(TwinService.class);
    private final TwinStatusService twinStatusService = mock(TwinStatusService.class);
    private final LinkService linkService = mock(LinkService.class);
    private final DataListService dataListService = mock(DataListService.class);
    private final DataListOptionService dataListOptionService = mock(DataListOptionService.class);
    private final DataListSubsetService dataListSubsetService = mock(DataListSubsetService.class);
    private final TwinPointerService twinPointerService = mock(TwinPointerService.class);
    private final PermissionService permissionService = mock(PermissionService.class);
    private final PermissionSchemaService permissionSchemaService = mock(PermissionSchemaService.class);
    private final I18nService i18nService = mock(I18nService.class);
    private final AttachmentRestrictionService attachmentRestrictionService = mock(AttachmentRestrictionService.class);
    private final UserGroupService userGroupService = mock(UserGroupService.class);
    private final UserService userService = mock(UserService.class);
    private final ProjectionTypeGroupService projectionTypeGroupService = mock(ProjectionTypeGroupService.class);
    private final TwinClassFreezeService twinClassFreezeService = mock(TwinClassFreezeService.class);

    private final EntityRefRestDTOMapper mapper = new EntityRefRestDTOMapper(
            twinClassService, twinClassFieldService, twinClassSchemaService, twinflowSchemaService,
            twinService, twinStatusService, linkService, dataListService, dataListOptionService,
            dataListSubsetService, twinPointerService, permissionService, permissionSchemaService,
            i18nService, attachmentRestrictionService, userGroupService, userService,
            projectionTypeGroupService, twinClassFreezeService);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.invokeMethod(mapper, "initRegistry");
    }

    @Test
    public void refsAreBulkLoadedPerClassAndPostponedAtShortMode() throws Exception {
        UUID twinClassId1 = UUID.randomUUID();
        UUID twinClassId2 = UUID.randomUUID();
        UUID twinPointerId = UUID.randomUUID();
        TwinClassEntity twinClass1 = new TwinClassEntity().setId(twinClassId1);
        TwinClassEntity twinClass2 = new TwinClassEntity().setId(twinClassId2);
        TwinPointerEntity twinPointer = new TwinPointerEntity().setId(twinPointerId);
        when(twinClassService.findEntitiesSafe(anySet())).thenReturn(kitOf(TwinClassEntity::getId, twinClass1, twinClass2));
        when(twinPointerService.findEntitiesSafe(anySet())).thenReturn(kitOf(TwinPointerEntity::getId, twinPointer));

        MapperContext mapperContext = new MapperContext().setLazyRelations(false);
        mapperContext.addRelatedObject(new EntityRef(TwinClassEntity.class, twinClassId1));
        mapperContext.addRelatedObject(new EntityRef(TwinClassEntity.class, twinClassId2));
        mapperContext.addRelatedObject(new EntityRef(TwinPointerEntity.class, twinPointerId));

        mapper.resolve(mapperContext);

        verify(twinClassService, times(1)).findEntitiesSafe(anySet()); // one bulk query per class
        verify(twinPointerService, times(1)).findEntitiesSafe(anySet());
        assertEquals(2, mapperContext.getRelatedTwinClassMap().size());
        assertEquals(1, mapperContext.getRelatedTwinPointerMap().size());
        RelatedObject<TwinClassEntity> relatedTwinClass = mapperContext.getRelatedTwinClassMap().get(twinClassId1);
        assertNotNull(relatedTwinClass);
        assertEquals(TwinClassMode.SHORT, relatedTwinClass.getModes().get(TwinClassMode.class));
        assertTrue(mapperContext.getRelatedEntityRefMap().isEmpty()); // drained
    }

    @Test
    public void clientRequestedDetailedModeIsNotDowngradedToShort() throws Exception {
        UUID twinClassId = UUID.randomUUID();
        when(twinClassService.findEntitiesSafe(anySet())).thenReturn(kitOf(TwinClassEntity::getId, new TwinClassEntity().setId(twinClassId)));

        MapperContext mapperContext = new MapperContext()
                .setLazyRelations(false)
                .setModes(TwinClassMode.DETAILED); // client explicitly asked twinClass at DETAILED
        mapperContext.addRelatedObject(new EntityRef(TwinClassEntity.class, twinClassId));

        mapper.resolve(mapperContext);

        assertEquals(TwinClassMode.DETAILED, mapperContext.getRelatedTwinClassMap().get(twinClassId).getModes().get(TwinClassMode.class));
    }

    @Test
    public void sameRefPostponedTwiceIsLoadedOnce() throws Exception {
        UUID twinClassId = UUID.randomUUID();
        when(twinClassService.findEntitiesSafe(anySet())).thenReturn(kitOf(TwinClassEntity::getId, new TwinClassEntity().setId(twinClassId)));

        MapperContext mapperContext = new MapperContext().setLazyRelations(false);
        mapperContext.addRelatedObject(new EntityRef(TwinClassEntity.class, twinClassId));
        mapperContext.addRelatedObject(new EntityRef(TwinClassEntity.class, twinClassId));

        mapper.resolve(mapperContext);

        verify(twinClassService, times(1)).findEntitiesSafe(anySet());
        assertEquals(1, mapperContext.getRelatedTwinClassMap().size());
    }

    @Test
    public void missingEntityFailsFast() throws Exception {
        when(twinClassService.findEntitiesSafe(anySet())).thenThrow(new ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION));

        MapperContext mapperContext = new MapperContext().setLazyRelations(false);
        mapperContext.addRelatedObject(new EntityRef(TwinClassEntity.class, UUID.randomUUID()));

        assertThrows(ServiceException.class, () -> mapper.resolve(mapperContext));
    }

    @Test
    public void emptyContextIsANoOp() throws Exception {
        MapperContext mapperContext = new MapperContext().setLazyRelations(false);
        mapper.resolve(mapperContext);
        verifyNoInteractions(twinClassService, twinPointerService, dataListSubsetService);
    }

    @SafeVarargs
    private static <T> Kit<T, UUID> kitOf(java.util.function.Function<T, UUID> getId, T... entities) {
        Kit<T, UUID> kit = new Kit<>(getId, DuplicateKeyMode.IGNORE);
        for (T entity : entities)
            kit.add(entity);
        return kit;
    }
}
