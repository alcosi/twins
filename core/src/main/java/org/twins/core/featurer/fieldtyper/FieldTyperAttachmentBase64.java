package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.file.FileData;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorAttachment;
import org.twins.core.featurer.fieldtyper.value.FieldValueAttachment;
import org.twins.core.featurer.storager.Storager;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.attachment.AttachmentService;

import java.io.ByteArrayInputStream;
import java.util.Properties;


@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_1335,
        name = "Attachment (single in base64 format)",
        description = "Allow the field to have an single attachment in base64 format")
public class FieldTyperAttachmentBase64 extends FieldTyperAttachment<FieldValueAttachment> {
    protected final Tika tika = new Tika();
    protected final TikaConfig config = TikaConfig.getDefaultConfig();
    private final AttachmentRestrictionService attachmentRestrictionService;
    private final AttachmentService attachmentService;


    @Override
    public FieldDescriptorAttachment getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        FieldDescriptorAttachment fieldDescriptor = super.getFieldDescriptor(twinClassFieldEntity, properties);
        return fieldDescriptor
                .minCount(1)
                .maxCount(1);
    }

    @Override
    @SneakyThrows
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueAttachment value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (StringUtils.isEmpty(value.getBase64Content()))
            return;
        var content = value.getBase64Content();

        TwinAttachmentEntity attachment = getSingleStoredAttachment(twin, value.getTwinClassField());
        if (attachment != null) {
            attachment.setFileChanged(true);
        } else {
            attachment = new TwinAttachmentEntity();
        }

        var base64 = content.contains(",") ? content.split(",")[1].replaceAll(" ", "") : content;
        byte[] bytes = Base64.decodeBase64(base64);
        StorageEntity storage = getStorage(properties);
        FileData domainFile = new FileData(new ByteArrayInputStream(bytes), value.getName(), (long) bytes.length);
        var apiUser = authService.getApiUser();
        attachment
                .setStorage(storage)
                .setStorageId(storage.getId())
                .setCreatedByUserId(apiUser.getUserId())
                .setCreatedByUser(apiUser.getUser())
                .setTwinId(twin.getId())
                .setTwin(twin)
                .setTwinClassFieldId(value.getTwinClassField().getId())
                .setTitle(value.getName())
                .setSize(domainFile.fileSize())
                .setAttachmentFile(domainFile);
        if (attachment.getId() != null) {
            attachmentService.updateAttachment(attachment, twinChangesCollector);
        } else {
            attachmentService.addAttachment(attachment, twinChangesCollector);
        }
    }

    @Override
    protected FieldValueAttachment deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        FieldValueAttachment fieldValue = new FieldValueAttachment(twinField.getTwinClassField());
        // Find attachments for this field
        TwinAttachmentEntity attachment = getSingleStoredAttachment(twinField.getTwin(), twinField.getTwinClassField());
        if (attachment == null)
            return fieldValue;
        try {
            StorageEntity storage = getStorage(properties);
            Storager fileService = featurerService.getFeaturer(storage.getStorageFeaturerId(), Storager.class);
            var bytes = fileService.getFileBytes(attachment.getStorageFileKey(), storage.getStoragerParams());
            fieldValue.setName(attachment.getTitle());
            fieldValue.setBase64Content("data:" + tika.detect(bytes) + ";base64," + Base64.encodeBase64String(bytes));
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Error converting attachment to base64: " + e.getMessage());
        }
        return fieldValue;
    }

    private TwinAttachmentEntity getSingleStoredAttachment(TwinEntity twin, TwinClassFieldEntity twinClassField) throws ServiceException {
        attachmentService.loadAttachments(twin);
        if (twin.getAttachmentKit().isEmpty())
            return null;
        var filteredAttachments = twin.getAttachmentKit().getList().stream().filter(att -> att.getTwinClassFieldId() != null &&
                att.getTwinClassFieldId().equals(twinClassField.getId())).toList();
        if (CollectionUtils.isEmpty(filteredAttachments)) {
            return null;
        } else if (filteredAttachments.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "{} does not support multi attachment", twinClassField.logNormal());
        } else {
            return filteredAttachments.getFirst();
        }
    }
}
