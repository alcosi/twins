package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDynamicMarkerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassDynamicMarkerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinValidatorSetMode;
import org.twins.core.mappers.rest.validator.TwinValidatorSetRestDTOMapper;
import org.twins.core.service.twinclass.TwinClassDynamicMarkerService;

import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinClassDynamicMarkerMode.class})
public class TwinClassDynamicMarkerDTOMapper extends RestSimpleDTOMapper<TwinClassDynamicMarkerEntity, TwinClassDynamicMarkerDTOv1> {

    @MapperModePointerBinding(modes = {TwinClassMode.TwinClassDynamicMarker2TwinClassMode.class})
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;
    @MapperModePointerBinding(modes = {DataListOptionMode.TwinClassDynamicMarker2DataListOptionMode.class})
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
    @MapperModePointerBinding(modes = {TwinValidatorSetMode.TwinClassDynamicMarker2TwinValidatorSetMode.class})
    private final TwinValidatorSetRestDTOMapper twinValidatorSetRestDTOMapper;

    private final TwinClassDynamicMarkerService twinClassDynamicMarkerService;

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

        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassDynamicMarker2TwinClassMode.HIDE)) {
            dst.setTwinClassId(src.getTwinClassId());
            twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.TwinClassDynamicMarker2TwinClassMode.SHORT));
        }
        if (mapperContext.hasModeButNot(DataListOptionMode.TwinClassDynamicMarker2DataListOptionMode.HIDE)) {
            dst.setMarkerDataListOptionId(src.getMarkerDataListOptionId());
            dataListOptionRestDTOMapper.postpone(src.getMarkerDataListOption(), mapperContext.forkOnPoint(DataListOptionMode.TwinClassDynamicMarker2DataListOptionMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinClassDynamicMarker2TwinValidatorSetMode.HIDE)) {
            dst.setTwinValidatorSetId(src.getTwinValidatorSetId());

            twinClassDynamicMarkerService.loadTwinValidatorSet(src);
            twinValidatorSetRestDTOMapper.postpone(src.getTwinValidatorSet(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinValidatorSetMode.TwinClassDynamicMarker2TwinValidatorSetMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinClassDynamicMarkerEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinValidatorSetMode.TwinClassDynamicMarker2TwinValidatorSetMode.HIDE)) {
            twinClassDynamicMarkerService.loadTwinValidatorSet(srcCollection);
        }
    }
}
