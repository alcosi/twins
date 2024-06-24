package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentViewDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twin.TwinAttachmentService;
import org.twins.core.service.twin.TwinService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class AttachmentViewRestDTOMapper extends RestSimpleDTOMapper<TwinAttachmentEntity, AttachmentViewDTOv1> {
    final UserRestDTOMapper userDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final TransitionBaseV1RestDTOMapper transitionRestDTOMapper;
    @Autowired
    TwinClassRestDTOMapper twinClassRestDTOMapper;
    final TwinFieldRestDTOMapper twinFieldRestDTOMapper;
    final TwinService twinService;
    final TwinAttachmentService twinAttachmentService;

    @Override
    public void map(TwinAttachmentEntity src, AttachmentViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(MapperMode.AttachmentMode.DETAILED)) {
            case DETAILED:
                dst
                        .setAuthorUserId(src.getCreatedByUserId())
                        .setAuthorUser(userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.cloneWithIsolatedModes().setMode(UserRestDTOMapper.Mode.SHORT)))
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
        if (!mapperContext.hasModeOrEmpty(MapperMode.AttachmentMode.HIDE)) {
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
