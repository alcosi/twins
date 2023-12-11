package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinBaseDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
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
                        .twinClass(twinClassRestDTOMapper.convertOrPostpone(src.getTwinClass(), mapperContext))
                        .headTwin(this.convertOrPostpone(src.getHeadTwin(), mapperContext)); //todo deep recursion risk
//                if (!mapperContext.hasMode(TwinHeadMode.HIDE))
//                     dst.headTwin(this.convertOrPostpone(src.getHeadTwin(), mapperContext.setOneTimeMode(TwinHeadMode.HIDE))); //using oneTimeMode because of recursion loop risk
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

    public enum TwinHeadMode implements MapperMode {
        SHOW, HIDE;

        public static final String _SHOW = "SHOW";
        public static final String _HIDE = "HIDE";
    }
}
