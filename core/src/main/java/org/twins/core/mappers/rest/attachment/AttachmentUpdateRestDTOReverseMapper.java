package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentAddDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentUpdateDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class AttachmentUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentUpdateDTOv1, TwinAttachmentEntity> {
    final AttachmentBaseRestDTOReverseMapper attachmentBaseRestDTOReverseMapper;

    @Override
    public void map(AttachmentUpdateDTOv1 src, TwinAttachmentEntity dst, MapperProperties mapperProperties) throws Exception {
        attachmentBaseRestDTOReverseMapper.map(src, dst, mapperProperties);
        dst.setId(src.getId());
    }
}
