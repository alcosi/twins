package org.twins.core.mappers.rest.related;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.related.RelatedObjectsDTOv1;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinBaseV3RestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


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
        MapperContext isolatedMapperContext = mapperContext.cloneIgnoreRelatedObjects()
                .setMode(TwinLinkRestDTOMapper.Mode.HIDE)
                .setMode(TwinBaseV3RestDTOMapper.AttachmentsMode.HIDE);
        RelatedObjectsDTOv1 ret = new RelatedObjectsDTOv1();
        Map<UUID, TwinDTOv2> twinMap = new HashMap<>();
        Map<UUID, TwinStatusDTOv1> statusMap = new HashMap<>();
        Map<UUID, UserDTOv1> userMap = new HashMap<>();
        Map<UUID, TwinClassDTOv1> twinClassMap = new HashMap<>();
        if (!mapperContext.getRelatedTwinClassMap().isEmpty())
            twinClassMap.putAll(twinClassRestDTOMapper.convertMap(mapperContext.getRelatedTwinClassMap(), isolatedMapperContext));
        if (!mapperContext.getRelatedTwinClassMap().isEmpty())
            twinMap.putAll(twinRestDTOMapperV2.convertMap(mapperContext.getRelatedTwinMap(), isolatedMapperContext));
        if (!mapperContext.getRelatedTwinStatusMap().isEmpty())
            statusMap.putAll(twinStatusRestDTOMapper.convertMap(mapperContext.getRelatedTwinStatusMap(), isolatedMapperContext));
        if (!mapperContext.getRelatedUserMap().isEmpty())
            userMap.putAll(userRestDTOMapper.convertMap(mapperContext.getRelatedUserMap(), isolatedMapperContext));

        //run mappers one more time, because related objects can also contain relations
        if (!isolatedMapperContext.getRelatedTwinClassMap().isEmpty())
            twinClassMap.putAll(twinClassRestDTOMapper.convertMap(isolatedMapperContext.getRelatedTwinClassMap(), isolatedMapperContext));
        if (!isolatedMapperContext.getRelatedTwinClassMap().isEmpty())
            twinMap.putAll(twinRestDTOMapperV2.convertMap(isolatedMapperContext.getRelatedTwinMap(), isolatedMapperContext));
        if (!isolatedMapperContext.getRelatedTwinStatusMap().isEmpty())
            statusMap.putAll(twinStatusRestDTOMapper.convertMap(isolatedMapperContext.getRelatedTwinStatusMap(), isolatedMapperContext));
        if (!isolatedMapperContext.getRelatedUserMap().isEmpty())
            userMap.putAll(userRestDTOMapper.convertMap(isolatedMapperContext.getRelatedUserMap(), isolatedMapperContext));

        ret
                .setTwinClassMap(twinClassMap.isEmpty() ? null : twinClassMap)
                .setTwinMap(twinMap.isEmpty() ? null : twinMap)
                .setStatusMap(statusMap.isEmpty() ? null : statusMap)
                .setUserMap(userMap.isEmpty() ? null : userMap);
        return ret;
    }

}
