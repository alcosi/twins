package org.twins.core.mappers.rest.twinclass;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkBackwardRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkForwardRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinBaseRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Collection;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TwinClassRestDTOMapper extends RestSimpleDTOMapper<TwinClassEntity, TwinClassDTOv1> {
    final TwinClassFieldService twinClassFieldService;
    final TwinService twinService;
    final TwinClassService twinClassService;
    final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;
    final LinkForwardRestDTOMapper linkForwardRestDTOMapper;
    final LinkBackwardRestDTOMapper linkBackwardRestDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
    final TwinStatusService twinStatusService;
    final LinkService linkService;
    final DataListService dataListService;
    @Lazy
    @Autowired
    final TwinBaseRestDTOMapper twinBaseRestDTOMapper;

    @Override
    public void map(TwinClassEntity src, TwinClassDTOv1 dst, MapperContext mapperContext) throws Exception {
        twinClassBaseRestDTOMapper.map(src, dst, mapperContext);
        if (!twinClassFieldRestDTOMapper.hideMode(mapperContext))
            dst.fields(
                    twinClassFieldRestDTOMapper.convertCollection(
                            twinClassFieldService.loadTwinClassFields(src).getCollection(), mapperContext.setModeIfNotPresent(MapperMode.TwinClassFieldMode.SHORT))); //todo only required
        if (mapperContext.hasModeButNot(MapperMode.TwinClassLinkMode.HIDE)) {
            LinkService.FindTwinClassLinksResult findTwinClassLinksResult = linkService.findLinks(src.getId());
            dst
                    .forwardLinkMap(linkForwardRestDTOMapper.convertMap(findTwinClassLinksResult.getForwardLinks(), mapperContext))
                    .backwardLinkMap(linkBackwardRestDTOMapper.convertMap(findTwinClassLinksResult.getBackwardLinks(), mapperContext));
        }
        if (mapperContext.hasModeButNot(MapperMode.TwinClassStatusMode.HIDE)) {
            Kit<TwinStatusEntity, UUID> statusKit = twinStatusService.loadStatusesForTwinClasses(src);
            if (statusKit != null) {
                if (mapperContext.isLazyRelations())
                    dst.statusMap(twinStatusRestDTOMapper.convertMap(statusKit.getMap(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(MapperMode.TwinClassStatusMode.SHORT))));
                else {
                    dst.statusList(mapperContext.addRelatedObjectMap(statusKit.getMap()));
                }
            }
        }
        if (!mapperContext.hasModeOrEmpty(MapperMode.TwinClassMarkerMode.HIDE) && src.getMarkerDataListId() != null) {
            twinClassService.loadMarkerDataList(src);
            DataListEntity markerDataListEntity = src.getMarkerDataList();
            dataListService.loadDataListOptions(markerDataListEntity);
            if (markerDataListEntity.getOptions() != null) {
                MapperContext dataListMapperContext = mapperContext.cloneWithIsolatedModes()
                        .setModeIfNotPresent(mapperContext.hasMode(MapperMode.TwinClassMarkerMode.SHORT) ? MapperMode.DataListOptionMode.SHORT : MapperMode.DataListOptionMode.DETAILED);
                if (mapperContext.isLazyRelations())
                    dst.markerMap(dataListOptionRestDTOMapper.convertMap(markerDataListEntity.getOptions().getMap(), dataListMapperContext));
                else {
                    //dst.markerList(markerDataListEntity.getOptions().keySet().stream().toList());
                    dataListMapperContext.addRelatedObject(markerDataListEntity);
                }
            }
        }
        if (!mapperContext.hasModeOrEmpty(MapperMode.TwinClassTagMode.HIDE) && src.getTagDataListId() != null) {
            DataListEntity tagDataListEntity = dataListService.findEntitySafe(src.getTagDataListId());
            dataListService.loadDataListOptions(tagDataListEntity);
            if (tagDataListEntity.getOptions() != null) {
                MapperContext dataListMapperContext = mapperContext.cloneWithIsolatedModes().setModeIfNotPresent(mapperContext.hasMode(MapperMode.TwinClassTagMode.SHORT) ? MapperMode.DataListOptionMode.SHORT : MapperMode.DataListOptionMode.DETAILED);
                if (mapperContext.isLazyRelations())
                    dst.tagMap(dataListOptionRestDTOMapper.convertMap(tagDataListEntity.getOptions().getMap(), dataListMapperContext));
                else {
                    //dst.tagList(tagDataListEntity.getOptions().getMap().keySet().stream().toList());
                    dataListMapperContext.addRelatedObject(tagDataListEntity);
                }
            }
        }
        if (mapperContext.hasMode(HeadClassMode.SHOW) && src.getHeadTwinClassId() != null) {
            twinClassService.loadHeadTwinClass(src);
            dst.headClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getHeadTwinClass(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(MapperMode.TwinClassMode.SHORT))));
        }
        if (mapperContext.hasMode(ExtendsClassMode.SHOW) && src.getExtendsTwinClassId() != null) {
            twinClassService.loadExtendsTwinClass(src);
            dst.extendsClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getExtendsTwinClass(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(MapperMode.TwinClassMode.SHORT))));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinClassEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(MapperMode.TwinClassStatusMode.HIDE)) {
            twinStatusService.loadStatusesForTwinClasses(srcCollection);
        }
        if (!twinClassFieldRestDTOMapper.hideMode(mapperContext)) {
            twinClassFieldService.loadTwinClassFields(srcCollection);
        }
        if (mapperContext.hasMode(HeadClassMode.SHOW)) {
            twinClassService.loadHeadTwinClasses(srcCollection);
        }
        if (mapperContext.hasMode(HeadClassMode.SHOW)) {
            twinClassService.loadExtendsTwinClasses(srcCollection);
        }
        if (!mapperContext.hasModeOrEmpty(MapperMode.TwinClassMarkerMode.HIDE)) {
            twinClassService.loadMarkerDataList(srcCollection, true);
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return twinClassBaseRestDTOMapper.hideMode(mapperContext);
    }

    @Override
    public String getObjectCacheId(TwinClassEntity src) {
        return src.getId().toString();
    }

    @AllArgsConstructor
    public enum HeadClassMode implements MapperMode {
        HIDE(0),
        SHOW(1);
        public static final String _SHOW = "SHOW";
        public static final String _HIDE = "HIDE";
        @Getter
        final int priority;
    }

    @AllArgsConstructor
    public enum ExtendsClassMode implements MapperMode {
        HIDE(0),
        SHOW(1);
        public static final String _SHOW = "SHOW";
        public static final String _HIDE = "HIDE";
        @Getter
        final int priority;
    }
}
