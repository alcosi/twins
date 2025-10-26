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
import org.twins.core.service.twinclass.TwinClassFieldPlugService;

@Component
@RequiredArgsConstructor
public class TwinClassFieldPlugRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldPlugEntity, TwinClassFieldPlugDTOv1> {

    private final TwinClassFieldPlugService twinClassFieldPlugService;

    @MapperModePointerBinding(modes = TwinClassFieldMode.TwinClassFieldPlug2TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.TwinClassFieldPlug2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(TwinClassFieldPlugEntity src, TwinClassFieldPlugDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassId(src.getTwinClassId())
                .setTwinClassFieldId(src.getTwinClassFieldId());

        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassFieldPlug2TwinClassMode.HIDE)) {
            twinClassFieldPlugService.loadClasses(src);
            twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.TwinClassFieldPlug2TwinClassMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(TwinClassFieldMode.TwinClassFieldPlug2TwinClassFieldMode.HIDE)) {
            twinClassFieldPlugService.loadFields(src);
            twinClassFieldRestDTOMapper.postpone(src.getTwinClassField(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassFieldMode.TwinClassFieldPlug2TwinClassFieldMode.SHORT)));
        }
    }
}
