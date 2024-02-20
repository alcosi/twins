package org.twins.core.mappers.rest.related;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.dto.rest.related.RelatedObjectsDTOv1;
import org.twins.core.dto.rest.space.SpaceRoleDTOv1;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinflow.TwinTransitionViewDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RelatedObject;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.mappers.rest.space.SpaceRoleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinTransitionRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;


@Component
@RequiredArgsConstructor
public class RelatedObjectsRestDTOConverter {
    final TwinClassRestDTOMapper twinClassRestDTOMapper;

    final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    final UserRestDTOMapper userRestDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final TwinTransitionRestDTOMapper twinTransitionRestDTOMapper;
    final DataListRestDTOMapper dataListRestDTOMapper;
    final SpaceRoleDTOMapper spaceRoleDTOMapper;

    public RelatedObjectsDTOv1 convert(MapperContext mapperContext) throws Exception {
        if (mapperContext.isLazyRelations())
            return null;
        MapperContext isolatedMapperContext = mapperContext.cloneIgnoreRelatedObjects();
        RelatedObjectsDTOv1 ret = new RelatedObjectsDTOv1();
        Map<UUID, TwinDTOv2> twinMap = new HashMap<>();
        Map<UUID, TwinStatusDTOv1> statusMap = new HashMap<>();
        Map<UUID, UserDTOv1> userMap = new HashMap<>();
        Map<UUID, TwinClassDTOv1> twinClassMap = new HashMap<>();
        Map<UUID, TwinTransitionViewDTOv1> twinTransitionMap = new HashMap<>();
        Map<UUID, DataListDTOv1> dataListMap = new HashMap<>();
        Map<UUID, SpaceRoleDTOv1> spaceRoleMap = new HashMap<>();
        if (!mapperContext.getRelatedTwinClassMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinClassMap(), twinClassRestDTOMapper, isolatedMapperContext, twinClassMap, TwinClassEntity::getId);
        if (!mapperContext.getRelatedTwinMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinMap(), twinRestDTOMapperV2, isolatedMapperContext, twinMap, TwinEntity::getId);
        if (!mapperContext.getRelatedTwinStatusMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinStatusMap(), twinStatusRestDTOMapper, isolatedMapperContext, statusMap, TwinStatusEntity::getId);
        if (!mapperContext.getRelatedUserMap().isEmpty())
            convertAndPut(mapperContext.getRelatedUserMap(), userRestDTOMapper, isolatedMapperContext, userMap, UserEntity::getId);
        if (!mapperContext.getRelatedTwinflowTransitionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinflowTransitionMap(), twinTransitionRestDTOMapper, isolatedMapperContext, twinTransitionMap, TwinflowTransitionEntity::getId);
        if (!mapperContext.getRelatedDataListMap().isEmpty())
            convertAndPut(mapperContext.getRelatedDataListMap(), dataListRestDTOMapper, isolatedMapperContext, dataListMap, DataListEntity::getId);
        if (!mapperContext.getRelatedSpaceRoleMap().isEmpty())
            convertAndPut(mapperContext.getRelatedSpaceRoleMap(), spaceRoleDTOMapper, isolatedMapperContext, spaceRoleMap, SpaceRoleUserEntity::getId);

        //run mappers one more time, because related objects can also contain relations (they was added to isolatedMapperContext on previous step)
        isolatedMapperContext.setLazyRelations(true); // on such depth we will not collect related objects anymore
        if (!isolatedMapperContext.getRelatedTwinClassMap().isEmpty())
            convertAndPut(isolatedMapperContext.getRelatedTwinClassMap(), twinClassRestDTOMapper, isolatedMapperContext, twinClassMap, TwinClassEntity::getId);
        if (!isolatedMapperContext.getRelatedTwinMap().isEmpty())
            convertAndPut(isolatedMapperContext.getRelatedTwinMap(), twinRestDTOMapperV2, isolatedMapperContext, twinMap, TwinEntity::getId);
        if (!isolatedMapperContext.getRelatedTwinStatusMap().isEmpty())
            convertAndPut(isolatedMapperContext.getRelatedTwinStatusMap(), twinStatusRestDTOMapper, isolatedMapperContext, statusMap, TwinStatusEntity::getId);
        if (!isolatedMapperContext.getRelatedUserMap().isEmpty())
            convertAndPut(isolatedMapperContext.getRelatedUserMap(), userRestDTOMapper, isolatedMapperContext, userMap, UserEntity::getId);
        if (!isolatedMapperContext.getRelatedTwinflowTransitionMap().isEmpty())
            convertAndPut(isolatedMapperContext.getRelatedTwinflowTransitionMap(), twinTransitionRestDTOMapper, isolatedMapperContext, twinTransitionMap, TwinflowTransitionEntity::getId);
        if (!isolatedMapperContext.getRelatedDataListMap().isEmpty())
            convertAndPut(isolatedMapperContext.getRelatedDataListMap(), dataListRestDTOMapper, isolatedMapperContext, dataListMap, DataListEntity::getId);
       if (!isolatedMapperContext.getRelatedSpaceRoleMap().isEmpty())
            convertAndPut(isolatedMapperContext.getRelatedSpaceRoleMap(), spaceRoleDTOMapper, isolatedMapperContext, spaceRoleMap, SpaceRoleUserEntity::getId);

        ret
                .setTwinClassMap(twinClassMap.isEmpty() ? null : twinClassMap)
                .setTwinMap(twinMap.isEmpty() ? null : twinMap)
                .setStatusMap(statusMap.isEmpty() ? null : statusMap)
                .setUserMap(userMap.isEmpty() ? null : userMap)
                .setTransitionsMap(twinTransitionMap.isEmpty() ? null : twinTransitionMap)
                .setDataListsMap(dataListMap.isEmpty() ? null : dataListMap)
                .setSpaceRoleMap(spaceRoleMap.isEmpty() ? null : spaceRoleMap);
        return ret;
    }

    public <E, D> void convertAndPut(Map<UUID, RelatedObject<E>> relatedObjects, RestSimpleDTOMapper<E, D> mapper, MapperContext mapperContext, Map<UUID, D> map, Function<? super E, ? extends UUID> functionGetId) throws Exception {
        for (RelatedObject<E> relatedObject : relatedObjects.values())
            map.put(functionGetId.apply(relatedObject.getObject()), mapper.convert(relatedObject.getObject(), mapperContext.setModesMap(relatedObject.getModes())));
    }
}
