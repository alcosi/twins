package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentDTOv1;
import org.twins.core.mappers.rest.comment.CommentRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.*;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.attachment.AttachmentActionService;
import org.twins.core.service.attachment.AttachmentService;

import java.util.*;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {
        AttachmentMode.class,
        AttachmentCollectionMode.class,
        TwinAttachmentActionMode.class
})
public class AttachmentRestDTOMapper extends RestSimpleDTOMapper<TwinAttachmentEntity, AttachmentDTOv1> {

    private final AttachmentService attachmentService;
    private final AttachmentActionService attachmentActionService;

    @MapperModePointerBinding(modes = UserMode.Attachment2UserMode.class)
    private final UserRestDTOMapper userDTOMapper;

    @MapperModePointerBinding(modes = TransitionMode.Attachment2TransitionMode.class)
    private final TransitionBaseV1RestDTOMapper transitionRestDTOMapper;

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = CommentMode.Attachment2CommentModeMode.class)
    private final CommentRestDTOMapper commentRestDTOMapper;

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = TwinMode.Attachment2TwinMode.class)
    private TwinRestDTOMapper twinRestDTOMapper;

    @Override
    public void map(TwinAttachmentEntity src, AttachmentDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(AttachmentMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setAuthorUserId(src.getCreatedByUserId())
                        .setTwinflowTransitionId(src.getTwinflowTransitionId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setTwinClassFieldId(src.getTwinClassFieldId())
                        .setCommentId(src.getTwinCommentId())
                        .setDescription(src.getDescription())
                        .setTitle(src.getTitle())
                        .setExternalId(src.getExternalId())
                        .setStorageLink(src.getStorageLink());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setStorageLink(src.getStorageLink())
                        .setStorageLinksMap(src.getModificationLinks());
        }
        if (mapperContext.hasModeButNot(CommentMode.Attachment2CommentModeMode.HIDE)) {
            dst.setCommentId(src.getTwinCommentId());
            commentRestDTOMapper.postpone(src.getComment(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(CommentMode.Attachment2CommentModeMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(TransitionMode.Attachment2TransitionMode.HIDE)) {
            dst.setTwinflowTransitionId(src.getTwinflowTransitionId());
            transitionRestDTOMapper.postpone(src.getTwinflowTransition(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TransitionMode.Attachment2TransitionMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(UserMode.Attachment2UserMode.HIDE)) {
            dst.setAuthorUserId(src.getCreatedByUserId());
            userDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Attachment2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(TwinMode.Attachment2TwinMode.HIDE)) {
            dst.setTwinId(src.getTwinId());
            twinRestDTOMapper.postpone(src.getTwin(), mapperContext.forkOnPoint(TwinMode.Attachment2TwinMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinAttachmentActionMode.HIDE)) {
            attachmentActionService.loadAttachmentActions(src);
            dst.setAttachmentActions(src.getAttachmentActions());
        }
    }

    @Override
    public List<AttachmentDTOv1> convertCollection(Collection<TwinAttachmentEntity> srcList, MapperContext mapperContext) throws Exception {
        Collection<TwinAttachmentEntity> newList = new ArrayList<>();
        switch (mapperContext.getModeOrUse(AttachmentCollectionMode.ALL)) {
            case DIRECT ->
                newList = srcList.stream().filter(attachmentService::checkOnDirect).collect(Collectors.toList());
            case FROM_TRANSITIONS ->
                newList = srcList.stream().filter(el -> el.getTwinflowTransitionId() != null).collect(Collectors.toList());
            case FROM_COMMENTS ->
                newList = srcList.stream().filter(el -> el.getTwinCommentId() != null).collect(Collectors.toList());
            case FROM_FIELDS ->
                newList = srcList.stream().filter(el -> el.getTwinClassFieldId() != null).collect(Collectors.toList());
            case ALL ->
                newList = srcList;
        }
        return super.convertCollection(newList, mapperContext);
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinAttachmentEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinAttachmentActionMode.HIDE))
            attachmentActionService.loadAttachmentActions(srcCollection);
    }

    @Override
    public String getObjectCacheId(TwinAttachmentEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(AttachmentMode.HIDE);
    }

}
