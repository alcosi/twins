package org.twins.core.mappers.rest.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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
public class CommentViewRestDTOMapper extends RestSimpleDTOMapper<TwinCommentEntity, CommentViewDTOv1> {
    final UserRestDTOMapper userDTOMapper;
    final AttachmentViewRestDTOMapper attachmentRestDTOMapper;
    final CommentService commentService;

    @Override
    public void map(TwinCommentEntity src, CommentViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(Mode.SHORT)) {
            case SHORT:
                dst
                        .setId(src.getId())
                        .setText(src.getText());
                break;
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setAuthorUserId(src.getCreatedByUserId())
                        .setAuthorUser(userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext))
                        .setCreatedAt(src.getCreatedAt())
                        .setChangedAt(src.getChangedAt())
                        .setText(src.getText());
                if (!attachmentRestDTOMapper.hideMode(mapperContext)) {
                    dst.setAttachments(attachmentRestDTOMapper.convertList(commentService.loadAttachments(src).getList(), mapperContext));
                }
                break;
        }
    }

    @Override
    public void beforeListConversion(Collection<TwinCommentEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeListConversion(srcCollection, mapperContext);
        if (!attachmentRestDTOMapper.hideMode(mapperContext))
            commentService.loadAttachments(srcCollection);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(Mode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinCommentEntity src) {
        return src.getId().toString();
    }

    @AllArgsConstructor
    public enum Mode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }
}
