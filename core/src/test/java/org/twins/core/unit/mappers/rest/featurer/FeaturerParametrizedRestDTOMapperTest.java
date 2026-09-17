package org.twins.core.unit.mappers.rest.featurer;

import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.mappers.rest.featurer.FeaturerParametrizedRestDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.EntityRef;
import org.twins.core.mappers.rest.mappercontext.FeaturerParams;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.system.EntityRefRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FeaturerParametrizedRestDTOMapper: converts a (featurerId, params) pair into a FeaturerDTO via
 * FeaturerRestDTOMapper and, at DETAILED mode only, postpones EntityRef objects for
 * entity-referencing param values (skipping injections, malformed values and non-entity params).
 */
public class FeaturerParametrizedRestDTOMapperTest {

    private final FeaturerService featurerService = mock(FeaturerService.class);
    private final FeaturerRestDTOMapper featurerRestDTOMapper = mock(FeaturerRestDTOMapper.class);
    private final EntityRefRestDTOMapper entityRefRestDTOMapper = mock(EntityRefRestDTOMapper.class);
    private final FeaturerParametrizedRestDTOMapper mapper =
            new FeaturerParametrizedRestDTOMapper(featurerService, featurerRestDTOMapper, entityRefRestDTOMapper);

    private static final Integer FEATURER_ID = 1310;

    private MapperContext detailedContext() {
        return new MapperContext().setModes(FeaturerMode.DETAILED);
    }

    private void stubFeaturer(Map<String, org.cambium.featurer.params.FeaturerParam<?>> paramDefinitions) throws Exception {
        when(featurerService.getFeaturerParams(FEATURER_ID)).thenReturn(paramDefinitions);
        when(featurerService.getFeaturerEntity(FEATURER_ID)).thenReturn(mock(FeaturerEntity.class));
        when(featurerRestDTOMapper.convert(any(FeaturerEntity.class), any(MapperContext.class))).thenReturn(new FeaturerDTOv1());
    }

    @Test
    public void entityParamIsPostponedAsEntityRefAtDetailedMode() throws Exception {
        UUID twinClassId = UUID.randomUUID();
        stubFeaturer(Map.of("twinClassId", new FeaturerParamUUIDTwinsTwinClassId("twinClassId")));
        HashMap<String, String> params = new HashMap<>(Map.of("twinClassId", twinClassId.toString()));

        FeaturerDTOv1 dto = mapper.convert(new FeaturerParams(FEATURER_ID, params), detailedContext());

        assertNotNull(dto); // produced via FeaturerRestDTOMapper delegation
        ArgumentCaptor<EntityRef> refCaptor = ArgumentCaptor.forClass(EntityRef.class);
        verify(entityRefRestDTOMapper, times(1)).postpone(refCaptor.capture(), any(MapperContext.class));
        EntityRef entityRef = refCaptor.getValue();
        assertEquals(TwinClassEntity.class, entityRef.getEntityClass());
        assertEquals(twinClassId, entityRef.getId());
    }

    @Test
    public void uuidSetParamIsPostponedAsMultipleEntityRefs() throws Exception {
        UUID twinId1 = UUID.randomUUID();
        UUID twinId2 = UUID.randomUUID();
        stubFeaturer(Map.of("twinIds", new FeaturerParamUUIDSetTwinsTwinId("twinIds")));
        HashMap<String, String> params = new HashMap<>(Map.of("twinIds", twinId1 + "," + twinId2));

        mapper.convert(new FeaturerParams(FEATURER_ID, params), detailedContext());

        verify(entityRefRestDTOMapper, times(2)).postpone(any(EntityRef.class), any(MapperContext.class));
    }

    @Test
    public void noRefsAtShortModeButFeaturerIsStillConverted() throws Exception {
        stubFeaturer(Map.of("twinClassId", new FeaturerParamUUIDTwinsTwinClassId("twinClassId")));
        HashMap<String, String> params = new HashMap<>(Map.of("twinClassId", UUID.randomUUID().toString()));

        FeaturerDTOv1 dto = mapper.convert(
                new FeaturerParams(FEATURER_ID, params),
                new MapperContext().setModes(FeaturerMode.SHORT));

        assertNotNull(dto);
        verifyNoInteractions(entityRefRestDTOMapper);
    }

    @Test
    public void hideModeProducesNothing() throws Exception {
        FeaturerDTOv1 dto = mapper.convert(
                new FeaturerParams(FEATURER_ID, new HashMap<>(Map.of("k", "v"))),
                new MapperContext().setModes(FeaturerMode.HIDE));
        assertNull(dto);
        verifyNoInteractions(featurerService, featurerRestDTOMapper, entityRefRestDTOMapper);
    }

    @Test
    public void malformedUuidValueIsSkippedWithoutFailure() throws Exception {
        stubFeaturer(Map.of("twinClassId", new FeaturerParamUUIDTwinsTwinClassId("twinClassId")));
        HashMap<String, String> params = new HashMap<>(Map.of("twinClassId", "not-a-uuid"));

        assertDoesNotThrow(() -> mapper.convert(new FeaturerParams(FEATURER_ID, params), detailedContext()));
        verifyNoInteractions(entityRefRestDTOMapper);
    }

    @Test
    public void injectionValueIsSkipped() throws Exception {
        stubFeaturer(Map.of("twinClassId", new FeaturerParamUUIDTwinsTwinClassId("twinClassId")));
        HashMap<String, String> params = new HashMap<>(Map.of("twinClassId", "injection@00000000-0000-0000-0000-000000000001"));

        mapper.convert(new FeaturerParams(FEATURER_ID, params), detailedContext());

        verifyNoInteractions(entityRefRestDTOMapper);
    }

    @Test
    public void nonEntityParamIsSkipped() throws Exception {
        // plain UUID param type without targetEntity in @FeaturerParamType — not entity-referencing
        stubFeaturer(Map.of("someId", new org.cambium.featurer.params.FeaturerParamUUID("someId")));
        HashMap<String, String> params = new HashMap<>(Map.of("someId", UUID.randomUUID().toString()));

        mapper.convert(new FeaturerParams(FEATURER_ID, params), detailedContext());

        verifyNoInteractions(entityRefRestDTOMapper);
    }

    @Test
    public void unknownParamKeyIsSkipped() throws Exception {
        stubFeaturer(Map.of()); // featurer knows no params, hstore has a stale key
        HashMap<String, String> params = new HashMap<>(Map.of("staleKey", UUID.randomUUID().toString()));

        mapper.convert(new FeaturerParams(FEATURER_ID, params), detailedContext());

        verifyNoInteractions(entityRefRestDTOMapper);
    }
}
