package org.twins.core.mappers.rest.related;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.related.RelatedObjectsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;


@Component
@RequiredArgsConstructor
public class RelatedObjectsRestDTOConverter {
    final TwinClassRestDTOMapper twinClassRestDTOMapper;

    final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    final UserRestDTOMapper userRestDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    public RelatedObjectsDTOv1 convert(MapperContext mapperContext) throws Exception {
        if (mapperContext.isLazyRelations())
            return null;
        mapperContext.setLazyRelations(true);
        RelatedObjectsDTOv1 ret = new RelatedObjectsDTOv1();
        if (!mapperContext.getRelatedTwinClassMap().isEmpty())
            ret.setTwinClassMap(twinClassRestDTOMapper.convertMap(mapperContext.getRelatedTwinClassMap(), mapperContext));
        if (!mapperContext.getRelatedTwinClassMap().isEmpty())
            ret.setTwinMap(twinRestDTOMapperV2.convertMap(mapperContext.getRelatedTwinMap(), mapperContext));
        if (!mapperContext.getRelatedTwinStatusMap().isEmpty())
            ret.setStatusMap(twinStatusRestDTOMapper.convertMap(mapperContext.getRelatedTwinStatusMap(), mapperContext));
        if (!mapperContext.getRelatedUserMap().isEmpty())
            ret.setUserMap(userRestDTOMapper.convertMap(mapperContext.getRelatedUserMap(), mapperContext));
        return ret;
    }

}
