package org.twins.core.mappers.rest.related;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.dto.rest.related.RelatedObjectsDTOv1;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinflow.TwinTransitionViewDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinTransitionRestDTOMapper;
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
    final TwinTransitionRestDTOMapper twinTransitionRestDTOMapper;
    final DataListRestDTOMapper dataListRestDTOMapper;

    public RelatedObjectsDTOv1 convert(MapperContext mapperContext) throws Exception {
        if (mapperContext.isLazyRelations())
            return null;
        MapperContext isolatedMapperContext = mapperContext.cloneIgnoreRelatedObjects()
                .setMode(TwinLinkRestDTOMapper.Mode.HIDE) //no links in related objects
                .setMode(AttachmentViewRestDTOMapper.Mode.HIDE) //no attachments in related objects
                .setMode(TwinTransitionRestDTOMapper.Mode.HIDE); //no transitions
        RelatedObjectsDTOv1 ret = new RelatedObjectsDTOv1();
        Map<UUID, TwinDTOv2> twinMap = new HashMap<>();
        Map<UUID, TwinStatusDTOv1> statusMap = new HashMap<>();
        Map<UUID, UserDTOv1> userMap = new HashMap<>();
        Map<UUID, TwinClassDTOv1> twinClassMap = new HashMap<>();
        Map<UUID, TwinTransitionViewDTOv1> twinTransitionMap = new HashMap<>();
        Map<UUID, DataListDTOv1> dataListMap = new HashMap<>();
        if (!mapperContext.getRelatedTwinClassMap().isEmpty())
            twinClassMap.putAll(twinClassRestDTOMapper.convertMap(mapperContext.getRelatedTwinClassMap(), isolatedMapperContext));
        if (!mapperContext.getRelatedTwinMap().isEmpty())
            twinMap.putAll(twinRestDTOMapperV2.convertMap(mapperContext.getRelatedTwinMap(), isolatedMapperContext));
        if (!mapperContext.getRelatedTwinStatusMap().isEmpty())
            statusMap.putAll(twinStatusRestDTOMapper.convertMap(mapperContext.getRelatedTwinStatusMap(), isolatedMapperContext));
        if (!mapperContext.getRelatedUserMap().isEmpty())
            userMap.putAll(userRestDTOMapper.convertMap(mapperContext.getRelatedUserMap(), isolatedMapperContext));
        if (!mapperContext.getRelatedTwinflowTransitionMap().isEmpty()) {
            isolatedMapperContext.setMode(mapperContext.getModeOrUse(TwinTransitionRestDTOMapper.Mode.HIDE)); // we have to temporary use mode from original context
            twinTransitionMap.putAll(twinTransitionRestDTOMapper.convertMap(mapperContext.getRelatedTwinflowTransitionMap(), isolatedMapperContext));
            isolatedMapperContext.setMode(TwinTransitionRestDTOMapper.Mode.HIDE);
        }
        if (!mapperContext.getRelatedDataListMap().isEmpty())
            dataListMap.putAll(dataListRestDTOMapper.convertMap(mapperContext.getRelatedDataListMap(), isolatedMapperContext));

        //run mappers one more time, because related objects can also contain relations
        if (!isolatedMapperContext.getRelatedTwinClassMap().isEmpty())
            twinClassMap.putAll(twinClassRestDTOMapper.convertMap(isolatedMapperContext.getRelatedTwinClassMap(), isolatedMapperContext));
        if (!isolatedMapperContext.getRelatedTwinClassMap().isEmpty())
            twinMap.putAll(twinRestDTOMapperV2.convertMap(isolatedMapperContext.getRelatedTwinMap(), isolatedMapperContext));
        if (!isolatedMapperContext.getRelatedTwinStatusMap().isEmpty())
            statusMap.putAll(twinStatusRestDTOMapper.convertMap(isolatedMapperContext.getRelatedTwinStatusMap(), isolatedMapperContext));
        if (!isolatedMapperContext.getRelatedUserMap().isEmpty())
            userMap.putAll(userRestDTOMapper.convertMap(isolatedMapperContext.getRelatedUserMap(), isolatedMapperContext));
        if (!isolatedMapperContext.getRelatedTwinflowTransitionMap().isEmpty())
            twinTransitionMap.putAll(twinTransitionRestDTOMapper.convertMap(isolatedMapperContext.getRelatedTwinflowTransitionMap(), isolatedMapperContext));
        if (!isolatedMapperContext.getRelatedDataListMap().isEmpty())
            dataListMap.putAll(dataListRestDTOMapper.convertMap(isolatedMapperContext.getRelatedDataListMap(), isolatedMapperContext));

        ret
                .setTwinClassMap(twinClassMap.isEmpty() ? null : twinClassMap)
                .setTwinMap(twinMap.isEmpty() ? null : twinMap)
                .setStatusMap(statusMap.isEmpty() ? null : statusMap)
                .setUserMap(userMap.isEmpty() ? null : userMap)
                .setTransitionsMap(twinTransitionMap.isEmpty() ? null : twinTransitionMap)
                .setDataListsMap(dataListMap.isEmpty() ? null : dataListMap);
        return ret;
    }

}
