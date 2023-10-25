package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinBaseDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinBaseV2RestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinBaseDTOv2> {
    final TwinBaseRestDTOMapper twinBaseRestDTOMapper;
    final UserRestDTOMapper userDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    @Autowired
    TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(TwinEntity src, TwinBaseDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinBaseRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(TwinBaseRestDTOMapper.TwinMode.ID_NAME_ONLY)) {
            case DETAILED:
                dst
                        .assignerUser(userDTOMapper.convertOrPostpone(src.getAssignerUser(), mapperContext))
                        .authorUser(userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext))
                        .status(twinStatusRestDTOMapper.convertOrPostpone(src.getTwinStatus(), mapperContext))
                        .twinClass(twinClassRestDTOMapper.convertOrPostpone(src.getTwinClass(), mapperContext));
        }
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }
}
