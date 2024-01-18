package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.attachment.AttachmentCudDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class AttachmentCUDRestDTOReverseMapperV2 extends RestSimpleDTOMapper<AttachmentCudDTOv1, EntityCUD<TwinAttachmentEntity>> {
    final AttachmentUpdateRestDTOReverseMapper attachmentUpdateRestDTOReverseMapper;
    final AttachmentAddRestDTOReverseMapper attachmentAddRestDTOReverseMapper;
    @Override
    public void map(AttachmentCudDTOv1 src, EntityCUD<TwinAttachmentEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(attachmentUpdateRestDTOReverseMapper.convertList(src.getUpdate()))
                .setCreateList(attachmentAddRestDTOReverseMapper.convertList(src.getCreate()))
                .setDeleteUUIDList(src.getDelete());
    }
}
