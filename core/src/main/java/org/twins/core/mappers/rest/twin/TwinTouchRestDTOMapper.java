package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinTouchEntity;
import org.twins.core.dto.rest.twin.TwinTouchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TouchMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TouchMode.class)
public class TwinTouchRestDTOMapper extends RestSimpleDTOMapper<TwinTouchEntity, TwinTouchDTOv1> {

    @MapperModePointerBinding(modes = TwinMode.Touch2TwinMode.class)
    private final TwinBaseRestDTOMapper twinBaseRestDTOMapper;

    @Override
    public void map(TwinTouchEntity src, TwinTouchDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TouchMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setTwinId(src.getTwinId())
                        .setTouchId(src.getTouchId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime());
                break;
            case SHORT:
                dst
                        .setTwinId(src.getTwinId())
                        .setTouchId(src.getTouchId());
                break;
        }
        if (mapperContext.hasModeButNot(TwinMode.Touch2TwinMode.HIDE)) {
            dst.setTwinId(src.getTwinId());
            twinBaseRestDTOMapper.postpone(src.getTwin(), mapperContext.forkOnPoint(TwinMode.Touch2TwinMode.SHORT));
        }
    }
}
