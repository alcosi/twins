package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinflowBaseV2RestDTOMapper extends RestSimpleDTOMapper<TwinflowEntity, TwinflowBaseDTOv2> {

    @MapperModePointerBinding(modes = StatusMode.TwinflowInitStatus2StatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.Twinflow2TwinClassMode.class)
    private final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;

    private final TwinflowBaseV1RestDTOMapper twinflowBaseV1RestDTOMapper;


    @MapperModePointerBinding(modes = UserMode.Twinflow2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(TwinflowEntity src, TwinflowBaseDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinflowBaseV1RestDTOMapper.map(src, dst, mapperContext);

        if (mapperContext.hasModeButNot(TwinClassMode.Twinflow2TwinClassMode.HIDE) && src.getCreatedByUserId() != null)
            dst
                    .setTwinClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.Twinflow2TwinClassMode.SHORT))));

        if (mapperContext.hasModeButNot(UserMode.Twinflow2UserMode.HIDE) && src.getCreatedByUserId() != null)
            dst
                    .setCreatedByUser(userRestDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Twinflow2UserMode.SHORT))))
                    .setCreatedByUserId(src.getCreatedByUserId());

        if (mapperContext.hasModeButNot(StatusMode.TwinflowInitStatus2StatusMode.HIDE) && src.getCreatedByUserId() != null)
            dst
                    .setInitialStatus(twinStatusRestDTOMapper.convertOrPostpone(src.getInitialTwinStatus(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(StatusMode.TwinflowInitStatus2StatusMode.SHORT))))
                    .setInitialStatusId(src.getInitialTwinStatusId());
    }

    @Override
    public String getObjectCacheId(TwinflowEntity src) {
        return twinflowBaseV1RestDTOMapper.getObjectCacheId(src);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return twinflowBaseV1RestDTOMapper.hideMode(mapperContext);
    }

}
