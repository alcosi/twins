package org.twins.core.mappers.rest.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinCommentEntity;
import org.twins.core.dto.rest.comment.CommentViewDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.comment.CommentService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = MapperMode.CommentMode.class)
public class CommentViewRestDTOMapper extends RestSimpleDTOMapper<TwinCommentEntity, CommentViewDTOv1> {
    @MapperModePointerBinding(modes = MapperMode.CommentUserMode.class)
    final UserRestDTOMapper userRestDTOMapper;
    @MapperModePointerBinding(modes = MapperMode.CommentAttachmentMode.class)
    final AttachmentViewRestDTOMapper attachmentRestDTOMapper;
    final CommentService commentService;

    @Override
    public void map(TwinCommentEntity src, CommentViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(MapperMode.CommentMode.SHORT)) {
            case SHORT:
                dst
                        .setId(src.getId())
                        .setText(src.getText());
                break;
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setAuthorUserId(src.getCreatedByUserId())
                        .setAuthorUser(userRestDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext))
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime() != null ? src.getCreatedAt().toLocalDateTime() : null)
                        .setText(src.getText());
                break;
        }
        if (mapperContext.hasModeButNot(MapperMode.CommentUserMode.HIDE))
            dst
                    .setAuthorUser(userRestDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext
                            .forkOnPoint(MapperMode.CommentUserMode.SHORT)));
        if (mapperContext.hasModeButNot(MapperMode.CommentAttachmentMode.HIDE))
            dst
                    .setAttachments(attachmentRestDTOMapper.convertCollection(commentService.loadAttachments(src).getCollection(), mapperContext
                            .forkOnPoint(MapperMode.CommentAttachmentMode.SHORT).setMode(MapperMode.AttachmentCollectionMode.FROM_COMMENTS)));
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinCommentEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(MapperMode.CommentAttachmentMode.HIDE))
            commentService.loadAttachments(srcCollection);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(MapperMode.CommentMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinCommentEntity src) {
        return src.getId().toString();
    }

}
