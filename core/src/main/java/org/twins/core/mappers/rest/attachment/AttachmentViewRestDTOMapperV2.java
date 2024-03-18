package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentViewDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinAttachmentMapper;
import org.twins.core.service.twin.TwinAttachmentService;

import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class AttachmentViewRestDTOMapperV2 extends RestSimpleDTOMapper<TwinAttachmentEntity, AttachmentViewDTOv2> {
    final AttachmentViewRestDTOMapper attachmentRestDTOMapper;
    final TwinAttachmentService twinAttachmentService;

    @Override
    public void map(TwinAttachmentEntity src, AttachmentViewDTOv2 dst, MapperContext mapperContext) throws Exception {
        attachmentRestDTOMapper.map(src, dst, mapperContext);
        dst.setTwinId(src.getTwinId());
    }

    public List<TwinAttachmentEntity> filterAttachment(List<TwinAttachmentEntity> srcCollection, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(TwinAttachmentMapper.Mode.ALL)) {
            case DIRECT:
                return srcCollection.stream().filter(twinAttachmentService::checkOnDirect).collect(Collectors.toList());
            case FROM_TRANSITIONS:
                return srcCollection.stream().filter(el -> el.getTwinflowTransitionId() != null).collect(Collectors.toList());
            case FROM_COMMENTS:
                return srcCollection.stream().filter(el -> el.getTwinCommentId() != null).collect(Collectors.toList());
            case FROM_FIELDS:
                return srcCollection.stream().filter(el -> el.getTwinClassFieldId() != null).collect(Collectors.toList());
        }
        return srcCollection;
    }
}
