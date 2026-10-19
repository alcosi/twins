package org.twins.core.mappers.rest.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.comment.CommentCountDTOv1;
import org.twins.core.enums.sort.CommentGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.CommentMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.comment.CommentService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {CommentMode.class})
public class CommentCountRestDTOMapper extends RestSimpleDTOMapper<
        CountResult<TwinCommentEntity, CommentGroupField>, CommentCountDTOv1> {

    @MapperModePointerBinding(modes = UserMode.Comment2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = TwinMode.Comment2TwinMode.class)
    private final TwinRestDTOMapperV2 twinRestDTOMapper;

    private final CommentService commentService;

    @Override
    public void map(CountResult<TwinCommentEntity, CommentGroupField> src, CommentCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        TwinCommentEntity entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setTwinId(entity.getTwinId())
                .setCreatedByUserId(entity.getCreatedByUserId())
                .setCount(src.getCount());

        if (needLoad(mapperContext, UserMode.Comment2UserMode.HIDE, src, CommentGroupField.createdByUserId)) {
            commentService.loadUser(entity);
            userRestDTOMapper.postpone(entity.getCreatedByUser(), mapperContext.forkOnPoint(UserMode.Comment2UserMode.SHORT));
        }
        if (needLoad(mapperContext, TwinMode.Comment2TwinMode.HIDE, src, CommentGroupField.twinId)) {
            commentService.loadTwin(entity);
            twinRestDTOMapper.postpone(entity.getTwin(), mapperContext.forkOnPoint(TwinMode.Comment2TwinMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinCommentEntity, CommentGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        var entities = srcCollection.stream().map(CountResult::getEntity).toList();
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, UserMode.Comment2UserMode.HIDE, someCount, CommentGroupField.createdByUserId))
            commentService.loadUser(entities);
        if (needLoad(mapperContext, TwinMode.Comment2TwinMode.HIDE, someCount, CommentGroupField.twinId))
            commentService.loadTwin(entities);
    }
}
