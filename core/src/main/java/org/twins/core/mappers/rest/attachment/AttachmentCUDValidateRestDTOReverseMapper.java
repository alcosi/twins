package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.attachment.AttachmentCUDValidateRqDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentCreateDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentCudDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.attachment.AttachmentService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AttachmentCUDValidateRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentCUDValidateRqDTOv1, EntityCUD<TwinAttachmentEntity>> {
    private final AttachmentService attachmentService;
    private final AttachmentUpdateRestDTOReverseMapper attachmentUpdateRestDTOReverseMapper;
    private final AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;

    @SneakyThrows
    public void preProcessAttachments(AttachmentCudDTOv1 attachments, Map<String, MultipartFile> files) {
        List<AttachmentUpdateDTOv1> updateDTOv1List = attachments == null ? Collections.emptyList() : attachments.update;
        List<AttachmentCreateDTOv1> createDTOv1List = attachments == null ? Collections.emptyList() : attachments.create;
        attachmentUpdateRestDTOReverseMapper.preProcessAttachments(updateDTOv1List, files);
        attachmentCreateRestDTOReverseMapper.preProcessAttachments(createDTOv1List, files);
    }

    @Override
    public void map(AttachmentCUDValidateRqDTOv1 src, EntityCUD<TwinAttachmentEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(attachmentUpdateRestDTOReverseMapper.convertCollection(src.getAttachments().getUpdate()))
                .setCreateList(attachmentCreateRestDTOReverseMapper.convertCollection(src.getAttachments().getCreate()))
                .setDeleteList(attachmentService.findEntitiesSafe(src.getAttachments().getDelete()).getList());
    }
}
