package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.mappers.rest.mappercontext.*;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkBackwardRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkForwardRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.*;
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
            TwinClassMode.TwinClassHeadOnTwinClassMode.class,
            TwinClassMode.TwinClassExtendsOnTwinClassMode.class
    })
    private final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;

    @MapperModePointerBinding(modes = ClassFieldMode.TwinClassOnClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @MapperModePointerBinding(modes = LinkMode.TwinClassOnLinkMode.class)
    private final LinkForwardRestDTOMapper linkForwardRestDTOMapper;

    @MapperModePointerBinding(modes = LinkMode.TwinClassOnLinkMode.class)
    private final LinkBackwardRestDTOMapper linkBackwardRestDTOMapper;

    @MapperModePointerBinding(modes = {
            DataListOptionMode.TwinClassMarkerModeOnDataListOptionMode.class,
            DataListOptionMode.TwinClassTagOnDataListOptionMode.class
    })
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;


    @MapperModePointerBinding(modes = StatusMode.TwinClassOnStatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassService twinClassService;
    private final TwinStatusService twinStatusService;
    private final LinkService linkService;
    private final DataListService dataListService;


    @Override
    public void map(TwinClassEntity src, TwinClassDTOv1 dst, MapperContext mapperContext) throws Exception {
        twinClassBaseRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(ClassFieldMode.TwinClassOnClassFieldMode.HIDE))
            dst.fields(
                    twinClassFieldRestDTOMapper.convertCollection(
                            twinClassFieldService.loadTwinClassFields(src).getCollection(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(ClassFieldMode.TwinClassOnClassFieldMode.SHORT)))); //todo only required
        if (mapperContext.hasModeButNot(LinkMode.TwinClassOnLinkMode.HIDE)) {
            LinkService.FindTwinClassLinksResult findTwinClassLinksResult = linkService.findLinks(src.getId());
            dst
                    .forwardLinkMap(linkForwardRestDTOMapper.convertMap(findTwinClassLinksResult.getForwardLinks(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(LinkMode.TwinClassOnLinkMode.SHORT))))
                    .backwardLinkMap(linkBackwardRestDTOMapper.convertMap(findTwinClassLinksResult.getBackwardLinks(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(LinkMode.TwinClassOnLinkMode.SHORT))));
        }
        if (mapperContext.hasModeButNot(StatusMode.TwinClassOnStatusMode.HIDE)) {
            Kit<TwinStatusEntity, UUID> statusKit = twinStatusService.loadStatusesForTwinClasses(src);
            if (statusKit != null) {
                if (mapperContext.isLazyRelations())
                    dst.statusMap(twinStatusRestDTOMapper.convertMap(statusKit.getMap(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(StatusMode.TwinClassOnStatusMode.SHORT))));
                else {
                    dst.statusList(mapperContext.addRelatedObjectMap(statusKit.getMap()));
                }
            }
        }
        if (mapperContext.hasModeButNot(DataListOptionMode.TwinClassMarkerModeOnDataListOptionMode.HIDE) && src.getMarkerDataListId() != null) {
            twinClassService.loadMarkerDataList(src);
            DataListEntity markerDataListEntity = src.getMarkerDataList();
            dataListService.loadDataListOptions(markerDataListEntity);
            if (markerDataListEntity.getOptions() != null) {
                MapperContext dataListMapperContext = mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListOptionMode.TwinClassMarkerModeOnDataListOptionMode.SHORT));
                if (mapperContext.isLazyRelations())
                    dst.markerMap(dataListOptionRestDTOMapper.convertMap(markerDataListEntity.getOptions().getMap(), dataListMapperContext));
                else {
                    //dst.markerList(markerDataListEntity.getOptions().keySet().stream().toList());
                    dataListMapperContext.addRelatedObject(markerDataListEntity);
                }
            }
        }
        if (mapperContext.hasModeButNot(DataListOptionMode.TwinClassTagOnDataListOptionMode.HIDE) && src.getTagDataListId() != null) {
            DataListEntity tagDataListEntity = dataListService.findEntitySafe(src.getTagDataListId());
            dataListService.loadDataListOptions(tagDataListEntity);
            if (tagDataListEntity.getOptions() != null) {
                MapperContext dataListMapperContext = mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListOptionMode.TwinClassTagOnDataListOptionMode.SHORT));
                if (mapperContext.isLazyRelations())
                    dst.tagMap(dataListOptionRestDTOMapper.convertMap(tagDataListEntity.getOptions().getMap(), dataListMapperContext));
                else {
                    //dst.tagList(tagDataListEntity.getOptions().getMap().keySet().stream().toList());
                    dataListMapperContext.addRelatedObject(tagDataListEntity);
                }
            }
        }
        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassHeadOnTwinClassMode.HIDE) && src.getHeadTwinClassId() != null) {
            twinClassService.loadHeadTwinClass(src);
            dst.headClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getHeadTwinClass(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.TwinClassHeadOnTwinClassMode.SHORT))));
        }
        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassExtendsOnTwinClassMode.HIDE) && src.getExtendsTwinClassId() != null) {
            twinClassService.loadExtendsTwinClass(src);
            dst.extendsClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getExtendsTwinClass(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.TwinClassExtendsOnTwinClassMode.SHORT))));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinClassEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(StatusMode.TwinClassOnStatusMode.HIDE)) {
            twinStatusService.loadStatusesForTwinClasses(srcCollection);
        }
        if (mapperContext.hasModeButNot(ClassFieldMode.TwinClassOnClassFieldMode.HIDE)) {
            twinClassFieldService.loadTwinClassFields(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassHeadOnTwinClassMode.HIDE)) {
            twinClassService.loadHeadTwinClasses(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinClassMode.TwinClassExtendsOnTwinClassMode.HIDE)) {
            twinClassService.loadExtendsTwinClasses(srcCollection);
        }
        if (mapperContext.hasModeButNot(DataListOptionMode.TwinClassMarkerModeOnDataListOptionMode.HIDE)) {
            twinClassService.loadMarkerDataList(srcCollection, true);
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
