package org.twins.core.unit.mappers.rest.twin;

import org.junit.jupiter.api.Test;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.dto.rest.twin.TwinGroupedSearchGroupDTOv1;
import org.twins.core.enums.twin.TwinGroupKeyType;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twin.TwinBaseRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinGroupedSearchGroupRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TwinGroupedSearchGroupRestDTOMapper never loads by id: key entities arrive attached to
 * TwinGroup.keyEntity (by TwinGroupedSearchService), the mapper only postpones them into relatedObjects
 * under their show-modes, plus bulk-preloads relations of leaf and key twins.
 */
public class TwinGroupedSearchGroupRestDTOMapperTest {

    private final UserRestDTOMapper userDTOMapper = mock(UserRestDTOMapper.class);
    private final TwinClassRestDTOMapper twinClassRestDTOMapper = mock(TwinClassRestDTOMapper.class);
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper = mock(TwinStatusRestDTOMapper.class);
    private final TwinBaseRestDTOMapper twinBaseRestDTOMapper = mock(TwinBaseRestDTOMapper.class);
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper = mock(DataListOptionRestDTOMapper.class);
    private final TwinRestDTOMapperV2 twinRestDTOMapperV2 = mock(TwinRestDTOMapperV2.class);

    private final TwinGroupedSearchGroupRestDTOMapper mapper = new TwinGroupedSearchGroupRestDTOMapper(
            userDTOMapper,
            twinClassRestDTOMapper,
            twinStatusRestDTOMapper,
            twinBaseRestDTOMapper,
            dataListOptionRestDTOMapper,
            twinRestDTOMapperV2);

    private static MapperContext relatedContext(org.twins.core.mappers.rest.mappercontext.MapperMode... modes) {
        return new MapperContext().setLazyRelations(false).setModes(modes);
    }

    @Test
    public void attachedKeyEntityIsPostponedUnderItsShowMode() throws Exception {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity().setId(userId);
        TwinGroup group = new TwinGroup()
                .setKeyType(TwinGroupKeyType.USER)
                .setKey(userId)
                .setKeyEntity(user)
                .setCount(3);

        TwinGroupedSearchGroupDTOv1 dto = mapper.convert(group, relatedContext(UserMode.Twin2UserMode.SHORT));

        assertEquals(TwinGroupKeyType.USER, dto.getKey().getType());
        assertEquals(userId, dto.getKey().getValue());
        assertEquals(3L, dto.getCount());
        verify(userDTOMapper).postpone(same(user), any(MapperContext.class));
        verifyNoInteractions(twinStatusRestDTOMapper, twinClassRestDTOMapper, twinBaseRestDTOMapper, dataListOptionRestDTOMapper);
    }

    @Test
    public void keyWithoutEntityPostponesNothing() throws Exception {
        TwinGroup group = new TwinGroup()
                .setKeyType(TwinGroupKeyType.USER)
                .setKey(UUID.randomUUID())
                .setCount(1);

        mapper.convert(group, relatedContext(UserMode.Twin2UserMode.SHORT));

        verifyNoInteractions(userDTOMapper);
    }

    @Test
    public void scalarKeyHasNoEntityToPostpone() throws Exception {
        TwinGroup group = new TwinGroup()
                .setKeyType(TwinGroupKeyType.TEXT)
                .setKey("some value")
                .setTwins(List.of(new TwinEntity().setId(UUID.randomUUID())));

        TwinGroupedSearchGroupDTOv1 dto = mapper.convert(group, relatedContext(TwinMode.SHORT));

        assertEquals("some value", dto.getKey().getValue());
        assertEquals(1, dto.getTwinIdList().size());
        verifyNoInteractions(userDTOMapper, twinStatusRestDTOMapper, twinClassRestDTOMapper, twinBaseRestDTOMapper, dataListOptionRestDTOMapper);
    }

    @Test
    public void convertCollectionPreloadsLeafTwinsAndKeyTwins() throws Exception {
        TwinEntity keyTwin = new TwinEntity().setId(UUID.randomUUID());
        TwinEntity leafTwin = new TwinEntity().setId(UUID.randomUUID());
        TwinGroup leaf = new TwinGroup()
                .setKeyType(TwinGroupKeyType.TWIN)
                .setKey(keyTwin.getId())
                .setKeyEntity(keyTwin)
                .setTwins(List.of(leafTwin));
        MapperContext mapperContext = relatedContext(TwinMode.SHORT);

        List<TwinGroupedSearchGroupDTOv1> dtos = mapper.convertCollection(List.of(leaf), mapperContext);

        verify(twinRestDTOMapperV2).beforeCollectionConversion(
                argThat((Collection<TwinEntity> c) -> c.size() == 1 && c.contains(leafTwin)), any(MapperContext.class));
        verify(twinBaseRestDTOMapper).beforeCollectionConversion(
                argThat((Collection<TwinEntity> c) -> c.size() == 1 && c.contains(keyTwin)), any(MapperContext.class));
        verify(twinBaseRestDTOMapper).postpone(same(keyTwin), same(mapperContext));
        verify(twinRestDTOMapperV2).postpone(
                argThat((Collection<TwinEntity> c) -> c.size() == 1 && c.contains(leafTwin)), any(MapperContext.class));
        assertEquals(List.of(leafTwin.getId()), dtos.get(0).getTwinIdList());
    }

    @Test
    public void nestedGroupsAreConvertedRecursivelyAndPostponePerType() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID statusId = UUID.randomUUID();
        UserEntity user = new UserEntity().setId(userId);
        TwinStatusEntity status = new TwinStatusEntity().setId(statusId);
        TwinGroup child = new TwinGroup()
                .setKeyType(TwinGroupKeyType.STATUS)
                .setKey(statusId)
                .setKeyEntity(status);
        TwinGroup parent = new TwinGroup()
                .setKeyType(TwinGroupKeyType.USER)
                .setKey(userId)
                .setKeyEntity(user)
                .setGroups(List.of(child));

        List<TwinGroupedSearchGroupDTOv1> dtos = mapper.convertCollection(List.of(parent),
                relatedContext(UserMode.Twin2UserMode.SHORT, StatusMode.Twin2StatusMode.SHORT));

        verify(userDTOMapper).postpone(same(user), any(MapperContext.class));
        verify(twinStatusRestDTOMapper).postpone(same(status), any(MapperContext.class));
        assertEquals(1, dtos.get(0).getGroups().size());
        assertEquals(statusId, dtos.get(0).getGroups().get(0).getKey().getValue());
        assertNull(dtos.get(0).getTwinIdList());
    }

    @Test
    public void lazyRelationsSkipsAllPostponesAndPreloads() throws Exception {
        TwinGroup group = new TwinGroup()
                .setKeyType(TwinGroupKeyType.USER)
                .setKey(UUID.randomUUID())
                .setKeyEntity(new UserEntity().setId(UUID.randomUUID()))
                .setTwins(List.of(new TwinEntity().setId(UUID.randomUUID())));

        List<TwinGroupedSearchGroupDTOv1> dtos = mapper.convertCollection(List.of(group),
                new MapperContext().setLazyRelations(true).setModes(UserMode.Twin2UserMode.SHORT, TwinMode.SHORT));

        assertEquals(1, dtos.get(0).getTwinIdList().size());
        verifyNoInteractions(userDTOMapper, twinStatusRestDTOMapper, twinClassRestDTOMapper, twinBaseRestDTOMapper,
                dataListOptionRestDTOMapper, twinRestDTOMapperV2);
    }
}
