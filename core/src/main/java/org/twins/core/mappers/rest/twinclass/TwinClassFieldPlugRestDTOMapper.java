package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFieldPlugEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldPlugDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;

@Component
@RequiredArgsConstructor
public class TwinClassFieldPlugRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldPlugEntity, TwinClassFieldPlugDTOv1> {

    @MapperModePointerBinding(modes = TwinClassFieldMode.TwinClassFieldPlug2TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.TwinClassFieldPlug2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    private final TwinClassFieldPlugBaseRestDTOMapper twinClassFieldPlugBaseRestDTOMapper;

    @Override
    public void map(TwinClassFieldPlugEntity src, TwinClassFieldPlugDTOv1 dst, MapperContext mapperContext) throws Exception {
        twinClassFieldPlugBaseRestDTOMapper.map(src, dst, mapperContext);

        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassFieldPlug2TwinClassMode.HIDE)) {
            dst.setTwinClass(twinClassRestDTOMapper.convertOrPostpone(src.getTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.TwinClassFieldPlug2TwinClassMode.SHORT))));
        }

        if (mapperContext.hasModeButNot(TwinClassFieldMode.TwinClassFieldPlug2TwinClassFieldMode.HIDE)) {
            dst.setTwinClassField(twinClassFieldRestDTOMapper.convertOrPostpone(src.getTwinClassField(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassFieldMode.TwinClassFieldPlug2TwinClassFieldMode.SHORT))));
        }
    }
}
