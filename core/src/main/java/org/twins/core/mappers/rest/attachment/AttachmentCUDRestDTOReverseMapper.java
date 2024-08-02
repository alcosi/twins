package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.twin.TwinUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.attachment.AttachmentService;


@Component
@RequiredArgsConstructor
public class AttachmentCUDRestDTOReverseMapper extends RestSimpleDTOMapper<TwinUpdateDTOv1, EntityCUD<TwinAttachmentEntity>> {
    private final AttachmentService attachmentService;
    private final AttachmentUpdateRestDTOReverseMapper attachmentUpdateRestDTOReverseMapper;
    private final AttachmentAddRestDTOReverseMapper attachmentAddRestDTOReverseMapper;

    @Override
    public void map(TwinUpdateDTOv1 src, EntityCUD<TwinAttachmentEntity> dst, MapperContext mapperContext) throws Exception {
        if (null == src.getAttachments())
            return;
        src.getAttachments().getCreate().forEach(ta -> ta.setTwinId(src.getTwinId()));
        src.getAttachments().getUpdate().forEach(ta -> ta.setTwinId(src.getTwinId()));
        dst
                .setUpdateList(attachmentUpdateRestDTOReverseMapper.convertCollection(src.getAttachments().getUpdate()))
                .setCreateList(attachmentAddRestDTOReverseMapper.convertCollection(src.getAttachments().getCreate()))
                .setDeleteList(attachmentService.findEntitiesSafe(src.getAttachments().getDelete()).getList());
    }
}
