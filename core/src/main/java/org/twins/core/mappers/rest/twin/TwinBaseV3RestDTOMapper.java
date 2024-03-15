package org.twins.core.mappers.rest.twin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinBaseDTOv3;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapperV2;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkListRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinTransitionRestDTOMapper;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinMarkerService;
import org.twins.core.service.twin.TwinTagService;
import org.twins.core.service.twinflow.TwinflowTransitionService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class TwinBaseV3RestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinBaseDTOv3> {
    final TwinBaseV2RestDTOMapper twinBaseV2RestDTOMapper;
    final AttachmentViewRestDTOMapper attachmentRestDTOMapper;
    final TwinAttachmentMapper twinAttachmentMapper;
    final AttachmentViewRestDTOMapperV2 attachmentViewRestDTOMapperV2;
    final AttachmentService attachmentService;
    final TwinLinkService twinLinkService;
    final TwinflowTransitionService twinflowTransitionService;
    final TwinLinkListRestDTOMapper twinLinkListRestDTOMapper;
    final TwinTransitionRestDTOMapper twinTransitionRestDTOMapper;
    final TwinMarkerService twinMarkerService;
    final TwinTagService twinTagService;
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @Override
    public void map(TwinEntity src, TwinBaseDTOv3 dst, MapperContext mapperContext) throws Exception {
        twinBaseV2RestDTOMapper.map(src, dst, mapperContext);
//        if (!twinAttachmentMapper.noneMode(mapperContext)){
//            dst.setAttachments(attachmentRestDTOMapper.convertList(twinAttachmentMapper.map(attachmentService.loadAttachments(src).getList(), mapperContext), mapperContext));
//            mapperContext.setMode(AttachmentViewRestDTOMapper.Mode.HIDE);
//        }
        if (!attachmentRestDTOMapper.hideMode(mapperContext))
            dst.setAttachments(attachmentRestDTOMapper.convertList(attachmentViewRestDTOMapperV2.filterAttachment(attachmentService.loadAttachments(src).getList(), mapperContext), mapperContext));
        if (!twinLinkListRestDTOMapper.hideMode(mapperContext))
            dst.setLinks(twinLinkListRestDTOMapper.convert(twinLinkService.loadTwinLinks(src), mapperContext));
        if (!twinTransitionRestDTOMapper.hideMode(mapperContext)) {
            twinflowTransitionService.loadValidTransitions(src);
            convertOrPostpone(src.getValidTransitionsKit(), dst, twinTransitionRestDTOMapper, mapperContext, TwinBaseDTOv3::setTransitions, TwinBaseDTOv3::setTransitionsIdList);
        }
        if (showMarkers(mapperContext)) {
            twinMarkerService.loadMarkers(src);
            DataListOptionRestDTOMapper.Mode showDataListOptionMode = mapperContext.hasMode(TwinMarkerMode.DETAILED) ? DataListOptionRestDTOMapper.Mode.DETAILED : DataListOptionRestDTOMapper.Mode.SHORT;
            convertOrPostpone(src.getTwinMarkerKit(), dst, dataListOptionRestDTOMapper, mapperContext.cloneWithIsolatedModes().setModeIfNotPresent(showDataListOptionMode), TwinBaseDTOv3::setMarkers, TwinBaseDTOv3::setMarkerIdList);
        }
        if (showTags(mapperContext)) {
            twinTagService.loadTags(src);
            DataListOptionRestDTOMapper.Mode showDataListOptionMode = mapperContext.hasMode(TwinTagMode.DETAILED) ? DataListOptionRestDTOMapper.Mode.DETAILED : DataListOptionRestDTOMapper.Mode.SHORT;
            convertOrPostpone(src.getTwinTagKit(), dst, dataListOptionRestDTOMapper, mapperContext.cloneWithIsolatedModes().setModeIfNotPresent(showDataListOptionMode), TwinBaseDTOv3::setTags, TwinBaseDTOv3::setTagIdList);
        }
    }

    private static boolean showMarkers(MapperContext mapperContext) {
        return !mapperContext.hasModeOrEmpty(TwinMarkerMode.HIDE);
    }

    private static boolean showTags(MapperContext mapperContext) {
        return !mapperContext.hasModeOrEmpty(TwinTagMode.HIDE);
    }

    @Override
    public void beforeListConversion(Collection<TwinEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeListConversion(srcCollection, mapperContext);
        if (!attachmentRestDTOMapper.hideMode(mapperContext))
            attachmentService.loadAttachments(srcCollection);
        if (showMarkers(mapperContext))
            twinMarkerService.loadMarkers(srcCollection);
        if (showTags(mapperContext))
            twinTagService.loadTags(srcCollection);
        if (!twinLinkListRestDTOMapper.hideMode(mapperContext))
            twinLinkService.loadTwinLinks(srcCollection);
        if (!twinTransitionRestDTOMapper.hideMode(mapperContext))
            twinflowTransitionService.loadValidTransitions(srcCollection);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return twinBaseV2RestDTOMapper.hideMode(mapperContext);
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }

    @AllArgsConstructor
    public enum TwinMarkerMode implements MapperMode {
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
    public enum TwinTagMode implements MapperMode {
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
