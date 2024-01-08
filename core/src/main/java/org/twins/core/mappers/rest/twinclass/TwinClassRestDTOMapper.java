package org.twins.core.mappers.rest.twinclass;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.common.Kit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.twin.TwinEntity;
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
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinHeadService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TwinClassRestDTOMapper extends RestSimpleDTOMapper<TwinClassEntity, TwinClassDTOv1> {
    final TwinClassFieldService twinClassFieldService;
    final TwinService twinService;
    final TwinHeadService twinHeadService;
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
                    twinClassFieldRestDTOMapper.convertList(
                            twinClassFieldService.loadTwinClassFields(src).getList(), mapperContext.setModeIfNotPresent(TwinClassFieldRestDTOMapper.Mode.SHORT))); //todo only required
        if (mapperContext.hasMode(HeadTwinMode.SHOW) && src.getHeadTwinClassId() != null) {
            Map<UUID, TwinEntity> validHeadsMap = twinHeadService.findValidHeadsAsMap(src);
            if (mapperContext.isLazyRelations())
                dst.validHeadsMap(twinBaseRestDTOMapper.convertMap(validHeadsMap, mapperContext.cloneWithIsolatedModes().setMode(TwinBaseRestDTOMapper.TwinMode.SHORT)));
            else {
                dst.validHeadsIds(mapperContext.addRelatedObjectMap(validHeadsMap));
            }
        }
        if (!linkForwardRestDTOMapper.hideMode(mapperContext)) {
            LinkService.FindTwinClassLinksResult findTwinClassLinksResult = linkService.findLinks(src.getId());
            dst
                    .forwardLinkMap(linkForwardRestDTOMapper.convertMap(findTwinClassLinksResult.getForwardLinks(), mapperContext))
                    .backwardLinkMap(linkBackwardRestDTOMapper.convertMap(findTwinClassLinksResult.getBackwardLinks(), mapperContext));
        }
        if (mapperContext.hasMode(StatusMode.SHOW)) {
            Kit<TwinStatusEntity> statusKit = twinStatusService.findByTwinClassAsMap(src);
            if (statusKit != null) {
                if (mapperContext.isLazyRelations())
                    dst.statusMap(twinStatusRestDTOMapper.convertMap(statusKit.getMap(), mapperContext));
                else {
                    dst.statusList(mapperContext.addRelatedObjectMap(statusKit.getMap()));
                }
            }
        }
        if (!mapperContext.hasMode(MarkerMode.HIDE) && src.getMarkerDataListId() != null) {
            DataListEntity markerDataListEntity = dataListService.findEntitySafe(src.getMarkerDataListId());
            dataListService.loadDataListOptions(markerDataListEntity);
            if (markerDataListEntity.getOptions() != null) {
                MapperContext dataListMapperContext = mapperContext.cloneWithIsolatedModes()
                        .setModeIfNotPresent(mapperContext.hasMode(MarkerMode.SHORT) ? DataListOptionRestDTOMapper.Mode.SHORT : DataListOptionRestDTOMapper.Mode.DETAILED);
                if (mapperContext.isLazyRelations())
                    dst.markerMap(dataListOptionRestDTOMapper.convertMap(markerDataListEntity.getOptions(), dataListMapperContext));
                else {
                    dst.markerList(markerDataListEntity.getOptions().keySet().stream().toList());
                    dataListMapperContext.addRelatedObject(markerDataListEntity);
                }
            }
        }
        if (!mapperContext.hasMode(TagMode.HIDE) && src.getTagDataListId() != null) {
            DataListEntity tagDataListEntity = dataListService.findEntitySafe(src.getTagDataListId());
            dataListService.loadDataListOptions(tagDataListEntity);
            if (tagDataListEntity.getOptions() != null) {
                if (mapperContext.isLazyRelations())
                    dst.tagMap(dataListOptionRestDTOMapper.convertMap(tagDataListEntity.getOptions(), mapperContext.cloneWithIsolatedModes().setModeIfNotPresent(DataListOptionRestDTOMapper.Mode.SHORT)));
                else {
                    dst.tagList(tagDataListEntity.getOptions().keySet().stream().toList());
                    mapperContext.addRelatedObject(tagDataListEntity);
                }
            }
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
    public enum HeadTwinMode implements MapperMode {
        HIDE(0),
        SHOW(1);
        public static final String _SHOW = "SHOW";
        public static final String _HIDE = "HIDE";
        @Getter
        final int priority;
    }

    @AllArgsConstructor
    public enum StatusMode implements MapperMode {
        HIDE(0),
        SHOW(1);
        public static final String _SHOW = "SHOW";
        public static final String _HIDE = "HIDE";
        @Getter
        final int priority;
    }

    @AllArgsConstructor
    public enum MarkerMode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }

    @AllArgsConstructor
    public enum TagMode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }
}
