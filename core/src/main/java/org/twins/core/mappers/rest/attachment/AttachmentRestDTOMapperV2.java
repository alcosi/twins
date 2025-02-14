package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapper;


@Component
@RequiredArgsConstructor
public class AttachmentRestDTOMapperV2 extends RestSimpleDTOMapper<TwinAttachmentEntity, AttachmentDTOv2> {
    private final AttachmentRestDTOMapper attachmentRestDTOMapper;
    @MapperModePointerBinding(modes = TwinMode.Attachment2TwinMode.class)
    private final TwinRestDTOMapper twinRestDTOMapper;

    @Override
    public void map(TwinAttachmentEntity src, AttachmentDTOv2 dst, MapperContext mapperContext) throws Exception {
        attachmentRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(TwinMode.Attachment2TwinMode.HIDE))
            dst
                    .setTwin(twinRestDTOMapper.convertOrPostpone(src.getTwin(), mapperContext.forkOnPoint(TwinMode.Attachment2TwinMode.SHORT)))
                    .setTwinId(src.getTwinId());
    }
}
