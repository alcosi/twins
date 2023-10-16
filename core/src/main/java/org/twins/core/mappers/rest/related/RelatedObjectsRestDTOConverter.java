package org.twins.core.mappers.rest.related;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.related.RelatedObjectsDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
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

    public RelatedObjectsDTOv1 convert(MapperProperties mapperProperties) throws Exception {
        if (mapperProperties.isLazyRelations())
            return null;
        mapperProperties.setLazyRelations(true);
        RelatedObjectsDTOv1 ret = new RelatedObjectsDTOv1();
        if (!mapperProperties.getRelatedTwinClassMap().isEmpty())
            ret.setTwinClassMap(twinClassRestDTOMapper.convertMap(mapperProperties.getRelatedTwinClassMap(), mapperProperties));
        if (!mapperProperties.getRelatedTwinClassMap().isEmpty())
            ret.setTwinMap(twinRestDTOMapperV2.convertMap(mapperProperties.getRelatedTwinMap(), mapperProperties));
        if (!mapperProperties.getRelatedTwinStatusMap().isEmpty())
            ret.setStatusMap(twinStatusRestDTOMapper.convertMap(mapperProperties.getRelatedTwinStatusMap(), mapperProperties));
        if (!mapperProperties.getRelatedUserMap().isEmpty())
            ret.setUserMap(userRestDTOMapper.convertMap(mapperProperties.getRelatedUserMap(), mapperProperties));
        return ret;
    }

}
