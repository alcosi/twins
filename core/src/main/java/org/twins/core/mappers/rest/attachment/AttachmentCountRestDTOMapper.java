package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.attachment.AttachmentCountDTOv1;
import org.twins.core.enums.sort.AttachmentGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.attachment.AttachmentService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class AttachmentCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinAttachmentEntity, AttachmentGroupField>, AttachmentCountDTOv1> {
    private final AttachmentService attachmentService;

    @MapperModePointerBinding(modes = TwinMode.Attachment2TwinMode.class)
    private final TwinRestDTOMapperV2 twinRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.Attachment2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = TransitionMode.Attachment2TransitionMode.class)
    private final TransitionBaseV1RestDTOMapper transitionRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.Attachment2PermissionMode.class)
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassFieldMode.Attachment2TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Override
    public void map(CountResult<TwinAttachmentEntity, AttachmentGroupField> src, AttachmentCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setTwinId(entity.getTwinId())
                .setTwinflowTransitionId(entity.getTwinflowTransitionId())
                .setViewPermissionId(entity.getViewPermissionId())
                .setCreatedByUserId(entity.getCreatedByUserId())
                .setTwinCommentId(entity.getTwinCommentId())
                .setTwinClassFieldId(entity.getTwinClassFieldId())
                .setStorageId(entity.getStorageId())
                .setCount(src.getCount());
        if (needLoad(mapperContext, TwinMode.Attachment2TwinMode.HIDE, src, AttachmentGroupField.twinId)) {
            attachmentService.loadTwin(entity);
            twinRestDTOMapper.postpone(entity.getTwin(), mapperContext.forkOnPoint(TwinMode.Attachment2TwinMode.SHORT));
        }
        if (needLoad(mapperContext, UserMode.Attachment2UserMode.HIDE, src, AttachmentGroupField.createdByUserId)) {
            attachmentService.loadCreatedByUser(entity);
            userRestDTOMapper.postpone(entity.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Attachment2UserMode.SHORT)));
        }
        if (needLoad(mapperContext, TransitionMode.Attachment2TransitionMode.HIDE, src, AttachmentGroupField.twinflowTransitionId)) {
            attachmentService.loadTwinflowTransition(entity);
            transitionRestDTOMapper.postpone(entity.getTwinflowTransition(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TransitionMode.Attachment2TransitionMode.SHORT)));
        }
        if (needLoad(mapperContext, PermissionMode.Attachment2PermissionMode.HIDE, src, AttachmentGroupField.viewPermissionId)) {
            attachmentService.loadViewPermission(entity);
            permissionRestDTOMapper.postpone(entity.getViewPermission(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(PermissionMode.Attachment2PermissionMode.SHORT)));
        }
        if (needLoad(mapperContext, TwinClassFieldMode.Attachment2TwinClassFieldMode.HIDE, src, AttachmentGroupField.twinClassFieldId)) {
            attachmentService.loadTwinClassField(entity);
            twinClassFieldRestDTOMapper.postpone(entity.getTwinClassField(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassFieldMode.Attachment2TwinClassFieldMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinAttachmentEntity, AttachmentGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        var entities = srcCollection.stream().map(CountResult::getEntity).toList();
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, TwinMode.Attachment2TwinMode.HIDE, someCount, AttachmentGroupField.twinId)) {
            attachmentService.loadTwin(entities);
        }
        if (needLoad(mapperContext, UserMode.Attachment2UserMode.HIDE, someCount, AttachmentGroupField.createdByUserId)) {
            attachmentService.loadCreatedByUser(entities);
        }
        if (needLoad(mapperContext, TransitionMode.Attachment2TransitionMode.HIDE, someCount, AttachmentGroupField.twinflowTransitionId)) {
            attachmentService.loadTwinflowTransition(entities);
        }
        if (needLoad(mapperContext, PermissionMode.Attachment2PermissionMode.HIDE, someCount, AttachmentGroupField.viewPermissionId)) {
            attachmentService.loadViewPermission(entities);
        }
        if (needLoad(mapperContext, TwinClassFieldMode.Attachment2TwinClassFieldMode.HIDE, someCount, AttachmentGroupField.twinClassFieldId)) {
            attachmentService.loadTwinClassField(entities);
        }
    }
}
