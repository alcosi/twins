package org.twins.core.unit.mappers.rest.related;

import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObjectsDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerParametrizedRestDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerTypeRestDTOMapper;
import org.twins.core.mappers.rest.history.HistoryTypeRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.FeaturerParams;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.EntityRestMapperRegistry;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.system.EntityRefRestDTOMapper;
import org.twins.core.service.EntityServiceRegistry;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RelatedObjectsRestDTOConverter is a singleton bean: these tests pin the per-request isolation of the
 * accumulator maps — DTOs postponed in one convert() call must never leak into the result of the next
 * call (cross-request / cross-tenant bleed), and the featurer accumulator group must stay shared between
 * the FeaturerEntity and FeaturerParams descriptors within one call.
 */
public class RelatedObjectsRestDTOConverterTest {

    private final EntityRestMapperRegistry entityRestMapperRegistry = mock(EntityRestMapperRegistry.class);
    private final EntityServiceRegistry entityServiceRegistry = mock(EntityServiceRegistry.class);
    private final EntityRefRestDTOMapper entityRefRestDTOMapper = mock(EntityRefRestDTOMapper.class);
    private final RestSimpleDTOMapper<Object, Object> genericMapper = mock(RestSimpleDTOMapper.class);
    private final FeaturerRestDTOMapper featurerMapper = mock(FeaturerRestDTOMapper.class);
    private final FeaturerTypeRestDTOMapper featurerTypeMapper = mock(FeaturerTypeRestDTOMapper.class);
    private final HistoryTypeRestDTOMapper historyTypeMapper = mock(HistoryTypeRestDTOMapper.class);
    private final FeaturerParametrizedRestDTOMapper featurerParametrizedMapper = mock(FeaturerParametrizedRestDTOMapper.class);

    private final RelatedObjectsRestDTOConverter converter =
            new RelatedObjectsRestDTOConverter(entityRestMapperRegistry, entityServiceRegistry, entityRefRestDTOMapper);

    private Object dtoFor(Object entity) {
        return "dto@" + entity.getClass().getSimpleName() + "#" + ((org.twins.core.domain.Identifiable<?>) entity).getId();
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        EntitySecureFindServiceImpl<Object> secureFindService = mock(EntitySecureFindServiceImpl.class);
        when(secureFindService.entityGetIdFunction())
                .thenReturn(e -> (UUID) ((org.twins.core.domain.Identifiable<?>) e).getId());
        doReturn(secureFindService).when(entityServiceRegistry).getService(any(Class.class));
        doReturn(genericMapper).when(entityRestMapperRegistry).getMapper(any(Class.class));
        doReturn(featurerMapper).when(entityRestMapperRegistry).getFeaturerRestDTOMapper();
        doReturn(featurerTypeMapper).when(entityRestMapperRegistry).getFeaturerTypeRestDTOMapper();
        doReturn(historyTypeMapper).when(entityRestMapperRegistry).getHistoryTypeRestDTOMapper();
        doReturn(featurerParametrizedMapper).when(entityRestMapperRegistry).getFeaturerParametrizedRestDTOMapper();
        doAnswer(inv -> dtoFor(inv.getArgument(0))).when(genericMapper).convert(any(), any());
        doAnswer(inv -> new FeaturerDTOv1()).when(featurerMapper).convert(any(FeaturerEntity.class), any(MapperContext.class));
        doAnswer(inv -> new FeaturerDTOv1()).when(featurerParametrizedMapper).convert(any(FeaturerParams.class), any(MapperContext.class));
        ReflectionTestUtils.invokeMethod(converter, "initDescriptors");
    }

    private MapperContext contextWith(Object... entities) {
        MapperContext mapperContext = MapperContext.create().setLazyRelations(false);
        for (Object entity : entities)
            assertTrue(mapperContext.addRelatedObject(entity), entity + " must be accepted as a related object");
        return mapperContext;
    }

    /**
     * THE regression test of the shared-accumulator bug: two sequential convert() calls with different
     * contexts. The second response must contain only its own objects — anything from the first request
     * is a cross-request data leak.
     */
    @Test
    public void convertIsolatesConsecutiveRequests() throws Exception {
        UserEntity user1 = new UserEntity().setId(UUID.randomUUID());
        TwinClassEntity twinClass1 = new TwinClassEntity().setId(UUID.randomUUID());
        RelatedObjectsDTOv1 first = converter.convert(contextWith(user1, twinClass1));
        assertEquals(1, first.getUserMap().size());
        assertTrue(first.getUserMap().containsKey(user1.getId()));
        assertEquals(1, first.getTwinClassMap().size());
        assertTrue(first.getTwinClassMap().containsKey(twinClass1.getId()));

        UserEntity user2 = new UserEntity().setId(UUID.randomUUID());
        RelatedObjectsDTOv1 second = converter.convert(contextWith(user2));
        assertEquals(1, second.getUserMap().size());
        assertTrue(second.getUserMap().containsKey(user2.getId()));
        assertFalse(second.getUserMap().containsKey(user1.getId()), "first request user leaked into the second response");
        assertNull(second.getTwinClassMap(), "first request twinClass leaked into the second response");
    }

    /**
     * Postponed I18nEntity must drain into relatedObjects.i18nMap (the descriptor and RELATED_CLASSES
     * registration were lost in the descriptor-based rewrite).
     */
    @Test
    public void postponedI18nDrainsIntoI18nMap() throws Exception {
        I18nEntity i18n = new I18nEntity().setId(UUID.randomUUID());
        RelatedObjectsDTOv1 result = converter.convert(contextWith(i18n));
        assertNotNull(result.getI18nMap(), "i18nMap must be filled for postponed I18nEntity");
        assertEquals(1, result.getI18nMap().size());
        assertTrue(result.getI18nMap().containsKey(i18n.getId()));
    }

    /**
     * The featurer result map is drained from two sources — FeaturerEntity and FeaturerParams (see
     * alsoDrain on the featurer descriptor): both conversions must land in the single featurerMap.
     */
    @Test
    public void featurerMapDrainsBothSourcesWithoutLeakingIntoNextRequest() throws Exception {
        FeaturerEntity featurerEntity = new FeaturerEntity().setId(42);
        FeaturerParams featurerParams = new FeaturerParams(42, new HashMap<>());
        RelatedObjectsDTOv1 result = converter.convert(contextWith(featurerEntity, featurerParams));
        assertNotNull(result.getFeaturerMap());
        assertEquals(1, result.getFeaturerMap().size(), "both descriptors drain into one shared featurerMap");
        assertTrue(result.getFeaturerMap().containsKey(42));
        //and the shared map must not leak into the next request either
        RelatedObjectsDTOv1 next = converter.convert(contextWith(new FeaturerEntity().setId(43)));
        assertEquals(1, next.getFeaturerMap().size());
        assertTrue(next.getFeaturerMap().containsKey(43));
    }

    /** Empty context: no map is applied at all (setters receive null), no leftovers from previous calls. */
    @Test
    public void emptyContextProducesNoMaps() throws Exception {
        converter.convert(contextWith(new UserEntity().setId(UUID.randomUUID()))); // leave some state behind
        RelatedObjectsDTOv1 result = converter.convert(MapperContext.create().setLazyRelations(false));
        assertNull(result.getUserMap());
        assertNull(result.getFeaturerMap());
        assertNull(result.getI18nMap());
    }
}
