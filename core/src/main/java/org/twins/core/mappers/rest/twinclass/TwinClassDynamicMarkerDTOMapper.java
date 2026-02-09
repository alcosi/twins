package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDynamicMarkerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassDynamicMarkerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;

@Slf4j
@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinClassDynamicMarkerMode.class})
public class TwinClassDynamicMarkerDTOMapper extends RestSimpleDTOMapper<TwinClassDynamicMarkerEntity, TwinClassDynamicMarkerDTOv1> {

    @MapperModePointerBinding(modes = {TwinClassMode.TwinClassDynamicMarkerMode2TwinClassMode.class})
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(TwinClassDynamicMarkerEntity src, TwinClassDynamicMarkerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinClassDynamicMarkerMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setTwinClassId(src.getTwinClassId())
                        .setTwinValidatorSetId(src.getTwinValidatorSetId())
                        .setMarkerDataListOptionId(src.getMarkerDataListOptionId())
                ;
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setTwinClassId(src.getTwinClassId())
                        .setMarkerDataListOptionId(src.getMarkerDataListOptionId());
                break;
        }

        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassDynamicMarkerMode2TwinClassMode.HIDE)) {
            dst.setTwinClassId(src.getTwinClassId());
            twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.TwinClassDynamicMarkerMode2TwinClassMode.SHORT));
        }
    }
}
