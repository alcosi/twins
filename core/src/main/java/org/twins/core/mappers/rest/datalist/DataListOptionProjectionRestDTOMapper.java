package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionProjectionDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionProjectionMode;
import org.twins.core.mappers.rest.mappercontext.modes.ProjectionTypeMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.projection.ProjectionTypeRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DataListOptionProjectionMode.class)
public class DataListOptionProjectionRestDTOMapper extends RestSimpleDTOMapper<DataListOptionProjectionEntity, DataListOptionProjectionDTOv1> {
    @MapperModePointerBinding(modes = ProjectionTypeMode.DataListOptionProjection2ProjectionTypeMode.class)
    private final ProjectionTypeRestDTOMapper projectionTypeRestDTOMapper;

    @MapperModePointerBinding(modes = DataListOptionMode.DataListOptionProjection2DataListOptionMode.class)
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.DataListOptionProjection2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapperV2;

    @Override
    public void map(DataListOptionProjectionEntity src, DataListOptionProjectionDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DataListOptionProjectionMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setChangedAt(src.getChangedAt().toLocalDateTime())
                    .setSrcDataListOptionId(src.getSrcDataListOptionId())
                    .setDstDataListOptionId(src.getDstDataListOptionId())
                    .setSavedByUserId(src.getSavedByUserId())
                    .setProjectionTypeId(src.getProjectionTypeId());
            case SHORT -> dst
                    .setId(src.getId())
                    .setChangedAt(src.getChangedAt().toLocalDateTime());
        }

        if (mapperContext.hasModeButNot(ProjectionTypeMode.DataListOptionProjection2ProjectionTypeMode.HIDE)) {
            dst.setProjectionTypeId(src.getProjectionTypeId());
            projectionTypeRestDTOMapper.postpone(src.getProjectionType(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(ProjectionTypeMode.DataListOptionProjection2ProjectionTypeMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(DataListOptionMode.DataListOptionProjection2DataListOptionMode.HIDE)) {
            dst
                    .setSrcDataListOptionId(src.getSrcDataListOptionId())
                    .setDstDataListOptionId(src.getDstDataListOptionId());

            dataListOptionRestDTOMapper.postpone(src.getSrcDataListOption(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListOptionMode.DataListOptionProjection2DataListOptionMode.SHORT)));
            dataListOptionRestDTOMapper.postpone(src.getDstDataListOption(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListOptionMode.DataListOptionProjection2DataListOptionMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(UserMode.DataListOptionProjection2UserMode.HIDE)) {
            dst.setSavedByUserId(src.getSavedByUserId());
            userRestDTOMapperV2.postpone(src.getSavedByUser(), mapperContext.forkOnPoint((mapperContext.getModeOrUse(UserMode.DataListOptionProjection2UserMode.SHORT))));
        }
    }
}
