package org.twins.core.mappers.rest.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.comment.CommentUpdate;
import org.twins.core.dto.rest.comment.CommentUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class CommentUpdateRestDTOMapper extends RestSimpleDTOMapper<CommentUpdateDTOv1, CommentUpdate> {
    private final AttachmentCUDRestDTOReverseMapperV2 attachmentCUDRestDTOReverseMapperV2;

    @Override
    public void map(CommentUpdateDTOv1 src, CommentUpdate dst, MapperContext mapperContext) throws Exception {
         dst
                 .setId(src.getId())
                 .setTwinId(src.getTwinId())
                 .setComment(src.getText())
                 .setCudAttachments(attachmentCUDRestDTOReverseMapperV2.convert(src.getAttachments()));
    }
}
