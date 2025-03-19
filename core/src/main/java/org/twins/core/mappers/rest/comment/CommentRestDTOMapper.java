package org.twins.core.mappers.rest.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dto.rest.comment.CommentDTOv1;
import org.twins.core.mappers.rest.mappercontext.*;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.comment.CommentService;
import org.twins.core.service.comment.CommentActionService;

import java.util.Collection;

import static org.cambium.common.util.DateUtils.convertOrNull;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {CommentMode.class, CommentActionMode.class})
public class CommentRestDTOMapper extends RestSimpleDTOMapper<TwinCommentEntity, CommentDTOv1> {

    @MapperModePointerBinding(modes = UserMode.Comment2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = AttachmentMode.Comment2AttachmentMode.class)
    private final AttachmentRestDTOMapper attachmentRestDTOMapper;

    private final CommentService commentService;
    private final CommentActionService commentActionService;

    @Override
    public void map(TwinCommentEntity src, CommentDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(CommentMode.SHORT)) {
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setText(src.getText());
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setText(src.getText())
                        .setAuthorUserId(src.getCreatedByUserId())
                        .setCreatedAt(convertOrNull(src.getCreatedAt()))
                        .setChangedAt(convertOrNull(src.getChangedAt()));
        }
        if (mapperContext.hasModeButNot(UserMode.Comment2UserMode.HIDE)) {
            dst.setAuthorUserId(src.getCreatedByUserId());
            userRestDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(UserMode.Comment2UserMode.SHORT));
        }
        if (mapperContext.hasModeButNot(AttachmentMode.Comment2AttachmentMode.HIDE)) {
            commentService.loadAttachments(src);
            dst.setAttachmentIds(attachmentRestDTOMapper.postpone(
                    src.getAttachmentKit(),
                    mapperContext.forkOnPoint(AttachmentMode.Comment2AttachmentMode.SHORT).setMode(AttachmentCollectionMode.FROM_COMMENTS))
            );
        }
        if (mapperContext.hasModeButNot(CommentActionMode.HIDE)) {
            commentActionService.loadCommentActions(src);
            dst.setCommentActions(src.getCommentActions());
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinCommentEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(AttachmentMode.Comment2AttachmentMode.HIDE))
            commentService.loadAttachments(srcCollection);
        if (mapperContext.hasModeButNot(CommentActionMode.HIDE))
            commentActionService.loadCommentActions(srcCollection);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(CommentMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinCommentEntity src) {
        return src.getId().toString();
    }

}
