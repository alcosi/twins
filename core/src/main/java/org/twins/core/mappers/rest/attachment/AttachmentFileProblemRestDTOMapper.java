package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.attachment.AttachmentCUDProblems;
import org.twins.core.dto.rest.attachment.AttachmentCUDProblemsDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.AttachmentValidateProblemsMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {AttachmentValidateProblemsMode.class})
public class AttachmentFileProblemRestDTOMapper extends RestSimpleDTOMapper<AttachmentCUDProblems, AttachmentCUDProblemsDTOv1> {




    @Override
    public void map(AttachmentCUDProblems src, AttachmentCUDProblemsDTOv1 dst, MapperContext mapperContext) throws Exception {
            if (mapperContext.hasModeButNot(AttachmentValidateProblemsMode.HIDE)) {
                dst
                        .setCreateProblems(src.getCreateProblems())
                        .setUpdateProblems(src.getUpdateProblems())
                        .setDeleteProblems(src.getDeleteProblems())
                        .setFieldAttachmentProblems(src.getFieldAttachmentProblems())
                        .setCommentAttachmentProblems(src.getCommentAttachmentProblems());
            }
    }

}
