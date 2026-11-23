package org.twins.core.unit.mappers.rest.twin;

import org.junit.jupiter.api.Test;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.dto.rest.twin.TwinGroupedSearchGroupDTOv1;
import org.twins.core.enums.twin.TwinGroupKeyType;
import org.twins.core.featurer.fieldtyper.value.FieldValueLinkSingle;
import org.twins.core.featurer.fieldtyper.value.FieldValueStatus;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldtyper.value.FieldValueUserSingle;
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
 * TwinGroupedSearchGroupRestDTOMapper never loads by id: key entities arrive inside TwinGroup.keyValue
 * (loaded by the search / TwinGroupedSearchService), the mapper only postpones them into relatedObjects
 * under their show-modes, plus bulk-preloads relations of leaf and key twins.
 */
public class TwinGroupedSearchGroupRestDTOMapperTest {

    private static final TwinClassFieldEntity FIELD = new TwinClassFieldEntity().setId(UUID.randomUUID());

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
    public void keyValueEntityIsPostponedUnderItsShowMode() throws Exception {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity().setId(userId);
        TwinGroup group = new TwinGroup()
                .setKeyValue(new FieldValueUserSingle(FIELD).setValue(user))
                .setCount(3);

        TwinGroupedSearchGroupDTOv1 dto = mapper.convert(group, relatedContext(UserMode.TwinGroupKey2UserMode.SHORT));

        assertEquals(TwinGroupKeyType.USER, dto.getKey().getType());
        assertEquals(userId, dto.getKey().getValue());
        assertEquals(3L, dto.getCount());
        verify(userDTOMapper).postpone(same(user), any(MapperContext.class));
        verifyNoInteractions(twinStatusRestDTOMapper, twinClassRestDTOMapper, twinBaseRestDTOMapper, dataListOptionRestDTOMapper);
    }

    @Test
    public void emptyKeyPostponesNothingButKeepsTheType() throws Exception {
        TwinGroup group = new TwinGroup()
                .setKeyValue(new FieldValueUserSingle(FIELD))
                .setCount(1);

        TwinGroupedSearchGroupDTOv1 dto = mapper.convert(group, relatedContext(UserMode.TwinGroupKey2UserMode.SHORT));

        assertEquals(TwinGroupKeyType.USER, dto.getKey().getType());
        assertNull(dto.getKey().getValue());
        verifyNoInteractions(userDTOMapper);
    }

    @Test
    public void scalarKeyHasNoEntityToPostpone() throws Exception {
        TwinGroup group = new TwinGroup()
                .setKeyValue(new FieldValueText(FIELD).setValue("some value"))
                .setTwins(List.of(new TwinEntity().setId(UUID.randomUUID())));

        TwinGroupedSearchGroupDTOv1 dto = mapper.convert(group, relatedContext(TwinMode.SHORT));

        assertEquals(TwinGroupKeyType.TEXT, dto.getKey().getType());
        assertEquals("some value", dto.getKey().getValue());
        assertEquals(1, dto.getTwinIdList().size());
        verifyNoInteractions(userDTOMapper, twinStatusRestDTOMapper, twinClassRestDTOMapper, twinBaseRestDTOMapper, dataListOptionRestDTOMapper);
    }

    @Test
    public void convertCollectionPreloadsLeafTwinsAndKeyTwins() throws Exception {
        TwinEntity keyTwin = new TwinEntity().setId(UUID.randomUUID());
        TwinEntity leafTwin = new TwinEntity().setId(UUID.randomUUID());
        TwinGroup leaf = new TwinGroup()
                .setKeyValue(new FieldValueLinkSingle(FIELD).setValue(keyTwin))
                .setTwins(List.of(leafTwin));
        MapperContext mapperContext = relatedContext(TwinMode.SHORT, TwinMode.TwinGroupKey2TwinMode.SHORT);

        List<TwinGroupedSearchGroupDTOv1> dtos = mapper.convertCollection(List.of(leaf), mapperContext);

        verify(twinRestDTOMapperV2).beforeCollectionConversion(
                argThat((Collection<TwinEntity> c) -> c.size() == 1 && c.contains(leafTwin)), any(MapperContext.class));
        verify(twinBaseRestDTOMapper).beforeCollectionConversion(
                argThat((Collection<TwinEntity> c) -> c.size() == 1 && c.contains(keyTwin)), any(MapperContext.class));
        verify(twinBaseRestDTOMapper).postpone(same(keyTwin), any(MapperContext.class));
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
                .setKeyValue(new FieldValueStatus(FIELD).setValue(status));
        TwinGroup parent = new TwinGroup()
                .setKeyValue(new FieldValueUserSingle(FIELD).setValue(user))
                .setGroups(List.of(child));

        List<TwinGroupedSearchGroupDTOv1> dtos = mapper.convertCollection(List.of(parent),
                relatedContext(UserMode.TwinGroupKey2UserMode.SHORT, StatusMode.TwinGroupKey2StatusMode.SHORT));

        verify(userDTOMapper).postpone(same(user), any(MapperContext.class));
        verify(twinStatusRestDTOMapper).postpone(same(status), any(MapperContext.class));
        assertEquals(1, dtos.get(0).getGroups().size());
        assertEquals(statusId, dtos.get(0).getGroups().get(0).getKey().getValue());
        assertNull(dtos.get(0).getTwinIdList());
    }

    @Test
    public void lazyRelationsSkipsAllPostponesAndPreloads() throws Exception {
        TwinGroup group = new TwinGroup()
                .setKeyValue(new FieldValueUserSingle(FIELD).setValue(new UserEntity().setId(UUID.randomUUID())))
                .setTwins(List.of(new TwinEntity().setId(UUID.randomUUID())));

        List<TwinGroupedSearchGroupDTOv1> dtos = mapper.convertCollection(List.of(group),
                new MapperContext().setLazyRelations(true).setModes(UserMode.TwinGroupKey2UserMode.SHORT, TwinMode.SHORT));

        assertEquals(1, dtos.get(0).getTwinIdList().size());
        verifyNoInteractions(userDTOMapper, twinStatusRestDTOMapper, twinClassRestDTOMapper, twinBaseRestDTOMapper,
                dataListOptionRestDTOMapper, twinRestDTOMapperV2);
    }
}
