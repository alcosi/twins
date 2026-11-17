package org.twins.core.unit.mappers.rest.twin;

import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySmartService;
import org.junit.jupiter.api.Test;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.dto.rest.twin.TwinGroupedSearchGroupDTOv1;
import org.twins.core.enums.twin.TwinGroupKeyType;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twin.TwinBaseRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinGroupedSearchGroupRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinstatus.TwinStatusService;
import org.twins.core.service.user.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * TwinGroupedSearchGroupRestDTOMapper: key entities are batch-loaded in beforeCollectionConversion,
 * but every postpone lives in map() — so a single-object convert() (which bypasses
 * beforeCollectionConversion) still resolves and postpones its key entity into relatedObjects.
 */
public class TwinGroupedSearchGroupRestDTOMapperTest {

    private final UserRestDTOMapper userDTOMapper = mock(UserRestDTOMapper.class);
    private final TwinClassRestDTOMapper twinClassRestDTOMapper = mock(TwinClassRestDTOMapper.class);
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper = mock(TwinStatusRestDTOMapper.class);
    private final TwinBaseRestDTOMapper twinBaseRestDTOMapper = mock(TwinBaseRestDTOMapper.class);
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper = mock(DataListOptionRestDTOMapper.class);
    private final TwinRestDTOMapperV2 twinRestDTOMapperV2 = mock(TwinRestDTOMapperV2.class);
    private final UserService userService = mock(UserService.class);
    private final TwinStatusService twinStatusService = mock(TwinStatusService.class);
    private final TwinClassService twinClassService = mock(TwinClassService.class);
    private final TwinService twinService = mock(TwinService.class);
    private final DataListOptionService dataListOptionService = mock(DataListOptionService.class);

    private final TwinGroupedSearchGroupRestDTOMapper mapper = new TwinGroupedSearchGroupRestDTOMapper(
            userDTOMapper,
            twinClassRestDTOMapper,
            twinStatusRestDTOMapper,
            twinBaseRestDTOMapper,
            dataListOptionRestDTOMapper,
            twinRestDTOMapperV2,
            userService,
            twinStatusService,
            twinClassService,
            twinService,
            dataListOptionService);

    private static MapperContext relatedContext(org.twins.core.mappers.rest.mappercontext.MapperMode... modes) {
        return new MapperContext().setLazyRelations(false).setModes(modes);
    }

    @Test
    public void singleConvertLazilyLoadsKeyEntityAndPostponesIt() throws Exception {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity().setId(userId);
        when(userService.findEntities(any(), any(), any(), any()))
                .thenReturn(new Kit<>(List.of(user), UserEntity::getId));
        TwinGroup group = new TwinGroup()
                .setKeyType(TwinGroupKeyType.USER)
                .setKey(userId)
                .setCount(3);

        TwinGroupedSearchGroupDTOv1 dto = mapper.convert(group, relatedContext(UserMode.Twin2UserMode.SHORT));

        assertEquals(TwinGroupKeyType.USER, dto.getKey().getType());
        assertEquals(userId, dto.getKey().getValue());
        assertEquals(3L, dto.getCount());
        assertSame(user, group.getKeyEntity());
        assertTrue(group.isKeyEntityResolved());
        verify(userService, times(1)).findEntities(any(), any(), any(), any());
        verify(userDTOMapper).postpone(same(user), any(MapperContext.class));
        verifyNoInteractions(twinStatusService, twinClassService, twinService, dataListOptionService);
    }

    @Test
    public void convertCollectionBatchLoadsKeyEntitiesOnceAndPostponesPerNode() throws Exception {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UserEntity user1 = new UserEntity().setId(userId1);
        UserEntity user2 = new UserEntity().setId(userId2);
        when(userService.findEntities(any(), any(), any(), any()))
                .thenReturn(new Kit<>(List.of(user1, user2), UserEntity::getId));
        TwinEntity leafTwin = new TwinEntity().setId(UUID.randomUUID());
        TwinGroup group1 = new TwinGroup().setKeyType(TwinGroupKeyType.USER).setKey(userId1);
        TwinGroup group2 = new TwinGroup()
                .setKeyType(TwinGroupKeyType.USER)
                .setKey(userId2)
                .setTwins(List.of(leafTwin));

        List<TwinGroupedSearchGroupDTOv1> dtos = mapper.convertCollection(List.of(group1, group2),
                relatedContext(UserMode.Twin2UserMode.SHORT, TwinMode.SHORT));

        verify(userService, times(1)).findEntities(any(), any(), any(), any());
        verify(userDTOMapper).postpone(same(user1), any(MapperContext.class));
        verify(userDTOMapper).postpone(same(user2), any(MapperContext.class));
        assertSame(user1, group1.getKeyEntity());
        assertSame(user2, group2.getKeyEntity());
        verify(twinRestDTOMapperV2).postpone(
                argThat((Collection<TwinEntity> c) -> c.size() == 1 && c.contains(leafTwin)),
                any(MapperContext.class));
        assertEquals(1, dtos.get(1).getTwinIdList().size());
        assertEquals(leafTwin.getId(), dtos.get(1).getTwinIdList().get(0));
    }

