package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentCreateDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class AttachmentCreateRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentCreateDTOv1, TwinAttachmentEntity> {

    protected final AttachmentSaveRestDTOReverseMapper attachmentSaveRestDTOReverseMapper;

    @Override
    public void map(AttachmentCreateDTOv1 src, TwinAttachmentEntity dst, MapperContext mapperContext) throws Exception {
        attachmentSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setTwinClassFieldId(src.getTwinClassFieldId())
                .setTwinCommentId(src.getCommentId())
        ;
    }

    @SneakyThrows
    public void preProcessAttachments(List<AttachmentCreateDTOv1> attachmentsRaw, Map<String, MultipartFile> files) {
        List<AttachmentCreateDTOv1> createDTOv1List = attachmentsRaw == null ? new ArrayList<>() : attachmentsRaw;
        List<AttachmentSaveDTOv1> attachments = createDTOv1List.stream().map(it -> (AttachmentSaveDTOv1) it).toList();
        attachmentSaveRestDTOReverseMapper.preProcessAttachments(attachments, files);
    }
}
