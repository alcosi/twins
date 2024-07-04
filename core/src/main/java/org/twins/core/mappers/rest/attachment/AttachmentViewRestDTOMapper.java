package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentViewDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twin.TwinAttachmentService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {
        MapperMode.AttachmentMode.class,
        MapperMode.AttachmentCollectionMode.class})
public class AttachmentViewRestDTOMapper extends RestSimpleDTOMapper<TwinAttachmentEntity, AttachmentViewDTOv1> {

    @MapperModePointerBinding(modes = MapperMode.AttachmentUserMode.class)
    private final UserRestDTOMapper userDTOMapper;

    @MapperModePointerBinding(modes = MapperMode.AttachmentTransitionMode.class)
    private final TransitionBaseV1RestDTOMapper transitionRestDTOMapper;

    private final TwinAttachmentService twinAttachmentService;

    @Override
    public void map(TwinAttachmentEntity src, AttachmentViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(MapperMode.AttachmentMode.DETAILED)) {
            case DETAILED:
                dst
                        .setAuthorUserId(src.getCreatedByUserId())
                        .setAuthorUser(userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(MapperMode.AttachmentUserMode.SHORT))))
                        .setTwinflowTransitionId(src.getTwinflowTransitionId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setTwinClassFieldId(src.getTwinClassFieldId())
                        .setCommentId(src.getTwinCommentId())
                        .setDescription(src.getDescription())
                        .setTitle(src.getTitle())
                        .setExternalId(src.getExternalId());
            case SHORT:
                dst
                        .setId(src.getId())
                        .setStorageLink(src.getStorageLink());
        }
        if (mapperContext.hasModeButNot(MapperMode.AttachmentTransitionMode.HIDE)) {
            dst
                    .setTwinflowTransitionId(src.getTwinflowTransitionId())
                    .setTwinflowTransition(transitionRestDTOMapper.convertOrPostpone(src.getTwinflowTransition(), mapperContext.forkOnPoint(MapperMode.AttachmentTransitionMode.SHORT)));
        }
    }

    @Override
    public List<AttachmentViewDTOv1> convertCollection(Collection<TwinAttachmentEntity> srcList, MapperContext mapperContext) throws Exception {
        Collection<TwinAttachmentEntity> newList = new ArrayList<>();
        switch (mapperContext.getModeOrUse(MapperMode.AttachmentCollectionMode.ALL)) {
            case DIRECT:
                newList = srcList.stream().filter(twinAttachmentService::checkOnDirect).collect(Collectors.toList());
                break;
            case FROM_TRANSITIONS:
                newList = srcList.stream().filter(el -> el.getTwinflowTransitionId() != null).collect(Collectors.toList());
                break;
            case FROM_COMMENTS:
                newList = srcList.stream().filter(el -> el.getTwinCommentId() != null).collect(Collectors.toList());
                break;
            case FROM_FIELDS:
                newList = srcList.stream().filter(el -> el.getTwinClassFieldId() != null).collect(Collectors.toList());
                break;
            case ALL:
                newList = srcList;
                break;
        }
        return super.convertCollection(newList, mapperContext);
    }

    @Override
    public String getObjectCacheId(TwinAttachmentEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(MapperMode.AttachmentMode.HIDE);
    }

}
