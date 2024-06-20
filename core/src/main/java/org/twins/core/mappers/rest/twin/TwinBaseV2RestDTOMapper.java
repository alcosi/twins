package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinBaseDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twin.TwinService;

@Component
@RequiredArgsConstructor
public class TwinBaseV2RestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinBaseDTOv2> {
    final TwinBaseRestDTOMapper twinBaseRestDTOMapper;
    final UserRestDTOMapper userDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    final TwinService twinService;


    @Lazy
    @Autowired
    TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(TwinEntity src, TwinBaseDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinBaseRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(TwinBaseRestDTOMapper.TwinMode.SHORT)) {
            case DETAILED:
                twinService.loadHeadForTwin(src);
                dst
                        .assignerUser(userDTOMapper.convertOrPostpone(src.getAssignerUser(), mapperContext))
                        .authorUser(userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext))
                        .status(twinStatusRestDTOMapper.convertOrPostpone(src.getTwinStatus(), mapperContext))
                        .twinClass(twinClassRestDTOMapper.convertOrPostpone(src.getTwinClass(), mapperContext)) //todo deep recursion risk
                        .headTwin(this.convertOrPostpone(src.getHeadTwin(), mapperContext.cloneWithIsolatedModes(mapperContext.getModeOrUse(RelatedByHeadTwinMode.WHITE)))); //head twin will be much less detail
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return twinBaseRestDTOMapper.hideMode(mapperContext);
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }
}
