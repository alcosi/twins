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
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;

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
        if (mapperContext.hasModeButNot(MapperMode.TwinStatusMode.HIDE))
            dst
                    .status(twinStatusRestDTOMapper.convertOrPostpone(src.getTwinStatus(), mapperContext.forkOnPoint(MapperMode.TwinStatusMode.SHORT)))
                    .statusId(src.getTwinStatusId());
        if (mapperContext.hasModeButNot(MapperMode.TwinUserMode.HIDE)) {
            MapperContext forkedMapperContext1 = mapperContext.forkOnPoint(MapperMode.TwinUserMode.SHORT);
            dst
                    .assignerUser(userDTOMapper.convertOrPostpone(src.getAssignerUser(), forkedMapperContext1))
                    .authorUser(userDTOMapper.convertOrPostpone(src.getCreatedByUser(), forkedMapperContext1))
                    .assignerUserId(src.getAssignerUserId())
                    .authorUserId(src.getCreatedByUserId());
        }
        if (mapperContext.hasModeButNot(MapperMode.TwinClassMode.HIDE))
            dst
                    .twinClass(twinClassRestDTOMapper.convertOrPostpone(src.getTwinClass(), mapperContext.forkOnPoint(MapperMode.TwinClassMode.SHORT))) //todo deep recursion risk
                    .twinClassId(src.getTwinClassId());
        if (mapperContext.hasModeButNot(MapperMode.TwinHeadMode.WHITE)) {
            twinService.loadHeadForTwin(src);
            dst
                    .headTwin(this.convertOrPostpone(src.getHeadTwin(), mapperContext.forkOnPoint(MapperMode.TwinHeadMode.GREEN)))  //head twin will be much less detail
                    .twinClassId(src.getTwinClassId());
        }
    }

    @Override
    public void beforeListConversion(Collection<TwinEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeListConversion(srcCollection, mapperContext);
        //todo load heads for collection
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
