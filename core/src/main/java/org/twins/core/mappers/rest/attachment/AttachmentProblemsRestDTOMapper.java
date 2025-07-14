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
public class AttachmentProblemsRestDTOMapper extends RestSimpleDTOMapper<AttachmentCUDProblems, AttachmentCUDProblemsDTOv1> {

    private final AttachmentCreateProblemsRestDTOMapper attachmentCreateProblemsRestDTOMapper;
    private final AttachmentUpdateProblemsRestDTOMapper attachmentUpdateProblemsRestDTOMapper;
    private final AttachmentDeleteProblemsRestDTOMapper attachmentDeleteProblemsRestDTOMapper;
    private final AttachmentFieldProblemsRestDTOMapper attachmentFieldProblemsRestDTOMapper;
    private final AttachmentCommentProblemsRestDTOMapper attachmentCommentProblemsRestDTOMapper;

    @Override
    public void map(AttachmentCUDProblems src, AttachmentCUDProblemsDTOv1 dst, MapperContext mapperContext) throws Exception {
            if (mapperContext.hasModeButNot(AttachmentValidateProblemsMode.HIDE)) {
                dst
                        .setUpdateProblems(attachmentUpdateProblemsRestDTOMapper.convertCollection(src.getUpdateProblems()))
                        .setDeleteProblems(attachmentDeleteProblemsRestDTOMapper.convertCollection(src.getDeleteProblems()))
                        .setFieldAttachmentProblems(attachmentFieldProblemsRestDTOMapper.convertCollection(src.getFieldAttachmentProblems()))
                        .setCommentAttachmentProblems(attachmentCommentProblemsRestDTOMapper.convertCollection(src.getCommentAttachmentProblems()))
                        .setCreateProblems(attachmentCreateProblemsRestDTOMapper.convertCollection(src.getCreateProblems()));
            }
    }



}
