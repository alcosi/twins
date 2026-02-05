package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkBackwardRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkForwardRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.resource.ResourceService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;


@Component
@RequiredArgsConstructor
@Slf4j
public class TwinClassRestDTOMapper extends RestSimpleDTOMapper<TwinClassEntity, TwinClassDTOv1> {

    @Lazy
    @MapperModePointerBinding(modes = {
            TwinClassMode.class,
            TwinClassMode.TwinClassHead2TwinClassMode.class,
            TwinClassMode.TwinClassExtends2TwinClassMode.class,
            TwinClassSegmentMode.class,
            TwinClassFieldCollectionMode.class,
            TwinClassFieldCollectionFilterRequiredMode.class,
            TwinClassFieldCollectionFilterSystemMode.class
    })
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassFieldMode.TwinClass2TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @MapperModePointerBinding(modes = LinkMode.TwinClass2LinkMode.class)
    private final LinkForwardRestDTOMapper linkForwardRestDTOMapper;

    @MapperModePointerBinding(modes = LinkMode.TwinClass2LinkMode.class)
    private final LinkBackwardRestDTOMapper linkBackwardRestDTOMapper;

    @MapperModePointerBinding(modes = {
            DataListOptionMode.TwinClassMarker2DataListOptionMode.class,
            DataListOptionMode.TwinClassTag2DataListOptionMode.class
    })
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @MapperModePointerBinding(modes = StatusMode.TwinClass2StatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.TwinClass2PermissionMode.class)
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.TwinClass2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @MapperModePointerBinding(modes = FaceMode.TwinClassPage2FaceMode.class)
    private final FaceRestDTOMapper faceRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassFreezeMode.TwinClassMode2TwinClassFreezeMode.class)
    private final TwinClassFreezeDTOMapper twinClassFreezeDTOMapper;

    private final I18nService i18nService;
    private final PermissionService permissionService;
    private final ResourceService resourceService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassService twinClassService;
    private final TwinStatusService twinStatusService;
    private final LinkService linkService;
    private final DataListService dataListService;

    @Override
    public void map(TwinClassEntity src, TwinClassDTOv1 dst, MapperContext mapperContext) throws Exception {
        if (mapperContext.hasMode(TwinClassMode.MANAGED) && !permissionService.currentUserHasPermission(Permissions.TWIN_CLASS_MANAGE)) {
            log.warn("Show Mode [{}] is not allowed for current user", TwinClassMode.MANAGED);
            mapperContext.setMode(TwinClassMode.DETAILED);
        }

        switch (mapperContext.getModeOrUse(TwinClassMode.DETAILED)) {
            case MANAGED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setHeadClassId(src.getHeadTwinClassId())
                        .setAbstractClass(src.getAbstractt())
                        .setUniqueName(src.getUniqueName())
                        .setMarkersDataListId(src.getMarkerDataListId())
                        .setTagsDataListId(src.getTagDataListId())
                        .setTwinClassFreezeId(src.getTwinClassFreezeId())
                        .setName(I18nCacheHolder.addId(src.getNameI18NId()))
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
                        .setIconDark(resourceService.getResourceUri(src.getIconDarkResource()))
                        .setIconLight(resourceService.getResourceUri(src.getIconLightResource()))
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setHeadHunterFeaturerId(src.getHeadHunterFeaturerId())
                        .setHeadHunterParams(src.getHeadHunterParams())
                        .setViewPermissionId(src.getViewPermissionId())
                        .setCreatePermissionId(src.getCreatePermissionId())
                        .setEditPermissionId(src.getEditPermissionId())
                        .setDeletePermissionId(src.getDeletePermissionId())
                        .setNameI18nId(src.getNameI18NId())
                        .setDescriptionI18nId(src.getDescriptionI18NId())
                        .setExtendsClassId(src.getExtendsTwinClassId())
                        .setExtendsClassIdSet(src.getExtendedClassIdSet())
                        .setTwinflowSchemaSpace(src.getTwinflowSchemaSpace())
                        .setTwinClassSchemaSpace(src.getTwinClassSchemaSpace())
                        .setPermissionSchemaSpace(src.getPermissionSchemaSpace())
                        .setAliasSpace(src.getAliasSpace())
                        .setOwnerType(src.getOwnerType())
                        .setPageFaceId(src.getPageFaceId())
                        .setBreadCrumbsFaceId(src.getBreadCrumbsFaceId())
                        .setInheritedPageFaceId(src.getInheritedPageFaceId())
                        .setInheritedBreadCrumbsFaceId(src.getInheritedBreadCrumbsFaceId())
                        .setAssigneeRequired(src.getAssigneeRequired())
                        .setExternalId(src.getExternalId())
                        .setExternalProperties(src.getExternalProperties())
                        .setSegment(src.getSegment())
                        .setHasSegment(src.getHasSegment())
                        .setHeadHierarchyCounterDirectChildren(src.getHeadHierarchyCounterDirectChildren())
                        .setExtendsHierarchyCounterDirectChildren(src.getExtendsHierarchyCounterDirectChildren());
                break;
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setHeadClassId(src.getHeadTwinClassId())
//                        .setHeadClass(convertOrPostpone(src))
                        .setAbstractClass(src.getAbstractt())
                        .setUniqueName(src.getUniqueName())
                        .setMarkersDataListId(src.getMarkerDataListId())
                        .setTagsDataListId(src.getTagDataListId())
                        .setTwinClassFreezeId(src.getTwinClassFreezeId())
                        .setName(I18nCacheHolder.addId(src.getNameI18NId()))
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
                        .setIconDark(resourceService.getResourceUri(src.getIconDarkResource()))
                        .setIconLight(resourceService.getResourceUri(src.getIconLightResource()))
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setExternalId(src.getExternalId())
                        .setExternalProperties(src.getExternalProperties())
                        .setSegment(src.getSegment())
                        .setHasSegment(src.getHasSegment());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey());
                break;
        }
        if (mapperContext.hasModeButNot(TwinClassExternalJsonMode.HIDE)) {
            dst.setExternalJson(src.getExternalJson());
        }
        if (mapperContext.hasModeButNot(TwinClassFieldCollectionMode.HIDE)) {
            twinClassFieldService.loadTwinClassFields(src);
            Stream<TwinClassFieldEntity> fieldsStream = src.getTwinClassFieldKit().getCollection().stream()
                    .filter(not(TwinClassFieldEntity::isBaseField));
            fieldsStream = switch (mapperContext.getModeOrUse(TwinClassFieldCollectionFilterRequiredMode.ANY)) {
                case ONLY -> fieldsStream.filter(TwinClassFieldEntity::getRequired);
                case ONLY_NOT -> fieldsStream.filter(not(TwinClassFieldEntity::getRequired));
                default -> fieldsStream;
            };
            fieldsStream = switch (mapperContext.getModeOrUse(TwinClassFieldCollectionFilterSystemMode.ANY)) {
                case ONLY -> fieldsStream.filter(TwinClassFieldEntity::getSystem);
                case ONLY_NOT -> fieldsStream.filter(not(TwinClassFieldEntity::getSystem));
                default -> fieldsStream;
            };
            List<TwinClassFieldEntity> collect = fieldsStream.toList();

            dst.setFieldIds(collect.stream().map(TwinClassFieldEntity::getId).collect(Collectors.toSet()));
            twinClassFieldRestDTOMapper.postpone(collect, mapperContext.forkOnPoint(TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE)); //todo only required
        }
        if (mapperContext.hasModeButNot(LinkMode.TwinClass2LinkMode.HIDE)) {
            //todo think over beforeCollectionConversion optimization
            LinkService.FindTwinClassLinksResult findTwinClassLinksResult = linkService.findLinks(src.getId());
            dst
                    .setForwardLinkMap(linkForwardRestDTOMapper.convertMap(findTwinClassLinksResult.getForwardLinks(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(LinkMode.TwinClass2LinkMode.SHORT))))
                    .setBackwardLinkMap(linkBackwardRestDTOMapper.convertMap(findTwinClassLinksResult.getBackwardLinks(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(LinkMode.TwinClass2LinkMode.SHORT))));
        }
        if (mapperContext.hasModeButNot(StatusMode.TwinClass2StatusMode.HIDE)) {
            twinStatusService.loadStatusesForTwinClasses(src);
            dst.setStatusIds(src.getTwinStatusKit().getIdSet());
            twinStatusRestDTOMapper.postpone(src.getTwinStatusKit(), mapperContext.forkOnPoint(StatusMode.TwinClass2StatusMode.SHORT));
        }
        //todo delete me
        if (mapperContext.hasModeButNot(DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE) && src.getMarkerDataListId() != null) {
            twinClassService.loadMarkerDataList(src);
            DataListEntity markerDataListEntity = src.getMarkerDataList();
            dataListService.loadDataListOptions(markerDataListEntity);
            if (markerDataListEntity.getOptions() != null) {
                MapperContext dataListMapperContext = mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListOptionMode.TwinClassMarker2DataListOptionMode.SHORT));
                if (mapperContext.isLazyRelations())
                    dst.setMarkerMap(dataListOptionRestDTOMapper.convertMap(markerDataListEntity.getOptions().getMap(), dataListMapperContext));
                else {
                    //dst.markerList(markerDataListEntity.getOptions().keySet().stream().toList());
                    dataListMapperContext.addRelatedObject(markerDataListEntity);
                }
            }
        }
        //todo delete me
        if (mapperContext.hasModeButNot(DataListOptionMode.TwinClassTag2DataListOptionMode.HIDE) && src.getTagDataListId() != null) {
            DataListEntity tagDataListEntity = dataListService.findEntitySafe(src.getTagDataListId());
            dataListService.loadDataListOptions(tagDataListEntity);
            if (tagDataListEntity.getOptions() != null) {
                MapperContext dataListMapperContext = mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListOptionMode.TwinClassTag2DataListOptionMode.SHORT));
                if (mapperContext.isLazyRelations())
                    dst.setTagMap(dataListOptionRestDTOMapper.convertMap(tagDataListEntity.getOptions().getMap(), dataListMapperContext));
                else {
                    //dst.tagList(tagDataListEntity.getOptions().getMap().keySet().stream().toList());
                    dataListMapperContext.addRelatedObject(tagDataListEntity);
                }
            }
        }
        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassHead2TwinClassMode.HIDE) && src.getHeadTwinClassId() != null) {
            twinClassService.loadHeadTwinClass(src);
            dst.setHeadClassId(src.getHeadTwinClassId());
            twinClassRestDTOMapper.postpone(src.getHeadTwinClass(), mapperContext.forkOnPoint(TwinClassMode.TwinClassHead2TwinClassMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassExtends2TwinClassMode.HIDE) && src.getExtendsTwinClassId() != null) {
            twinClassService.loadExtendsTwinClass(src);
            dst.setExtendsClassId(src.getExtendsTwinClassId());
            //todo perhaps we need to postpone all extended classes
            twinClassRestDTOMapper.convertOrPostpone(src.getExtendsTwinClass(), mapperContext.forkOnPoint(TwinClassMode.TwinClassExtends2TwinClassMode.SHORT));
        }
        if (mapperContext.hasModeButNot(PermissionMode.TwinClass2PermissionMode.HIDE) &&
                (src.getViewPermissionId() != null || src.getCreatePermissionId() != null || src.getEditPermissionId() != null || src.getDeletePermissionId() != null)) {
            twinClassService.loadPermissions(src);
            dst
                    .setViewPermissionId(src.getViewPermissionId())
                    .setCreatePermissionId(src.getCreatePermissionId())
                    .setEditPermissionId(src.getEditPermissionId())
                    .setDeletePermissionId(src.getDeletePermissionId());
            permissionRestDTOMapper.postpone(src.getViewPermission(), mapperContext.forkOnPoint(PermissionMode.TwinClass2PermissionMode.SHORT));
            permissionRestDTOMapper.postpone(src.getCreatePermission(), mapperContext.forkOnPoint(PermissionMode.TwinClass2PermissionMode.SHORT));
            permissionRestDTOMapper.postpone(src.getEditPermission(), mapperContext.forkOnPoint(PermissionMode.TwinClass2PermissionMode.SHORT));
            permissionRestDTOMapper.postpone(src.getDeletePermission(), mapperContext.forkOnPoint(PermissionMode.TwinClass2PermissionMode.SHORT));
        }
        if (mapperContext.hasModeButNot(FeaturerMode.TwinClass2FeaturerMode.HIDE)) {
            twinClassService.loadHeadHunter(src);
            dst.setHeadHunterFeaturerId(src.getHeadHunterFeaturerId());
            featurerRestDTOMapper.postpone(src.getHeadHunterFeaturer(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.TwinClass2FeaturerMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(FaceMode.TwinClassPage2FaceMode.HIDE)) {
            faceRestDTOMapper.postpone(src.getPageFace(), mapperContext.forkOnPoint(FaceMode.TwinClassPage2FaceMode.SHORT));
            dst.setPageFaceId(src.getPageFaceId());
        }
        if (mapperContext.hasModeButNot(TwinClassSegmentMode.HIDE)) {
            twinClassService.loadSegments(src);
            dst.setSegmentClassIds(src.getSegmentTwinsClassKit().getIdSet());
            postpone(src.getSegmentTwinsClassKit(), mapperContext.forkAndExclude(TwinClassSegmentMode.SHOW));
        }
        if (mapperContext.hasModeButNot(TwinClassFreezeMode.TwinClassMode2TwinClassFreezeMode.HIDE)) {
            twinClassService.loadFreeze(src);
            twinClassFreezeDTOMapper.postpone(src.getTwinClassFreeze(), mapperContext.forkOnPoint(TwinClassFreezeMode.TwinClassMode2TwinClassFreezeMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinClassEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(StatusMode.TwinClass2StatusMode.HIDE)) {
            twinStatusService.loadStatusesForTwinClasses(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinClassFieldCollectionMode.HIDE)) {
            twinClassFieldService.loadTwinClassFields(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassHead2TwinClassMode.HIDE)) {
            twinClassService.loadHeadTwinClasses(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassExtends2TwinClassMode.HIDE)) {
            twinClassService.loadExtendsTwinClasses(srcCollection);
        }
        if (mapperContext.hasModeButNot(DataListOptionMode.TwinClassMarker2DataListOptionMode.HIDE)) {
            twinClassService.loadMarkerDataList(srcCollection, true);
        }
        if (mapperContext.hasModeButNot(PermissionMode.TwinClass2PermissionMode.HIDE)) {
            twinClassService.loadPermissions(srcCollection);
        }
        if (mapperContext.hasModeButNot(FeaturerMode.TwinClass2FeaturerMode.HIDE)) {
            twinClassService.loadHeadHunter(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinClassSegmentMode.HIDE)) {
            twinClassService.loadSegments(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinClassFreezeMode.TwinClassMode2TwinClassFreezeMode.HIDE)) {
            twinClassService.loadFreeze(srcCollection);
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinClassMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinClassEntity src) {
        return src.getId().toString();
    }
}
