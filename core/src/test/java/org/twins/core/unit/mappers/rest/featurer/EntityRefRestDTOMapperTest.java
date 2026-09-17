package org.twins.core.unit.mappers.rest.featurer;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.exception.ErrorCodeFeaturer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.mappers.rest.mappercontext.EntityRef;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.RelatedObject;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinPointerMode;
import org.twins.core.mappers.rest.related.EntityRestMapperRegistry;
import org.twins.core.mappers.rest.system.EntityRefRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.EntityServiceRegistry;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

/**
 * EntityRefRestDTOMapper: delegates the pattern load of lazy EntityRefs to EntityServiceRegistry
 * (bulk loading itself is covered by EntityServiceRegistryTest) and postpones the loaded entities
 * into their typed related maps at the default show mode (DETAILED) from EntityRestMapperRegistry;
 * single-ref convert produces the entity DTO via the registry mapper.
 * Fail fast: load exceptions propagate.
 */
public class EntityRefRestDTOMapperTest {

    private final EntityServiceRegistry entityServiceRegistry = mock(EntityServiceRegistry.class);
    private final TwinClassRestDTOMapper twinClassRestDTOMapper = mock(TwinClassRestDTOMapper.class);
    private final EntityRestMapperRegistry entityRestMapperRegistry = mock(EntityRestMapperRegistry.class);

    private final EntityRefRestDTOMapper mapper = new EntityRefRestDTOMapper(entityServiceRegistry);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mapper, "entityRestMapperRegistry", entityRestMapperRegistry);
        doReturn(TwinClassMode.DETAILED).when(entityRestMapperRegistry).getShowMode(TwinClassEntity.class);
        doReturn(TwinPointerMode.DETAILED).when(entityRestMapperRegistry).getShowMode(TwinPointerEntity.class);
    }

    /**
     * Simulates the registry load: fills refs from the given id -> entity map (both load overloads).
     */
    private void stubLoad(Map<UUID, Object> entitiesById) throws ServiceException {
        doAnswer(invocation -> {
            fill(invocation.getArgument(0), entitiesById);
            return null;
        }).when(entityServiceRegistry).load(anyCollection());
        doAnswer(invocation -> {
            fill(List.of((EntityRef) invocation.getArgument(0)), entitiesById);
            return null;
        }).when(entityServiceRegistry).load(any(EntityRef.class));
    }

    private static void fill(Collection<EntityRef> refs, Map<UUID, Object> entitiesById) {
        for (EntityRef ref : refs) {
            Object entity = entitiesById.get(ref.getId());
            if (entity != null)
                ref.setEntity(entity);
        }
    }

    @Test
    public void resolvePostponesLoadedEntitiesAtDefaultShowMode() throws Exception {
        UUID twinClassId1 = UUID.randomUUID();
        UUID twinClassId2 = UUID.randomUUID();
        UUID twinPointerId = UUID.randomUUID();
        TwinClassEntity twinClass1 = new TwinClassEntity().setId(twinClassId1);
        TwinClassEntity twinClass2 = new TwinClassEntity().setId(twinClassId2);
        TwinPointerEntity twinPointer = new TwinPointerEntity().setId(twinPointerId);
        Map<UUID, Object> entitiesById = Map.of(
                twinClassId1, twinClass1,
                twinClassId2, twinClass2,
                twinPointerId, twinPointer);
        stubLoad(entitiesById);

        MapperContext mapperContext = new MapperContext().setLazyRelations(false);
        mapperContext.addRelatedObject(new EntityRef(TwinClassEntity.class, twinClassId1));
        mapperContext.addRelatedObject(new EntityRef(TwinClassEntity.class, twinClassId2));
        mapperContext.addRelatedObject(new EntityRef(TwinPointerEntity.class, twinPointerId));

        mapper.resolve(mapperContext);

        verify(entityServiceRegistry, times(1)).load(anyCollection()); // one bulk load for all refs
        assertEquals(2, mapperContext.getRelatedTwinClassMap().size());
        assertEquals(1, mapperContext.getRelatedTwinPointerMap().size());
        RelatedObject<TwinClassEntity> relatedTwinClass = mapperContext.getRelatedTwinClassMap().get(twinClassId1);
        assertNotNull(relatedTwinClass);
        assertEquals(TwinClassMode.DETAILED, relatedTwinClass.getModes().get(TwinClassMode.class)); // default show mode
        assertTrue(mapperContext.getRelatedEntityRefMap().isEmpty()); // drained
    }

    @Test
    public void clientRequestedDetailedModeIsNotDowngradedToShort() throws Exception {
        UUID twinClassId = UUID.randomUUID();
        TwinClassEntity twinClass = new TwinClassEntity().setId(twinClassId);
        stubLoad(Map.of(twinClassId, twinClass));

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
        TwinClassEntity twinClass = new TwinClassEntity().setId(twinClassId);
        stubLoad(Map.of(twinClassId, twinClass));

        MapperContext mapperContext = new MapperContext().setLazyRelations(false);
        mapperContext.addRelatedObject(new EntityRef(TwinClassEntity.class, twinClassId));
        mapperContext.addRelatedObject(new EntityRef(TwinClassEntity.class, twinClassId));

        mapper.resolve(mapperContext);

        verify(entityServiceRegistry, times(1)).load(anyCollection());
        assertEquals(1, mapperContext.getRelatedTwinClassMap().size()); // smartPut dedup
    }

    @Test
    public void loadFailureFailsFast() throws Exception {
        doThrow(new ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION)).when(entityServiceRegistry).load(anyCollection());

        MapperContext mapperContext = new MapperContext().setLazyRelations(false);
        mapperContext.addRelatedObject(new EntityRef(TwinClassEntity.class, UUID.randomUUID()));

        assertThrows(ServiceException.class, () -> mapper.resolve(mapperContext));
    }

    @Test
    public void emptyContextIsANoOp() throws Exception {
        MapperContext mapperContext = new MapperContext().setLazyRelations(false);
        mapper.resolve(mapperContext);
        verifyNoInteractions(entityServiceRegistry, entityRestMapperRegistry);
    }

    @Test
    public void beforeCollectionConversionDelegatesToRegistryLoad() throws Exception {
        UUID twinClassId = UUID.randomUUID();
        TwinClassEntity twinClass = new TwinClassEntity().setId(twinClassId);
        stubLoad(new HashMap<>(Map.of(twinClassId, twinClass)));

        EntityRef ref = new EntityRef(TwinClassEntity.class, twinClassId);
        mapper.convertCollection(List.of(ref), new MapperContext());

        verify(entityServiceRegistry, times(1)).load(anyCollection());
        assertEquals(twinClass, ref.getEntity()); // loaded by the registry, distributed via setters
    }

    @Test
    public void convertResolvesSingleRefAndProducesDto() throws Exception {
        UUID twinClassId = UUID.randomUUID();
        TwinClassEntity twinClass = new TwinClassEntity().setId(twinClassId);
        TwinClassDTOv1 dto = new TwinClassDTOv1();
        stubLoad(Map.of(twinClassId, twinClass));
        doReturn(twinClassRestDTOMapper).when(entityRestMapperRegistry).getMapper(TwinClassEntity.class);
        when(twinClassRestDTOMapper.convert(any(TwinClassEntity.class), any(MapperContext.class))).thenReturn(dto);

        EntityRef ref = new EntityRef(TwinClassEntity.class, twinClassId);
        Object result = mapper.convert(ref, new MapperContext());

        assertEquals(dto, result); // DTO of the concrete entity, resolved via EntityRestMapperRegistry
        assertEquals(twinClass, ref.getEntity());
    }
}
