package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinStatusRestDTOMapperV2 extends RestSimpleDTOMapper<TwinStatusEntity, TwinStatusDTOv2> {

    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.TwinStatus2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(TwinStatusEntity src, TwinStatusDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinStatusRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(TwinClassMode.TwinStatus2TwinClassMode.HIDE)) {
            dst
                    .setTwinClass(twinClassRestDTOMapper.convertOrPostpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.TwinStatus2TwinClassMode.SHORT)))
                    .setTwinClassId(src.getTwinClassId());
        }
    }
}
