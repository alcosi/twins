package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentViewDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class AttachmentViewRestDTOMapperV2 extends RestSimpleDTOMapper<TwinAttachmentEntity, AttachmentViewDTOv2> {

    private final AttachmentViewRestDTOMapper attachmentRestDTOMapper;

    @Override
    public void map(TwinAttachmentEntity src, AttachmentViewDTOv2 dst, MapperContext mapperContext) throws Exception {
        attachmentRestDTOMapper.map(src, dst, mapperContext);
        dst.setTwinId(src.getTwinId());
    }
}
