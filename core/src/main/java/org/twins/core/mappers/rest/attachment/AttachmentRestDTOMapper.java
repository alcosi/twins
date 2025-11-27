package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentModificationEntity;
import org.twins.core.dto.rest.attachment.AttachmentDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.comment.CommentRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.attachment.AttachmentActionService;
import org.twins.core.service.attachment.AttachmentService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {
        AttachmentMode.class,
        AttachmentModificationMode.class,
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
    private TwinRestDTOMapperV2 twinRestDTOMapper;

    @Lazy
    @Autowired
    private TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Autowired
    @MapperModePointerBinding(modes = PermissionMode.Attachment2PermissionMode.class)
    private PermissionRestDTOMapper permissionRestDTOMapper;

    @Override
    public void map(TwinAttachmentEntity src, AttachmentDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(AttachmentMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setAuthorUserId(src.getCreatedByUserId())
                    .setTwinflowTransitionId(src.getTwinflowTransitionId())
                    .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                    .setTwinClassFieldId(src.getTwinClassFieldId())
                    .setCommentId(src.getTwinCommentId())
                    .setViewPermissionId(src.getViewPermissionId())
                    .setDescription(src.getDescription())
                    .setSize(src.getSize())
                    .setTitle(src.getTitle())
                    .setExternalId(src.getExternalId())
                    .setStorageLink(attachmentService.getAttachmentUri(src))
                    .setOrder(src.getOrder());
            case SHORT -> dst
                    .setId(src.getId())
                    .setStorageLink(attachmentService.getAttachmentUri(src));
        }
        if (mapperContext.hasModeButNot(CommentMode.Attachment2CommentModeMode.HIDE)) {
            dst.setCommentId(src.getTwinCommentId());
            commentRestDTOMapper.postpone(src.getComment(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(CommentMode.Attachment2CommentModeMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(AttachmentModificationMode.HIDE)) {
            attachmentService.loadAttachmentModifications(src);
            dst.setModifications(new HashMap<>());
            if (CollectionUtils.isNotEmpty(src.getModifications()))
                for (TwinAttachmentModificationEntity mod : src.getModifications())
                    dst.getModifications().put(mod.getModificationType(), mod.getStorageFileKey());
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
        if (mapperContext.hasModeButNot(TwinClassFieldMode.Attachment2TwinClassFieldMode.HIDE)) {
            dst.setTwinClassFieldId(src.getTwinClassFieldId());
            twinClassFieldRestDTOMapper.postpone(src.getTwinClassField(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassFieldMode.Attachment2TwinClassFieldMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(PermissionMode.Attachment2PermissionMode.HIDE)) {
            dst.setViewPermissionId(src.getViewPermissionId());
            permissionRestDTOMapper.postpone(src.getViewPermission(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(PermissionMode.Attachment2PermissionMode.SHORT)));
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
            case ALL -> newList = srcList;
        }
        return super.convertCollection(newList, mapperContext);
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinAttachmentEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinAttachmentActionMode.HIDE))
            attachmentActionService.loadAttachmentActions(srcCollection);
        if (mapperContext.hasModeButNot(AttachmentModificationMode.HIDE))
            attachmentService.loadAttachmentModifications(srcCollection);
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
