package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentModificationEntity;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.file.DomainFile;
import org.twins.core.dto.rest.attachment.AttachmentSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.storage.StorageService;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@Slf4j
public class AttachmentSaveRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentSaveDTOv1, TwinAttachmentEntity> {
    protected final StorageService storageService;
    protected final AuthService authService;
    protected final UUID externalLinkStorageId = UUID.fromString("01977e27-e455-70b4-abf7-0907b0b9c7dc");

    @SneakyThrows
    public void preProcessAttachments(List<AttachmentSaveDTOv1> attachments, Map<String, MultipartFile> files) {
        ApiUser apiUser = authService.getApiUser();
        StorageEntity storage = storageService.findEntitySafe(apiUser.getDomain().getAttachmentsStorageId());
        StorageEntity externalLinkStorage = storageService.findEntitySafe(externalLinkStorageId);

        if (attachments != null && !attachments.isEmpty()) {
            attachments.forEach(att -> {
                boolean haveLink = att.storageLink != null && !att.storageLink.isBlank();
                boolean isMultipart = haveLink && att.storageLink.toLowerCase().startsWith("multipart://");
                boolean isExternalLink = !isMultipart;
                if (isExternalLink) {
                    att.setStorage(externalLinkStorage);
                } else {
                    att.setStorage(storage);
                }
                if (haveLink && isMultipart) {
                    MultipartFile file = files.get(att.storageLink.replaceFirst("multipart://", ""));
                    if (file == null) {
                        throw new RuntimeException("File not found for link " + att.storageLink);
                    }
                    try {
                        DomainFile domainFile = new DomainFile(file.getInputStream(), file.getOriginalFilename(), file.getSize());
                        att.setDomainFile(domainFile);
                        if (att.size == null || att.size < 1) {
                            att.size = domainFile.fileSize();
                        }
                    } catch (Throwable t) {
                        log.error("Error while processing multipart link {}", att.storageLink, t);
                        throw new RuntimeException("Error while processing multipart link " + att.storageLink, t);
                    }
                }
            });
        }
    }
    @Override
    public void map(AttachmentSaveDTOv1 src, TwinAttachmentEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinId(src.getTwinId())
                .setStorageFileKey(src.getStorageLink())
                .setStorageId(src.storage.getId())
                .setStorage(src.storage)
                .setAttachmentFile(src.domainFile)
                .setModifications(new Kit<>(TwinAttachmentModificationEntity::getModificationType))
                .setTitle(src.getTitle())
                //TODO set size as is.
                .setSize(src.getSize() == null ? 0 : src.getSize())
                .setDescription(src.getDescription())
                .setExternalId(src.getExternalId());
        if (null != src.getModifications()) {
            for (Map.Entry<String, String> mod : src.getModifications().entrySet()) {
                TwinAttachmentModificationEntity modEntity = new TwinAttachmentModificationEntity();
                modEntity
                        .setModificationType(mod.getKey())
                        .setStorageFileKey(mod.getValue());
                dst.getModifications().add(modEntity);
            }
        }
    }
}
