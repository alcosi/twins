package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentAddDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;
import org.twins.core.service.twin.TwinService;


@Component
@RequiredArgsConstructor
public class AttachmentAddRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentAddDTOv1, TwinAttachmentEntity> {
    final AttachmentBaseRestDTOReverseMapper attachmentBaseRestDTOReverseMapper;
    @Override
    public void map(AttachmentAddDTOv1 src, TwinAttachmentEntity dst, MapperProperties mapperProperties) throws Exception {
        attachmentBaseRestDTOReverseMapper.map(src, dst, mapperProperties);
    }
}
