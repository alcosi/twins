package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinflowMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.i18n.I18nService;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinflowMode.class)
public class TwinflowBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinflowEntity, TwinflowBaseDTOv1> {

    private final I18nService i18nService;

    @MapperModePointerBinding(modes = StatusMode.TwinflowInitStatus2StatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.Twinflow2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.Twinflow2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(TwinflowEntity src, TwinflowBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinflowMode.SHORT)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setName(I18nCacheHolder.addId(src.getNameI18NId()))
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
                        .setTwinClassId(src.getTwinClassId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setCreatedByUserId(src.getCreatedByUserId())
                        .setInitialStatusId(src.getInitialTwinStatusId())
                        .setInitialSketchStatusId(src.getInitialSketchTwinStatusId());
                break;
            case SHORT:
                dst
                        .setId(src.getId());
                break;
        }
        if (mapperContext.hasModeButNot(TwinClassMode.Twinflow2TwinClassMode.HIDE)) {
            dst.setTwinClassId(src.getTwinClassId());
            twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.Twinflow2TwinClassMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(UserMode.Twinflow2UserMode.HIDE) && src.getCreatedByUserId() != null) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            userRestDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Twinflow2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(StatusMode.TwinflowInitStatus2StatusMode.HIDE) && src.getCreatedByUserId() != null) {
            dst.setInitialStatusId(src.getInitialTwinStatusId());
            dst.setInitialSketchStatusId(src.getInitialSketchTwinStatusId());
            twinStatusRestDTOMapper.postpone(src.getInitialTwinStatus(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(StatusMode.TwinflowInitStatus2StatusMode.SHORT)));
            twinStatusRestDTOMapper.postpone(src.getInitialSketchTwinStatus(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(StatusMode.TwinflowInitStatus2StatusMode.SHORT)));
        }
    }

    @Override
    public String getObjectCacheId(TwinflowEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinflowMode.HIDE);
    }

}
