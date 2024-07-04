package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
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
@MapperModeBinding(modes = {MapperMode.TwinActionMode.class})
public class TwinBaseV3RestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinBaseDTOv3> {

    private final TwinBaseV2RestDTOMapper twinBaseV2RestDTOMapper;

    @MapperModePointerBinding(modes = {MapperMode.TwinAttachmentMode.class, MapperMode.TwinAttachmentCollectionMode.class})
    private final AttachmentViewRestDTOMapper attachmentRestDTOMapper;

    @MapperModePointerBinding(modes = {MapperMode.TwinLinkMode.class})
    private final TwinLinkListRestDTOMapper twinLinkListRestDTOMapper;

    @MapperModePointerBinding(modes = {MapperMode.TwinTransitionMode.class})
    private final TwinTransitionRestDTOMapper twinTransitionRestDTOMapper;

    @MapperModePointerBinding(modes = {MapperMode.TwinTagMode.class, MapperMode.TwinMarkerMode.class})
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    final TwinActionService twinActionService;
    final AttachmentService attachmentService;
    final TwinLinkService twinLinkService;
    final TwinMarkerService twinMarkerService;
    final TwinTagService twinTagService;
    final TwinflowTransitionService twinflowTransitionService;

    @Override
    public void map(TwinEntity src, TwinBaseDTOv3 dst, MapperContext mapperContext) throws Exception {
        twinBaseV2RestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasMode(MapperMode.TwinFieldCollectionMode.ALL_FIELDS_WITH_ATTACHMENTS) || mapperContext.hasMode(MapperMode.TwinFieldCollectionMode.NOT_EMPTY_FIELDS_WITH_ATTACHMENTS)) {
            mapperContext.setPriorityMinMode(MapperMode.TwinAttachmentCollectionMode.FROM_FIELDS);
            mapperContext.setPriorityMinMode(MapperMode.TwinAttachmentMode.SHORT);
        }
        if (showAttachments(mapperContext)) {
            attachmentService.loadAttachments(src);
            dst.setAttachments(attachmentRestDTOMapper.convertCollection(src.getAttachmentKit().getCollection(), mapperContext.forkOnPoint(MapperMode.TwinAttachmentMode.SHORT, MapperMode.TwinAttachmentCollectionMode.FROM_FIELDS)));
        }
        if (showLinks(mapperContext)) {
            twinLinkService.loadTwinLinks(src);
            dst.setLinks(twinLinkListRestDTOMapper.convert(src.getTwinLinks(), mapperContext.forkOnPoint(MapperMode.TwinLinkMode.SHORT)));
        }
        if (showTransitions(mapperContext)) {
            twinflowTransitionService.loadValidTransitions(src);
            convertOrPostpone(src.getValidTransitionsKit(), dst, twinTransitionRestDTOMapper, mapperContext.forkOnPoint(MapperMode.TwinTransitionMode.HIDE), TwinBaseDTOv3::setTransitions, TwinBaseDTOv3::setTransitionsIdList);
        }
        if (showMarkers(mapperContext)) {
            twinMarkerService.loadMarkers(src);
            convertOrPostpone(src.getTwinMarkerKit(), dst, dataListOptionRestDTOMapper, mapperContext.forkOnPoint(MapperMode.TwinMarkerMode.HIDE), TwinBaseDTOv3::setMarkers, TwinBaseDTOv3::setMarkerIdList);
        }
        if (showTags(mapperContext)) {
            twinTagService.loadTags(src);
            convertOrPostpone(src.getTwinTagKit(), dst, dataListOptionRestDTOMapper, mapperContext.forkOnPoint(MapperMode.TwinTagMode.HIDE), TwinBaseDTOv3::setTags, TwinBaseDTOv3::setTagIdList);
        }
        if (showActions(mapperContext)) {
            twinActionService.loadActions(src);
            dst.setActions(src.getActions());
        }
    }

    private static boolean showMarkers(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(MapperMode.TwinMarkerMode.HIDE);
    }

    private static boolean showTags(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(MapperMode.TwinTagMode.HIDE);
    }

    private static boolean showActions(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(MapperMode.TwinActionMode.HIDE);
    }

    private static boolean showTransitions(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(MapperMode.TwinTransitionMode.HIDE);
    }

    private static boolean showAttachments(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(MapperMode.TwinAttachmentMode.HIDE);
    }

    private static boolean showLinks(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(MapperMode.TwinLinkMode.HIDE);
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        twinBaseV2RestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
        if (showAttachments(mapperContext))
            attachmentService.loadAttachments(srcCollection);
        if (showMarkers(mapperContext))
            twinMarkerService.loadMarkers(srcCollection);
        if (showTags(mapperContext))
            twinTagService.loadTags(srcCollection);
        if (showActions(mapperContext))
            twinActionService.loadActions(srcCollection);
        if (showLinks(mapperContext))
            twinLinkService.loadTwinLinks(srcCollection);
        if (showTransitions(mapperContext))
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


}
