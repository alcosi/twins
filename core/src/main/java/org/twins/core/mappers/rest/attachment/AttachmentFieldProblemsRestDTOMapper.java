package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.attachment.AttachmentFieldProblem;
import org.twins.core.dto.rest.attachment.TwinFieldAttachmentProblemsDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.AttachmentValidateProblemsMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {AttachmentValidateProblemsMode.class})
public class AttachmentFieldProblemsRestDTOMapper extends RestSimpleDTOMapper<AttachmentFieldProblem, TwinFieldAttachmentProblemsDTOv1> {

    @Override
    public void map(AttachmentFieldProblem src, TwinFieldAttachmentProblemsDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setProblem(src.getProblem())
                .setTwinFieldId(src.getTwinFieldId())
                .setMessage(src.getMessage());
    }
}
