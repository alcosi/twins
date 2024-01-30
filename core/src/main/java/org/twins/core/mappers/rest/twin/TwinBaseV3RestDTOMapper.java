package org.twins.core.mappers.rest.twin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.common.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinBaseDTOv3;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkListRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinTransitionRestDTOMapper;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinMarkerService;
import org.twins.core.service.twin.TwinTagService;
import org.twins.core.service.twinflow.TwinflowTransitionService;

import java.util.Collection;
import java.util.List;


@Component
@RequiredArgsConstructor
public class TwinBaseV3RestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinBaseDTOv3> {
    final TwinBaseV2RestDTOMapper twinBaseV2RestDTOMapper;
    final AttachmentViewRestDTOMapper attachmentRestDTOMapper;
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
        if (!attachmentRestDTOMapper.hideMode(mapperContext))
            dst.setAttachments(attachmentRestDTOMapper.convertList(attachmentService.loadAttachments(src).getList(), mapperContext));
        if (!twinLinkListRestDTOMapper.hideMode(mapperContext))
            dst.setLinks(twinLinkListRestDTOMapper.convert(twinLinkService.loadTwinLinks(src), mapperContext));
        if (!twinTransitionRestDTOMapper.hideMode(mapperContext)) {
            twinflowTransitionService.loadValidTransitions(src);
            convertOrPostpone(src.getValidTransitionsKit(), dst, twinTransitionRestDTOMapper, mapperContext, TwinBaseDTOv3::setTransitions, TwinBaseDTOv3::setTransitionsIdList);
        }
        if (mapperContext.hasMode(TwinMarkerMode.SHOW)) {
            twinMarkerService.loadMarkers(src);
            convertOrPostpone(src.getTwinMarkerKit(), dst, dataListOptionRestDTOMapper, mapperContext, TwinBaseDTOv3::setMarkers, TwinBaseDTOv3::setMarkerIdList);
        }
        if (mapperContext.hasMode(TwinTagMode.SHOW)) {
            twinTagService.loadTags(src);
            convertOrPostpone(src.getTwinTagKit(), dst, dataListOptionRestDTOMapper, mapperContext, TwinBaseDTOv3::setTags, TwinBaseDTOv3::setTagIdList);
        }
    }

    @Override
    public void beforeListConversion(Collection<TwinEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeListConversion(srcCollection, mapperContext);

        if (!attachmentRestDTOMapper.hideMode(mapperContext))
            attachmentService.loadAttachments(srcCollection);
        if (!twinLinkListRestDTOMapper.hideMode(mapperContext))
            twinLinkService.loadTwinLinks(srcCollection);
        if (!twinTransitionRestDTOMapper.hideMode(mapperContext))
            twinflowTransitionService.loadValidTransitions(srcCollection);
        if (mapperContext.hasMode(TwinMarkerMode.SHOW))
            twinMarkerService.loadMarkers(srcCollection);

        if (mapperContext.hasMode(TwinTagMode.SHOW)) {
            twinTagService.loadTags(srcCollection);
        }
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
        SHOW(1);

        public static final String _HIDE = "HIDE";
        public static final String _SHOW = "SHOW";

        @Getter
        final int priority;
    }

    @AllArgsConstructor
    public enum TwinTagMode implements MapperMode {
        HIDE(0),
        SHOW(1);

        public static final String _HIDE = "HIDE";
        public static final String _SHOW = "SHOW";

        @Getter
        final int priority;
    }
}
