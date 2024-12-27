package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkBackwardRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkForwardRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Collection;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TwinClassRestDTOMapper extends RestSimpleDTOMapper<TwinClassEntity, TwinClassDTOv1> {

    @MapperModePointerBinding(modes = {
            TwinClassMode.class,
            TwinClassMode.TwinClassHead2TwinClassMode.class,
            TwinClassMode.TwinClassExtends2TwinClassMode.class
    })
    private final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;

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

    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassService twinClassService;
    private final TwinStatusService twinStatusService;
    private final LinkService linkService;
    private final DataListService dataListService;
    private final FeaturerService featurerService;


    @Override
    public void map(TwinClassEntity src, TwinClassDTOv1 dst, MapperContext mapperContext) throws Exception {
        twinClassBaseRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE))
            dst.setFields(
                    twinClassFieldRestDTOMapper.convertCollection(
                            twinClassFieldService.loadTwinClassFields(src).getCollection(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassFieldMode.TwinClass2TwinClassFieldMode.SHORT)))); //todo only required
        if (mapperContext.hasModeButNot(LinkMode.TwinClass2LinkMode.HIDE)) {
            //todo think over beforeCollectionConversion optimization
            LinkService.FindTwinClassLinksResult findTwinClassLinksResult = linkService.findLinks(src.getId());
            dst
                    .setForwardLinkMap(linkForwardRestDTOMapper.convertMap(findTwinClassLinksResult.getForwardLinks(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(LinkMode.TwinClass2LinkMode.SHORT))))
                    .setBackwardLinkMap(linkBackwardRestDTOMapper.convertMap(findTwinClassLinksResult.getBackwardLinks(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(LinkMode.TwinClass2LinkMode.SHORT))));
        }
        if (mapperContext.hasModeButNot(StatusMode.TwinClass2StatusMode.HIDE)) {
            Kit<TwinStatusEntity, UUID> statusKit = twinStatusService.loadStatusesForTwinClasses(src);
            if (statusKit != null) {
                MapperContext statusMapperContext = mapperContext.forkOnPoint(mapperContext.getModeOrUse(StatusMode.TwinClass2StatusMode.SHORT));
                if (mapperContext.isLazyRelations())
                    dst.setStatusMap(twinStatusRestDTOMapper.convertMap(statusKit.getMap(), statusMapperContext));
                else {
                    dst.setStatusList(statusMapperContext.addRelatedObjectMap(statusKit.getMap()));
                }
            }
        }
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
            dst
                    .setHeadClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getHeadTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.TwinClassHead2TwinClassMode.SHORT))))
                    .setHeadClassId(src.getHeadTwinClassId());
        }
        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassExtends2TwinClassMode.HIDE) && src.getExtendsTwinClassId() != null) {
            twinClassService.loadExtendsTwinClass(src);
            dst
                    .setExtendsClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getExtendsTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.TwinClassExtends2TwinClassMode.SHORT))))
                    .setExtendsClassId(src.getExtendsTwinClassId());
        }
        if (mapperContext.hasModeButNot(PermissionMode.TwinClass2PermissionMode.HIDE) && src.getViewPermissionId() != null) {
            twinClassService.loadPermissions(src);
            dst
                    .setViewPermission(permissionRestDTOMapper.convert(src.getViewPermission(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(PermissionMode.TwinClass2PermissionMode.SHORT))))
                    .setCreatePermission(permissionRestDTOMapper.convert(src.getCreatePermission(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(PermissionMode.TwinClass2PermissionMode.SHORT))))
                    .setEditPermission(permissionRestDTOMapper.convert(src.getEditPermission(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(PermissionMode.TwinClass2PermissionMode.SHORT))))
                    .setDeletePermission(permissionRestDTOMapper.convert(src.getDeletePermission(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(PermissionMode.TwinClass2PermissionMode.SHORT))))
                    .setViewPermissionId(src.getViewPermissionId())
                    .setCreatePermissionId(src.getCreatePermissionId())
                    .setEditPermissionId(src.getEditPermissionId())
                    .setDeletePermissionId(src.getDeletePermissionId());
        }
        if (mapperContext.hasModeButNot(FeaturerMode.TwinClass2FeaturerMode.HIDE)) {
            featurerService.loadHeadHunter(src);
            dst
                    .setHeadHunterFeaturer(featurerRestDTOMapper.convertOrPostpone(src.getHeadHunterFeaturer(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.TwinClass2FeaturerMode.SHORT))))
                    .setHeadHunterFeaturerId(src.getHeadHunterFeaturerId());
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinClassEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(StatusMode.TwinClass2StatusMode.HIDE)) {
            twinStatusService.loadStatusesForTwinClasses(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinClassFieldMode.TwinClass2TwinClassFieldMode.HIDE)) {
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
            featurerService.loadHeadHunter(srcCollection);
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
