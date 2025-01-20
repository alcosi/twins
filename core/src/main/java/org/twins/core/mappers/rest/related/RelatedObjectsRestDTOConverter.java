package org.twins.core.mappers.rest.related;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.stereotype.Component;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.factory.FactoryDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.dto.rest.permission.PermissionGroupDTOv1;
import org.twins.core.dto.rest.permission.PermissionSchemaDTOv2;
import org.twins.core.dto.rest.related.RelatedObjectsDTOv1;
import org.twins.core.dto.rest.space.SpaceRoleDTOv1;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv2;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryPipelineRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryRestDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.RelatedObject;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionGroupRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionSchemaRestDTOMapperV2;
import org.twins.core.mappers.rest.space.SpaceRoleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapperV2;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;


@Component
@RequiredArgsConstructor
public class RelatedObjectsRestDTOConverter {

    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    private final UserRestDTOMapper userRestDTOMapper;
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;
    private final TwinStatusRestDTOMapperV2 twinStatusRestDTOMapperV2;
    private final TransitionBaseV1RestDTOMapper transitionBaseV1RestDTOMapper;
    private final DataListRestDTOMapper dataListRestDTOMapper;
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
    private final SpaceRoleDTOMapper spaceRoleDTOMapper;
    private final BusinessAccountDTOMapper businessAccountDTOMapper;
    private final PermissionGroupRestDTOMapper permissionGroupRestDTOMapper;
    private final PermissionRestDTOMapper permissionRestDTOMapper;
    private final PermissionSchemaRestDTOMapperV2 permissionSchemaRestDTOMapperV2;
    private final TwinflowBaseV1RestDTOMapper twinflowBaseV1RestDTOMapper;
    private final FactoryRestDTOMapper factoryRestDTOMapper;
    private final FactoryPipelineRestDTOMapper factoryPipelineRestDTOMapper;
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    public RelatedObjectsDTOv1 convert(MapperContext mapperContext) throws Exception {
        if (mapperContext.isLazyRelations())
            return null;
        RelatedObjectsDTOv1 ret = new RelatedObjectsDTOv1();
        Map<UUID, TwinDTOv2> twinMap = new HashMap<>();
        Map<UUID, TwinStatusDTOv2> statusMap = new HashMap<>();
        Map<UUID, UserDTOv1> userMap = new HashMap<>();
        Map<UUID, UserGroupDTOv1> userGroupMap = new HashMap<>();
        Map<UUID, TwinClassDTOv1> twinClassMap = new HashMap<>();
        Map<UUID, TwinflowTransitionBaseDTOv1> twinflowTransitionMap = new HashMap<>();
        Map<UUID, DataListDTOv1> dataListMap = new HashMap<>();
        Map<UUID, DataListOptionDTOv1> dataListOptionMap = new HashMap<>();
        Map<UUID, SpaceRoleDTOv1> spaceRoleMap = new HashMap<>();
        Map<UUID, BusinessAccountDTOv1> businessAccountMap = new HashMap<>();
        Map<UUID, PermissionGroupDTOv1> permissionGroupMap = new HashMap<>();
        Map<UUID, PermissionDTOv1> permissionMap = new HashMap<>();
        Map<UUID, PermissionSchemaDTOv2> permissionSchemaMap = new HashMap<>();
        Map<UUID, TwinflowBaseDTOv1> twinflowMap = new HashMap<>();
        Map<UUID, FactoryDTOv1> factoryMap = new HashMap<>();
        Map<UUID, FactoryPipelineDTOv1> factoryPipelineMap = new HashMap<>();
        Map<Integer, FeaturerDTOv1> featurerMap = new HashMap<>();

        MapperContext mapperContextLevel2 = mapperContext.cloneIgnoreRelatedObjects();
        if (!mapperContext.getRelatedTwinClassMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinClassMap(), twinClassRestDTOMapper, mapperContextLevel2, twinClassMap, TwinClassEntity::getId);
        if (!mapperContext.getRelatedTwinMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinMap(), twinRestDTOMapperV2, mapperContextLevel2, twinMap, TwinEntity::getId);
        if (!mapperContext.getRelatedTwinStatusMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinStatusMap(), twinStatusRestDTOMapperV2, mapperContextLevel2, statusMap, TwinStatusEntity::getId);
        if (!mapperContext.getRelatedUserMap().isEmpty())
            convertAndPut(mapperContext.getRelatedUserMap(), userRestDTOMapper, mapperContextLevel2, userMap, UserEntity::getId);
        if (!mapperContext.getRelatedUserGroupMap().isEmpty())
            convertAndPut(mapperContext.getRelatedUserGroupMap(), userGroupRestDTOMapper, mapperContextLevel2, userGroupMap, UserGroupEntity::getId);
        if (!mapperContext.getRelatedTwinflowTransitionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinflowTransitionMap(), transitionBaseV1RestDTOMapper, mapperContextLevel2, twinflowTransitionMap, TwinflowTransitionEntity::getId);
        if (!mapperContext.getRelatedDataListMap().isEmpty())
            convertAndPut(mapperContext.getRelatedDataListMap(), dataListRestDTOMapper, mapperContextLevel2, dataListMap, DataListEntity::getId);
        if (!mapperContext.getRelatedDataListOptionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedDataListOptionMap(), dataListOptionRestDTOMapper, mapperContextLevel2, dataListOptionMap, DataListOptionEntity::getId);
        if (!mapperContext.getRelatedSpaceRoleMap().isEmpty())
            convertAndPut(mapperContext.getRelatedSpaceRoleMap(), spaceRoleDTOMapper, mapperContextLevel2, spaceRoleMap, SpaceRoleEntity::getId);
        if (!mapperContext.getRelatedBusinessAccountMap().isEmpty())
            convertAndPut(mapperContext.getRelatedBusinessAccountMap(), businessAccountDTOMapper, mapperContextLevel2, businessAccountMap, BusinessAccountEntity::getId);
        if (!mapperContext.getRelatedPermissionGroupMap().isEmpty())
            convertAndPut(mapperContext.getRelatedPermissionGroupMap(), permissionGroupRestDTOMapper, mapperContextLevel2, permissionGroupMap, PermissionGroupEntity::getId);
        if (!mapperContext.getRelatedPermissionMap().isEmpty())
            convertAndPut(mapperContext.getRelatedPermissionMap(), permissionRestDTOMapper, mapperContextLevel2, permissionMap, PermissionEntity::getId);
        if (!mapperContext.getRelatedPermissionSchemaMap().isEmpty())
            convertAndPut(mapperContext.getRelatedPermissionSchemaMap(), permissionSchemaRestDTOMapperV2, mapperContextLevel2, permissionSchemaMap, PermissionSchemaEntity::getId);
        if (!mapperContext.getRelatedTwinflowMap().isEmpty())
            convertAndPut(mapperContext.getRelatedTwinflowMap(), twinflowBaseV1RestDTOMapper, mapperContextLevel2, twinflowMap, TwinflowEntity::getId);
        if (!mapperContext.getRelatedFactoryMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryMap(), factoryRestDTOMapper, mapperContextLevel2, factoryMap, TwinFactoryEntity::getId);
        if (!mapperContext.getRelatedFactoryPipelineMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFactoryPipelineMap(), factoryPipelineRestDTOMapper, mapperContextLevel2, factoryPipelineMap, TwinFactoryPipelineEntity::getId);
        if (!mapperContext.getRelatedFeaturerMap().isEmpty())
            convertAndPut(mapperContext.getRelatedFeaturerMap(), featurerRestDTOMapper, mapperContextLevel2, featurerMap, FeaturerEntity::getId);

        //run mappers one more time, because related objects can also contain relations (they were added to isolatedMapperContext on previous step)
        MapperContext mapperContextLevel3 = mapperContextLevel2.cloneIgnoreRelatedObjects();
        if (!mapperContextLevel2.getRelatedTwinClassMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinClassMap(), twinClassRestDTOMapper, mapperContextLevel3, twinClassMap, TwinClassEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinMap(), twinRestDTOMapperV2, mapperContextLevel3, twinMap, TwinEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinStatusMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinStatusMap(), twinStatusRestDTOMapperV2, mapperContextLevel3, statusMap, TwinStatusEntity::getId);
        if (!mapperContextLevel2.getRelatedUserMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedUserMap(), userRestDTOMapper, mapperContextLevel3, userMap, UserEntity::getId);
        if (!mapperContextLevel2.getRelatedUserGroupMap().isEmpty())
            convertAndPut(mapperContext.getRelatedUserGroupMap(), userGroupRestDTOMapper, mapperContextLevel3, userGroupMap, UserGroupEntity::getId);
        if (!mapperContextLevel2.getRelatedTwinflowTransitionMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinflowTransitionMap(), transitionBaseV1RestDTOMapper, mapperContextLevel3, twinflowTransitionMap, TwinflowTransitionEntity::getId);
        if (!mapperContextLevel2.getRelatedDataListMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedDataListMap(), dataListRestDTOMapper, mapperContextLevel3, dataListMap, DataListEntity::getId);
        if (!mapperContextLevel2.getRelatedDataListOptionMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedDataListOptionMap(), dataListOptionRestDTOMapper, mapperContextLevel3, dataListOptionMap, DataListOptionEntity::getId);
        if (!mapperContextLevel2.getRelatedSpaceRoleMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedSpaceRoleMap(), spaceRoleDTOMapper, mapperContextLevel3, spaceRoleMap, SpaceRoleEntity::getId);
        if (!mapperContextLevel2.getRelatedBusinessAccountMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedBusinessAccountMap(), businessAccountDTOMapper, mapperContextLevel3, businessAccountMap, BusinessAccountEntity::getId);
        if (!mapperContextLevel2.getRelatedPermissionGroupMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedPermissionGroupMap(), permissionGroupRestDTOMapper, mapperContextLevel3, permissionGroupMap, PermissionGroupEntity::getId);
        if (!mapperContextLevel2.getRelatedPermissionMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedPermissionMap(), permissionRestDTOMapper, mapperContextLevel3, permissionMap, PermissionEntity::getId);
        if (!mapperContextLevel2.getRelatedPermissionSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedPermissionSchemaMap(), permissionSchemaRestDTOMapperV2, mapperContextLevel3, permissionSchemaMap, PermissionSchemaEntity::getId);
        if (!mapperContextLevel2.getRelatedPermissionSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedTwinflowMap(), twinflowBaseV1RestDTOMapper, mapperContextLevel3, twinflowMap, TwinflowEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryMap(), factoryRestDTOMapper, mapperContextLevel3, factoryMap, TwinFactoryEntity::getId);
        if (!mapperContextLevel2.getRelatedFactoryPipelineMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFactoryPipelineMap(), factoryPipelineRestDTOMapper, mapperContextLevel3, factoryPipelineMap, TwinFactoryPipelineEntity::getId);
        if (!mapperContextLevel2.getRelatedFeaturerMap().isEmpty())
            convertAndPut(mapperContextLevel2.getRelatedFeaturerMap(), featurerRestDTOMapper, mapperContextLevel3, featurerMap, FeaturerEntity::getId);

        //run mappers one more time, because related objects can also contain relations (they were added to isolatedMapperContext on previous step)
        //this level was added because of dataLists. In case of search twins, twinClass will be detected on level1, twinClass.tagDataList will be detected on level2 and list options for tagDataList will be detected only on level3
        mapperContextLevel3.setLazyRelations(true); // on such depth we will not collect related objects anymore
        if (!mapperContextLevel3.getRelatedTwinClassMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinClassMap(), twinClassRestDTOMapper, mapperContextLevel3, twinClassMap, TwinClassEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinMap(), twinRestDTOMapperV2, mapperContextLevel3, twinMap, TwinEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinStatusMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinStatusMap(), twinStatusRestDTOMapperV2, mapperContextLevel3, statusMap, TwinStatusEntity::getId);
        if (!mapperContextLevel3.getRelatedUserMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedUserMap(), userRestDTOMapper, mapperContextLevel3, userMap, UserEntity::getId);
        if (!mapperContextLevel3.getRelatedUserGroupMap().isEmpty())
            convertAndPut(mapperContext.getRelatedUserGroupMap(), userGroupRestDTOMapper, mapperContextLevel3, userGroupMap, UserGroupEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinflowTransitionMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinflowTransitionMap(), transitionBaseV1RestDTOMapper, mapperContextLevel3, twinflowTransitionMap, TwinflowTransitionEntity::getId);
        if (!mapperContextLevel3.getRelatedDataListMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedDataListMap(), dataListRestDTOMapper, mapperContextLevel3, dataListMap, DataListEntity::getId);
        if (!mapperContextLevel3.getRelatedDataListOptionMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedDataListOptionMap(), dataListOptionRestDTOMapper, mapperContextLevel3, dataListOptionMap, DataListOptionEntity::getId);
        if (!mapperContextLevel3.getRelatedSpaceRoleMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedSpaceRoleMap(), spaceRoleDTOMapper, mapperContextLevel3, spaceRoleMap, SpaceRoleEntity::getId);
        if (!mapperContextLevel3.getRelatedBusinessAccountMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedBusinessAccountMap(), businessAccountDTOMapper, mapperContextLevel3, businessAccountMap, BusinessAccountEntity::getId);
        if (!mapperContextLevel3.getRelatedPermissionGroupMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedPermissionGroupMap(), permissionGroupRestDTOMapper, mapperContextLevel3, permissionGroupMap, PermissionGroupEntity::getId);
        if (!mapperContextLevel3.getRelatedPermissionMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedPermissionMap(), permissionRestDTOMapper, mapperContextLevel3, permissionMap, PermissionEntity::getId);
        if (!mapperContextLevel3.getRelatedPermissionSchemaMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedPermissionSchemaMap(), permissionSchemaRestDTOMapperV2, mapperContextLevel3, permissionSchemaMap, PermissionSchemaEntity::getId);
        if (!mapperContextLevel3.getRelatedTwinflowMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedTwinflowMap(), twinflowBaseV1RestDTOMapper, mapperContextLevel3, twinflowMap, TwinflowEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryMap(), factoryRestDTOMapper, mapperContextLevel3, factoryMap, TwinFactoryEntity::getId);
        if (!mapperContextLevel3.getRelatedFactoryPipelineMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFactoryPipelineMap(), factoryPipelineRestDTOMapper, mapperContextLevel3, factoryPipelineMap, TwinFactoryPipelineEntity::getId);
        if (!mapperContextLevel3.getRelatedFeaturerMap().isEmpty())
            convertAndPut(mapperContextLevel3.getRelatedFeaturerMap(), featurerRestDTOMapper, mapperContextLevel3, featurerMap, FeaturerEntity::getId);

        ret
                .setTwinClassMap(twinClassMap.isEmpty() ? null : twinClassMap)
                .setTwinMap(twinMap.isEmpty() ? null : twinMap)
                .setStatusMap(statusMap.isEmpty() ? null : statusMap)
                .setUserMap(userMap.isEmpty() ? null : userMap)
                .setUserGroupMap(userGroupMap.isEmpty() ? null : userGroupMap)
                .setTransitionsMap(twinflowTransitionMap.isEmpty() ? null : twinflowTransitionMap)
                .setDataListsMap(dataListMap.isEmpty() ? null : dataListMap)
                .setDataListsOptionMap(dataListOptionMap.isEmpty() ? null : dataListOptionMap)
                .setSpaceRoleMap(spaceRoleMap.isEmpty() ? null : spaceRoleMap)
                .setBusinessAccountMap(businessAccountMap.isEmpty() ? null : businessAccountMap)
                .setPermissionGroupMap(permissionGroupMap.isEmpty() ? null : permissionGroupMap)
                .setPermissionMap(permissionMap.isEmpty() ? null : permissionMap)
                .setPermissionSchemaMap(permissionSchemaMap.isEmpty() ? null : permissionSchemaMap)
                .setTwinflowMap(twinflowMap.isEmpty() ? null : twinflowMap)
                .setFactoryMap(factoryMap.isEmpty() ? null : factoryMap)
                .setFactoryPipelineMap(factoryPipelineMap.isEmpty() ? null : factoryPipelineMap)
                .setFeaturerMap(featurerMap.isEmpty() ? null : featurerMap)
        ;
        return ret;
    }

    public <E, D, K> void convertAndPut(Map<K, RelatedObject<E>> relatedObjects, RestSimpleDTOMapper<E, D> mapper, MapperContext mapperContext, Map<K, D> map, Function<? super E, ? extends K> functionGetId) throws Exception {
        for (RelatedObject<E> relatedObject : relatedObjects.values())
            map.put(functionGetId.apply(relatedObject.getObject()), mapper.convert(relatedObject.getObject(), mapperContext.setModesMap(relatedObject.getModes())));
    }
}
