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
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkListRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinTransitionRestDTOMapper;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinActionService;
import org.twins.core.service.twin.TwinMarkerService;
import org.twins.core.service.twin.TwinTagService;
import org.twins.core.service.twinflow.TwinflowTransitionService;

import java.util.Collection;


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
    final TwinActionService twinActionService;

    @Override
    public void map(TwinEntity src, TwinBaseDTOv3 dst, MapperContext mapperContext) throws Exception {
        twinBaseV2RestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasMode(TwinRestDTOMapper.FieldsMode.ALL_FIELDS_WITH_ATTACHMENTS) || mapperContext.hasMode(TwinRestDTOMapper.FieldsMode.NOT_EMPTY_FIELDS_WITH_ATTACHMENTS)) {
            mapperContext.setPriorityMinMode(AttachmentViewRestDTOMapper.TwinAttachmentMode.FROM_FIELDS);
            mapperContext.setPriorityMinMode(AttachmentViewRestDTOMapper.Mode.SHORT);
        }
        if (!attachmentRestDTOMapper.hideMode(mapperContext))
            dst.setAttachments(attachmentRestDTOMapper.convertList(attachmentService.loadAttachments(src).getCollection(), mapperContext));
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
        if (showActions(mapperContext)) {
            twinActionService.loadActions(src);
            dst.setActions(src.getActions());
        }
    }

    private static boolean showMarkers(MapperContext mapperContext) {
        return !mapperContext.hasModeOrEmpty(TwinMarkerMode.HIDE);
    }

    private static boolean showTags(MapperContext mapperContext) {
        return !mapperContext.hasModeOrEmpty(TwinTagMode.HIDE);
    }

    private static boolean showActions(MapperContext mapperContext) {
        return !mapperContext.hasModeOrEmpty(TwinActionMode.HIDE);
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
        if (showActions(mapperContext))
            twinActionService.loadActions(srcCollection);
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

    @AllArgsConstructor
    public enum TwinActionMode implements MapperMode {
        HIDE(0),
        SHOW(1);

        public static final String _HIDE = "HIDE";
        public static final String _SHOW = "SHOW";

        @Getter
        final int priority;
    }
}
