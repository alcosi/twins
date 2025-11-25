package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinBaseDTOv3;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentRestDTOMapper;
import org.twins.core.mappers.rest.attachment.TwinAttachmentsCounterRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkListRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinTransitionRestDTOMapper;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.*;
import org.twins.core.service.twinflow.TwinflowTransitionService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinActionMode.class, TwinSegmentMode.class})
public class TwinBaseV3RestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinBaseDTOv3> {

    private final TwinBaseRestDTOMapper twinBaseRestDTOMapper;

    @MapperModePointerBinding(modes = {AttachmentMode.Twin2AttachmentMode.class, AttachmentCollectionMode.Twin2AttachmentCollectionMode.class})
    private final AttachmentRestDTOMapper attachmentRestDTOMapper;

    @MapperModePointerBinding(modes = {TwinLinkMode.Twin2TwinLinkMode.class})
    private final TwinLinkListRestDTOMapper twinLinkListRestDTOMapper;

    @MapperModePointerBinding(modes = {TransitionMode.Twin2TransitionMode.class})
    private final TwinTransitionRestDTOMapper twinTransitionRestDTOMapper;

    @MapperModePointerBinding(modes = {DataListOptionMode.TwinTag2DataListOptionMode.class, DataListOptionMode.TwinMarker2DataListOptionMode.class})
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @MapperModePointerBinding(modes = {TwinAttachmentCountMode.class})
    private final TwinAttachmentsCounterRestDTOMapper twinAttachmentsCounterRestDTOMapper;

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = {TwinClassMode.TwinCreatableChild2TwinClassMode.class})
    private TwinClassRestDTOMapper twinClassRestDTOMapper;

    final TwinActionService twinActionService;
    final AttachmentService attachmentService;
    final TwinLinkService twinLinkService;
    final TwinMarkerService twinMarkerService;
    final TwinTagService twinTagService;
    final TwinflowTransitionService twinflowTransitionService;
    final TwinService twinService;
    final TwinHeadService twinHeadService;

    @Override
    public void map(TwinEntity src, TwinBaseDTOv3 dst, MapperContext mapperContext) throws Exception {
        twinBaseRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(AttachmentMode.TwinField2AttachmentMode.HIDE)) {
            //todo not clear code
            mapperContext.setPriorityMinMode(AttachmentCollectionMode.Twin2AttachmentCollectionMode.FROM_FIELDS);
            mapperContext.setPriorityMinMode(AttachmentMode.Twin2AttachmentMode.SHORT);
        }
        if (showAttachments(mapperContext)) {
            attachmentService.loadAttachments(src);
            dst.setAttachments(attachmentRestDTOMapper.convertCollection(src.getAttachmentKit().getCollection(), mapperContext.forkOnPoint(AttachmentMode.Twin2AttachmentMode.SHORT, AttachmentCollectionMode.Twin2AttachmentCollectionMode.FROM_FIELDS)));
        }
        //todo do optimization load
        if (showTwinAttachmentsCount(mapperContext)) {
            dst.setAttachmentsCount(twinAttachmentsCounterRestDTOMapper.convert(src, mapperContext));
        }
        if (showLinks(mapperContext)) {
            twinLinkService.loadTwinLinks(src);
            dst.setLinks(twinLinkListRestDTOMapper.convert(src.getTwinLinks(), mapperContext.forkOnPoint(TwinLinkMode.Twin2TwinLinkMode.SHORT)));
        }
        if (showTransitions(mapperContext)) {
            twinflowTransitionService.loadValidTransitions(src);
            dst.setTransitionsIdList(src.getValidTransitionsKit().getIdSet());
            twinTransitionRestDTOMapper.postpone(src.getValidTransitionsKit(), mapperContext.forkOnPoint(TransitionMode.Twin2TransitionMode.HIDE));
        }
        if (showMarkers(mapperContext)) {
            twinMarkerService.loadMarkers(src);
            dst.setMarkerIdList(src.getTwinMarkerKit().getIdSet());
            dataListOptionRestDTOMapper.postpone(src.getTwinMarkerKit(), mapperContext.forkOnPoint(DataListOptionMode.TwinMarker2DataListOptionMode.HIDE));
        }
        if (showTags(mapperContext)) {
            twinTagService.loadTags(src);
            dst.setTagIdList(src.getTwinTagKit().getIdSet());
            dataListOptionRestDTOMapper.postpone(src.getTwinTagKit(), mapperContext.forkOnPoint(DataListOptionMode.TwinTag2DataListOptionMode.HIDE));
        }
        if (showActions(mapperContext)) {
            twinActionService.loadActions(src);
            dst.setActions(src.getActions());
        }
        if (showCreatableChildTwinClasses(mapperContext)) {
            twinHeadService.loadCreatableChildTwinClasses(src);
            dst.setCreatableChildTwinClassIds(src.getCreatableChildTwinClasses().getIdSet());
            twinClassRestDTOMapper.postpone(src.getCreatableChildTwinClasses(), mapperContext.forkOnPoint(TwinClassMode.TwinCreatableChild2TwinClassMode.HIDE));
        }
        if (showSegments(mapperContext)) {
            twinService.loadSegments(src);
            dst.setSegmentTwinIdList(src.getSegments().getIdSet());
            postpone(src.getSegments(), mapperContext.forkAndExclude(TwinSegmentMode.SHOW));
        }
    }

    private static boolean showMarkers(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(DataListOptionMode.TwinMarker2DataListOptionMode.HIDE);
    }

    private static boolean showTags(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(DataListOptionMode.TwinTag2DataListOptionMode.HIDE);
    }

    private static boolean showActions(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(TwinActionMode.HIDE);
    }

    private static boolean showCreatableChildTwinClasses(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(TwinClassMode.TwinCreatableChild2TwinClassMode.HIDE);
    }

    private static boolean showTransitions(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(TransitionMode.Twin2TransitionMode.HIDE);
    }

    private static boolean showAttachments(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(AttachmentMode.Twin2AttachmentMode.HIDE);
    }

    private static boolean showLinks(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(TwinLinkMode.Twin2TwinLinkMode.HIDE);
    }

    private static boolean showTwinAttachmentsCount(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(TwinAttachmentCountMode.HIDE);
    }

    private static boolean showSegments(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(TwinSegmentMode.HIDE);
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        twinBaseRestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
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
        if (showTwinAttachmentsCount(mapperContext))
            twinAttachmentsCounterRestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
        if (showCreatableChildTwinClasses(mapperContext))
            twinHeadService.loadCreatableChildTwinClasses(srcCollection);
        if (showSegments(mapperContext))
            twinService.loadSegments(srcCollection);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return twinBaseRestDTOMapper.hideMode(mapperContext);
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }


}
