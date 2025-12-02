package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListProjectionEntity;
import org.twins.core.dto.rest.datalist.DataListProjectionDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;
import org.twins.core.mappers.rest.mappercontext.modes.DataListProjectionMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DataListProjectionMode.class)
public class DataListProjectionRestDTOMapper extends RestSimpleDTOMapper<DataListProjectionEntity, DataListProjectionDTOv1> {

    @MapperModePointerBinding(modes = DataListMode.DataListProjection2DataListMode.class)
    private final DataListRestDTOMapper dataListRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.DataListProjection2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapperV2;

    @Override
    public void map(DataListProjectionEntity src, DataListProjectionDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DataListProjectionMode.SHOW)) {
            case SHOW ->
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setChangedAt(src.getChangedAt().toLocalDateTime());
        }

        if (mapperContext.hasModeButNot(DataListMode.DataListProjection2DataListMode.HIDE)) {
            dst
                    .setSrcDataListId(src.getId())
                    .setDstDataListId(dst.getId());
            dataListRestDTOMapper.postpone(src.getSrcDataList(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListMode.DataListProjection2DataListMode.SHORT)));
            dataListRestDTOMapper.postpone(src.getDstDataList(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListMode.DataListProjection2DataListMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(UserMode.DataListProjection2UserMode.HIDE)) {
            dst
                    .setSavedByUserId(src.getSavedByUserId());
            userRestDTOMapperV2.postpone(src.getSavedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.DataListProjection2UserMode.SHORT)));
        }
    }
}
