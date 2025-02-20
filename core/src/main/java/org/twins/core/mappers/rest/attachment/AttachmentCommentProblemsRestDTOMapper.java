package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.attachment.AttachmentCommentProblem;
import org.twins.core.dto.rest.attachment.TwinCommentAttachmentProblemsDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.AttachmentValidateProblemsMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {AttachmentValidateProblemsMode.class})
public class AttachmentCommentProblemsRestDTOMapper extends RestSimpleDTOMapper<AttachmentCommentProblem, TwinCommentAttachmentProblemsDTOv1> {

    @Override
    public void map(AttachmentCommentProblem src, TwinCommentAttachmentProblemsDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setProblem(src.getProblem())
                .setCommentId(src.getCommentId())
                .setMessage(src.getMessage());
    }
}
