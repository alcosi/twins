package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinflowBaseV2RestDTOMapper extends RestSimpleDTOMapper<TwinflowEntity, TwinflowBaseDTOv2> {
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final TwinflowBaseV1RestDTOMapper twinflowBaseV1RestDTOMapper;
    final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(TwinflowEntity src, TwinflowBaseDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinflowBaseV1RestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(TwinflowBaseV1RestDTOMapper.TwinflowMode.SHORT)) {
            case DETAILED:
                dst
                        .setCreatedByUser(userRestDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext))
                        .setInitialStatus(twinStatusRestDTOMapper.convertOrPostpone(src.getInitialTwinStatus(), mapperContext));
                break;
        }
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
