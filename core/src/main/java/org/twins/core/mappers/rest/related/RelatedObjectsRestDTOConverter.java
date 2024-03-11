package org.twins.core.mappers.rest.related;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
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
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
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
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
    final SpaceRoleDTOMapper spaceRoleDTOMapper;

    public RelatedObjectsDTOv1 convert(MapperContext mapperContext) throws Exception {
        if (mapperContext.isLazyRelations())
            return null;
        RelatedObjectsDTOv1 ret = new RelatedObjectsDTOv1();
        Map<UUID, TwinDTOv2> twinMap = new HashMap<>();
        Map<UUID, TwinStatusDTOv1> statusMap = new HashMap<>();
        Map<UUID, UserDTOv1> userMap = new HashMap<>();
        Map<UUID, TwinClassDTOv1> twinClassMap = new HashMap<>();
        Map<UUID, TwinTransitionViewDTOv1> twinTransitionMap = new HashMap<>();
        Map<UUID, DataListDTOv1> dataListMap = new HashMap<>();
        Map<UUID, DataListOptionDTOv1> dataListOptionMap = new HashMap<>();
        Map<UUID, SpaceRoleDTOv1> spaceRoleMap = new HashMap<>();

        MapperContext mapperContextLevel2 = mapperContext.cloneIgnoreRelatedObjects();
        if (!mapperContext.getRelatedTwinClassMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinClassMap(), twinClassRestDTOMapper, mapperContextLevel2, twinClassMap, TwinClassEntity::getId);
        if (!mapperContext.getRelatedTwinMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinMap(), twinRestDTOMapperV2, mapperContextLevel2, twinMap, TwinEntity::getId);
        if (!mapperContext.getRelatedTwinStatusMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinStatusMap(), twinStatusRestDTOMapper, mapperContextLevel2, statusMap, TwinStatusEntity::getId);
        if (!mapperContext.getRelatedUserMap().isEmpty())
            convertAndPut(mapperContext.getRelatedUserMap(), userRestDTOMapper, mapperContextLevel2, userMap, UserEntity::getId);
        if (!mapperContext.getRelatedTwinflowTransitionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinflowTransitionMap(), twinTransitionRestDTOMapper, mapperContextLevel2, twinTransitionMap, TwinflowTransitionEntity::getId);
        if (!mapperContext.getRelatedDataListMap().isEmpty())
            convertAndPut(mapperContext.getRelatedDataListMap(), dataListRestDTOMapper, mapperContextLevel2, dataListMap, DataListEntity::getId);
        if (!mapperContext.getRelatedDataListOptionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedDataListOptionMap(), dataListOptionRestDTOMapper, mapperContextLevel2, dataListOptionMap, DataListOptionEntity::getId);
        if (!mapperContext.getRelatedSpaceRoleMap().isEmpty())
            convertAndPut(mapperContext.getRelatedSpaceRoleMap(), spaceRoleDTOMapper, mapperContextLevel2, spaceRoleMap, SpaceRoleUserEntity::getId);

        //run mappers one more time, because related objects can also contain relations (they were added to isolatedMapperContext on previous step)
        MapperContext mapperContextLevel3 = mapperContextLevel2.cloneIgnoreRelatedObjects();
        if (!mapperContextLevel2.getRelatedTwinClassMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinClassMap(), twinClassRestDTOMapper, mapperContextLevel3, twinClassMap, TwinClassEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinMap(), twinRestDTOMapperV2, mapperContextLevel3, twinMap, TwinEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinStatusMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinStatusMap(), twinStatusRestDTOMapper, mapperContextLevel3, statusMap, TwinStatusEntity::getId);
        if (!mapperContextLevel2.getRelatedUserMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedUserMap(), userRestDTOMapper, mapperContextLevel3, userMap, UserEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinflowTransitionMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinflowTransitionMap(), twinTransitionRestDTOMapper, mapperContextLevel3, twinTransitionMap, TwinflowTransitionEntity::getId);
        if (!mapperContextLevel2.getRelatedDataListMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedDataListMap(), dataListRestDTOMapper, mapperContextLevel3, dataListMap, DataListEntity::getId);
        if (!mapperContextLevel2.getRelatedDataListOptionMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedDataListOptionMap(), dataListOptionRestDTOMapper, mapperContextLevel3, dataListOptionMap, DataListOptionEntity::getId);
        if (!mapperContextLevel2.getRelatedSpaceRoleMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedSpaceRoleMap(), spaceRoleDTOMapper, mapperContextLevel3, spaceRoleMap, SpaceRoleUserEntity::getId);

        //run mappers one more time, because related objects can also contain relations (they were added to isolatedMapperContext on previous step)
        //this level was added because of dataLists. In case of search twins, twinClass will be detected on level1, twinClass.tagDataList will be detected on level2 and list options for tagDataList will be detected only on level3
        mapperContextLevel3.setLazyRelations(true); // on such depth we will not collect related objects anymore
        if (!mapperContextLevel3.getRelatedTwinClassMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinClassMap(), twinClassRestDTOMapper, mapperContextLevel3, twinClassMap, TwinClassEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinMap(), twinRestDTOMapperV2, mapperContextLevel3, twinMap, TwinEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinStatusMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinStatusMap(), twinStatusRestDTOMapper, mapperContextLevel3, statusMap, TwinStatusEntity::getId);
        if (!mapperContextLevel3.getRelatedUserMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedUserMap(), userRestDTOMapper, mapperContextLevel3, userMap, UserEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinflowTransitionMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinflowTransitionMap(), twinTransitionRestDTOMapper, mapperContextLevel3, twinTransitionMap, TwinflowTransitionEntity::getId);
        if (!mapperContextLevel3.getRelatedDataListMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedDataListMap(), dataListRestDTOMapper, mapperContextLevel3, dataListMap, DataListEntity::getId);
        if (!mapperContextLevel3.getRelatedDataListOptionMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedDataListOptionMap(), dataListOptionRestDTOMapper, mapperContextLevel3, dataListOptionMap, DataListOptionEntity::getId);
        if (!mapperContextLevel3.getRelatedSpaceRoleMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedSpaceRoleMap(), spaceRoleDTOMapper, mapperContextLevel3, spaceRoleMap, SpaceRoleUserEntity::getId);

        ret
                .setTwinClassMap(twinClassMap.isEmpty() ? null : twinClassMap)
                .setTwinMap(twinMap.isEmpty() ? null : twinMap)
                .setStatusMap(statusMap.isEmpty() ? null : statusMap)
                .setUserMap(userMap.isEmpty() ? null : userMap)
                .setTransitionsMap(twinTransitionMap.isEmpty() ? null : twinTransitionMap)
                .setDataListsMap(dataListMap.isEmpty() ? null : dataListMap)
                .setDataListsOptionMap(dataListOptionMap.isEmpty() ? null : dataListOptionMap)
                .setSpaceRoleMap(spaceRoleMap.isEmpty() ? null : spaceRoleMap);
        return ret;
    }

    public <E, D> void convertAndPut(Map<UUID, RelatedObject<E>> relatedObjects, RestSimpleDTOMapper<E, D> mapper, MapperContext mapperContext, Map<UUID, D> map, Function<? super E, ? extends UUID> functionGetId) throws Exception {
        for (RelatedObject<E> relatedObject : relatedObjects.values())
            map.put(functionGetId.apply(relatedObject.getObject()), mapper.convert(relatedObject.getObject(), mapperContext.setModesMap(relatedObject.getModes())));
    }
}
