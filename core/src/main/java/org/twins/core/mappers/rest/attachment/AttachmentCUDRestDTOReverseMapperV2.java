package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.attachment.AttachmentCreateDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentCudDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.attachment.AttachmentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class AttachmentCUDRestDTOReverseMapperV2 extends RestSimpleDTOMapper<AttachmentCudDTOv1, EntityCUD<TwinAttachmentEntity>> {
    private final AttachmentService attachmentService;
    private final AttachmentUpdateRestDTOReverseMapper attachmentUpdateRestDTOReverseMapper;
    private final AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;

    @Override
    public void map(AttachmentCudDTOv1 src, EntityCUD<TwinAttachmentEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(attachmentUpdateRestDTOReverseMapper.convertCollection(src.getUpdate()))
                .setCreateList(attachmentCreateRestDTOReverseMapper.convertCollection(src.getCreate()))
                .setDeleteList(attachmentService.findEntitiesSafe(src.getDelete()).getList());
    }

    @SneakyThrows
    public void preProcessAttachments(AttachmentCudDTOv1 attachments, Map<String, MultipartFile> files) {
        List<AttachmentUpdateDTOv1> updateDTOv1List = attachments == null ? new ArrayList<>() : attachments.update;
        List<AttachmentCreateDTOv1> createDTOv1List = attachments == null ? new ArrayList<>() : attachments.create;
        attachmentUpdateRestDTOReverseMapper.preProcessAttachments(updateDTOv1List, files);
        attachmentCreateRestDTOReverseMapper.preProcessAttachments(createDTOv1List, files);
    }
}
