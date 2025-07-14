package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentSaveDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class AttachmentUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentUpdateDTOv1, TwinAttachmentEntity> {

    private final AttachmentSaveRestDTOReverseMapper attachmentSaveRestDTOReverseMapper;

    @Override
    public void map(AttachmentUpdateDTOv1 src, TwinAttachmentEntity dst, MapperContext mapperContext) throws Exception {
        attachmentSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }

    @SneakyThrows
    public void preProcessAttachments(List<AttachmentUpdateDTOv1> attachmentsRaw, Map<String, MultipartFile> files) {
        List<AttachmentUpdateDTOv1> createDTOv1List = attachmentsRaw == null ? new ArrayList<>() : attachmentsRaw;
        List<AttachmentSaveDTOv1> attachments = createDTOv1List.stream().map(it -> (AttachmentSaveDTOv1) it).toList();
        attachmentSaveRestDTOReverseMapper.preProcessAttachments(attachments, files);
    }
}