    @Test
    public void convertCollectionLoadsNestedTreeKeysPerType() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID statusId = UUID.randomUUID();
        UserEntity user = new UserEntity().setId(userId);
        TwinStatusEntity status = new TwinStatusEntity().setId(statusId);
        when(userService.findEntities(any(), any(), any(), any()))
                .thenReturn(new Kit<>(List.of(user), UserEntity::getId));
        when(twinStatusService.findEntities(any(), any(), any(), any()))
                .thenReturn(new Kit<>(List.of(status), TwinStatusEntity::getId));
        TwinGroup child = new TwinGroup().setKeyType(TwinGroupKeyType.STATUS).setKey(statusId);
        TwinGroup parent = new TwinGroup()
                .setKeyType(TwinGroupKeyType.USER)
                .setKey(userId)
                .setGroups(List.of(child));

        mapper.convertCollection(List.of(parent),
                relatedContext(UserMode.Twin2UserMode.SHORT, StatusMode.Twin2StatusMode.SHORT));

        verify(userService, times(1)).findEntities(any(), any(), any(), any());
        verify(twinStatusService, times(1)).findEntities(any(), any(), any(), any());
        verify(userDTOMapper).postpone(same(user), any(MapperContext.class));
        verify(twinStatusRestDTOMapper).postpone(same(status), any(MapperContext.class));
    }

    @Test
    public void twinKeyIsLoadedWithPermissionCheckAndPostponedWithSameContext() throws Exception {
        UUID twinId = UUID.randomUUID();
        TwinEntity keyTwin = new TwinEntity().setId(twinId);
        when(twinService.findEntities(any(), any(), any(), any()))
                .thenReturn(new Kit<>(List.of(keyTwin), TwinEntity::getId));
        TwinGroup group = new TwinGroup().setKeyType(TwinGroupKeyType.TWIN).setKey(twinId);
        MapperContext mapperContext = relatedContext(TwinMode.SHORT);

        mapper.convert(group, mapperContext);

        verify(twinService).findEntities(any(),
                eq(EntitySmartService.ListFindMode.ifMissedIgnore),
                eq(EntitySmartService.ReadPermissionCheckMode.ifDeniedLog),
                eq(EntitySmartService.EntityValidateMode.none));
        verify(twinBaseRestDTOMapper).beforeCollectionConversion(anyCollection(), same(mapperContext));
        verify(twinBaseRestDTOMapper).postpone(same(keyTwin), same(mapperContext));
    }

    @Test
    public void missingKeyEntityIsMarkedResolvedAndNotReloaded() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.findEntities(any(), any(), any(), any()))
                .thenReturn(new Kit<>(Collections.emptyList(), UserEntity::getId));
        TwinGroup group = new TwinGroup().setKeyType(TwinGroupKeyType.USER).setKey(userId);
        MapperContext mapperContext = relatedContext(UserMode.Twin2UserMode.SHORT);

        mapper.convert(group, mapperContext);
        mapper.convert(group, mapperContext);

        assertTrue(group.isKeyEntityResolved());
        assertNull(group.getKeyEntity());
        verify(userService, times(1)).findEntities(any(), any(), any(), any());
        verify(userDTOMapper, times(2)).postpone(isNull(UserEntity.class), any(MapperContext.class));
    }

    @Test
    public void lazyRelationsSkipsKeyLoadingEntirely() throws Exception {
        TwinGroup group = new TwinGroup()
                .setKeyType(TwinGroupKeyType.USER)
                .setKey(UUID.randomUUID())
                .setTwins(List.of(new TwinEntity().setId(UUID.randomUUID())));

        mapper.convertCollection(List.of(group),
                new MapperContext().setLazyRelations(true).setModes(UserMode.Twin2UserMode.SHORT, TwinMode.SHORT));

        assertFalse(group.isKeyEntityResolved());
        assertNull(group.getKeyEntity());
        verifyNoInteractions(userService, twinStatusService, twinClassService, twinService, dataListOptionService,
                twinRestDTOMapperV2, userDTOMapper);
    }
}
