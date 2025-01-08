package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.attachment.AttachmentCUDValidateResult;
import org.twins.core.dto.rest.attachment.AttachmentCUDValidateRsDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.AttachmentMode;

@Component
@RequiredArgsConstructor
public class AttachmentCUDValidateRestDTOMapper extends RestSimpleDTOMapper<AttachmentCUDValidateResult, AttachmentCUDValidateRsDTOv1> {


    @MapperModePointerBinding(modes = AttachmentMode.AttachmentCUDValidate2AttachmentMode.class)
    private final AttachmentViewRestDTOMapperV2 attachmentViewRestDTOMapperV2;

    private final AttachmentProblemRestDTOMapper attachmentProblemRestDTOMapper;


    @Override
    public void map(AttachmentCUDValidateResult src, AttachmentCUDValidateRsDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setAttachmentsForUD(attachmentViewRestDTOMapperV2.convertCollection(src.getAttachmentsForUD()))
                .setCudProblems(attachmentProblemRestDTOMapper.convert(src.getCudProblems(), mapperContext));
    }
}
