package org.twins.core.unit.mappers.rest.datalist;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.dto.rest.datalist.DataListSubsetDTOv1;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListSubsetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.datalist.DataListSubsetService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Regression guard for the mode pointer copy-paste bug: the embedded dataList verbosity
 * must follow the DataListSubset2DataListMode pointer (not DataListOption2DataListMode).
 */
public class DataListSubsetRestDTOMapperModeTest {

    private final DataListRestDTOMapper dataListRestDTOMapper = mock(DataListRestDTOMapper.class);
    private final UserRestDTOMapper userRestDTOMapper = mock(UserRestDTOMapper.class);
    private final DataListSubsetService dataListSubsetService = mock(DataListSubsetService.class);
    private final DataListSubsetRestDTOMapper mapper =
            new DataListSubsetRestDTOMapper(dataListRestDTOMapper, userRestDTOMapper, dataListSubsetService);

    private DataListSubsetEntity subsetEntity() {
        return new DataListSubsetEntity()
                .setId(UUID.randomUUID())
                .setDataListId(UUID.randomUUID())
                .setKey("key1")
                .setDataList(new DataListEntity().setId(UUID.randomUUID()));
    }

    @Test
    public void clientSubsetDataListModeIsRespectedInDataListFork() throws Exception {
        MapperContext mapperContext = new MapperContext()
                .setModes(
                        DataListMode.DataListSubset2DataListMode.DETAILED,
                        UserMode.DataListSubset2UserMode.HIDE);

        mapper.map(subsetEntity(), new DataListSubsetDTOv1(), mapperContext);

        ArgumentCaptor<MapperContext> forkCaptor = ArgumentCaptor.forClass(MapperContext.class);
        verify(dataListRestDTOMapper).postpone(any(DataListEntity.class), forkCaptor.capture());
        // forkOnPoint resolves the configured pointer via getModeOrUse(pointer): with the Subset2DataList pointer
        // the client mode (DETAILED) must propagate into the forked context as DataListMode.DETAILED
        assertEquals(DataListMode.DETAILED, forkCaptor.getValue().getModeOrUse(DataListMode.SHORT));
        verifyNoInteractions(userRestDTOMapper);
    }

    @Test
    public void defaultDataListForkIsShortWhenPointerSetToShort() throws Exception {
        // hasModeButNot(HIDE) is false when the pointer mode is absent at all,
        // so the explicit SHORT here stands for the default (non-DETAILED) client request
        MapperContext mapperContext = new MapperContext()
                .setModes(DataListMode.DataListSubset2DataListMode.SHORT);

        mapper.map(subsetEntity(), new DataListSubsetDTOv1(), mapperContext);

        ArgumentCaptor<MapperContext> forkCaptor = ArgumentCaptor.forClass(MapperContext.class);
        verify(dataListRestDTOMapper).postpone(any(DataListEntity.class), forkCaptor.capture());
        assertEquals(DataListMode.SHORT, forkCaptor.getValue().getModeOrUse(DataListMode.SHORT));
    }
}
