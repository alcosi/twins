package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentAddDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentBaseDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;
import org.twins.core.service.twin.TwinService;


@Component
@RequiredArgsConstructor
public class AttachmentBaseRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentBaseDTOv1, TwinAttachmentEntity> {

    @Override
    public void map(AttachmentBaseDTOv1 src, TwinAttachmentEntity dst, MapperProperties mapperProperties) throws Exception {
        dst
                .setStorageLink(src.getStorageLink())
                .setTitle(src.getTitle())
                .setDescription(src.getDescription())
                .setExternalId(src.getExternalId());
    }
}
