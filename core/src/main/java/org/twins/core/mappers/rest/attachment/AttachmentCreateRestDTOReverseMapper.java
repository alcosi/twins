package org.twins.core.mappers.rest.attachment;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.file.FileData;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentModificationEntity;
import org.twins.core.dto.rest.attachment.AttachmentCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.List;
import java.util.Map;

import static org.twins.core.exception.ErrorCodeTwins.BAD_REQUEST_MULTIPART_FILE_IS_NOT_PRESENTED;


@Component
@Slf4j
public class AttachmentCreateRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentCreateDTOv1, TwinAttachmentEntity> {

    @SneakyThrows
    public void preProcessAttachments(List<AttachmentCreateDTOv1> attachments, Map<String, MultipartFile> files) {
        try {
            if (CollectionUtils.isEmpty(attachments))
                return;
            attachments.forEach(att -> {
                processAttribute(files, att);
            });
        } catch (Throwable t) {
            log.error("Error while pre-processing attachments", t);
            throw t;
        }
    }

    @SneakyThrows
    protected void processAttribute(Map<String, MultipartFile> files, AttachmentCreateDTOv1 att) {
        boolean haveLink = att.storageLink != null && !att.storageLink.isBlank();
        boolean isMultipart = haveLink && att.storageLink.toLowerCase().startsWith("multipart://");
        att.setExternalLink(!isMultipart);
        if (haveLink && isMultipart) {
            String fileKey = att.storageLink.replaceFirst("multipart://", "");
            MultipartFile file = files.get(fileKey);
            if (file == null) {
                throw new ServiceException(BAD_REQUEST_MULTIPART_FILE_IS_NOT_PRESENTED, "File not found " + fileKey);
            }
            try {
                FileData domainFile = new FileData(file.getInputStream(), file.getOriginalFilename(), file.getSize());
                att.setDomainFile(domainFile);
                att.fileChanged = true;
                if (att.size == null || att.size < 1) {
                    att.size = domainFile.fileSize();
                }
            } catch (Throwable t) {
                log.error("Error while processing multipart link {}", att.storageLink, t);
                throw new ServiceException(BAD_REQUEST_MULTIPART_FILE_IS_NOT_PRESENTED, "Error while processing multipart link " + att.storageLink + " ." + t.getClass().getSimpleName());
            }
        }
    }

    @Override
    public void map(AttachmentCreateDTOv1 src, TwinAttachmentEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinId(src.getTwinId())
                .setStorageFileKey(src.getStorageLink())
                .setAttachmentFile(src.domainFile)
                .setFileChanged(src.fileChanged)
                .setModifications(new Kit<>(TwinAttachmentModificationEntity::getModificationType))
                .setTitle(src.getTitle())
                //TODO set size as is.
                .setSize(src.getSize() == null ? 0 : src.getSize())
                .setDescription(src.getDescription())
                .setExternalId(src.getExternalId())
                .setOrder(src.getOrder());
        if (null != src.getModifications()) {
            for (Map.Entry<String, String> mod : src.getModifications().entrySet()) {
                TwinAttachmentModificationEntity modEntity = new TwinAttachmentModificationEntity();
                modEntity
                        .setModificationType(mod.getKey())
                        .setStorageFileKey(mod.getValue());
                dst.getModifications().add(modEntity);
            }
        }
        dst
                .setTwinClassFieldId(src.getTwinClassFieldId())
                .setTwinCommentId(src.getCommentId())
        ;
    }
}
