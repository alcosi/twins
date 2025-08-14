package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.file.DomainFile;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorAttachment;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageAttachment;
import org.twins.core.featurer.fieldtyper.value.FieldValueAttachment;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsAttachmentRestrictionId;
import org.twins.core.featurer.storager.Storager;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.storage.StorageService;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_1316,
        name = "Attachment",
        description = "Allow the field to have an attachment")
public class FieldTyperAttachment extends FieldTyper<FieldDescriptorAttachment, FieldValueAttachment, TwinFieldStorageAttachment, TwinFieldSearchNotImplemented> {
    protected final Tika tika = new Tika();
    protected final TikaConfig config = TikaConfig.getDefaultConfig();
    private final AttachmentRestrictionService attachmentRestrictionService;
    private final AttachmentService attachmentService;
    protected final StorageService storageService;
    protected final AuthService authService;

    @FeaturerParam(name = "Restriction Id", description = "Id of field typer restrictions", order = 1, optional = true)
    public static final FeaturerParamUUIDTwinsAttachmentRestrictionId restrictionId = new FeaturerParamUUIDTwinsAttachmentRestrictionId("restrictionId");

    @FeaturerParam(name = "Base64 Format", description = "Allow to send attachments in base64 format", order = 2, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean base64Format = new FeaturerParamBoolean("base64Format");

    @FeaturerParam(name = "storageId", description = "Storage id. If not set domain attachment storage will be used", order = 3, optional = true)
    public static final FeaturerParamUUID storageId = new FeaturerParamUUID("storageId");

    @Override
    public FieldDescriptorAttachment getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        if (restrictionId.extract(properties) != null) {
            TwinAttachmentRestrictionEntity restriction = attachmentRestrictionService.findEntitySafe(restrictionId.extract(properties));

            return new FieldDescriptorAttachment()
                    .minCount(restriction.getMinCount())
                    .maxCount(restriction.getMaxCount())
                    .extensions(restriction.getFileExtensionLimit())
                    .fileSizeMbLimit(restriction.getFileSizeMbLimit())
                    .filenameRegExp(restriction.getFileNameRegexp());
        }
        return new FieldDescriptorAttachment();
    }

    @Override
    @SneakyThrows
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueAttachment value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        Boolean isBase64Format = base64Format.extract(properties);
        if (Boolean.TRUE.equals(isBase64Format) &&
                value.getBase64Content() != null &&
                !value.getBase64Content().isEmpty()) {
            var content = value.getBase64Content();
            var base64 = content.contains(",") ? content.split(",")[1] : content;
            byte[] bytes = Base64.decodeBase64(base64);
            UUID extractedStorageId = storageId.extract(properties);
            ApiUser apiUser = authService.getApiUser();
            var storageId = extractedStorageId == null ? apiUser.getDomain().getAttachmentsStorageId() : extractedStorageId;
            StorageEntity storage = storageService.findEntitySafe(storageId);
            DomainFile domainFile = new DomainFile(new ByteArrayInputStream(bytes), value.getName(), (long) bytes.length);

            TwinAttachmentEntity attachmentEntity = new TwinAttachmentEntity()
                    .setStorage(storage)
                    .setStorageId(storageId)
                    .setCreatedByUserId(apiUser.getUserId())
                    .setCreatedByUser(apiUser.getUser())
                    .setTwinId(twin.getId())
                    .setTwinClassFieldId(value.getTwinClassField().getId())
                    .setTitle(value.getName())
                    .setAttachmentFile(domainFile);
            //Delete this filed attachments before serialization if field is updated
            attachmentService.loadAttachments(twin);
            java.util.List<TwinAttachmentEntity> attachments = twin.getAttachmentKit().getList();
            var filteredAttachments = attachments == null ? null : attachments.stream().filter(att -> att.getTwinClassFieldId() != null &&
                    att.getTwinClassFieldId().equals(value.getTwinClassField().getId())).toList();
            attachmentService.addAttachments(java.util.Collections.singletonList(attachmentEntity), twinChangesCollector);
            attachmentService.deleteAttachments(filteredAttachments, twinChangesCollector);
        }
    }

    @Override
    protected FieldValueAttachment deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        FieldValueAttachment fieldValue = new FieldValueAttachment(twinField.getTwinClassField());
        Boolean isBase64Format = base64Format.extract(properties);

        if (Boolean.TRUE.equals(isBase64Format)) {
            // Find attachments for this field
            TwinEntity twinEntity = twinField.getTwin();
            if (twinEntity != null && twinEntity.getId() != null) {
                attachmentService.loadAttachments(twinEntity);
                java.util.List<TwinAttachmentEntity> attachments = twinEntity.getAttachmentKit().getList();
                var attachment = attachments == null ? null : attachments.stream().filter(att -> att.getTwinClassFieldId() != null &&
                        att.getTwinClassFieldId().equals(twinField.getTwinClassField().getId())).findAny().orElse(null);
                if (attachment != null) {
                    try {
                        StorageEntity storage = storageService.findEntitySafe(attachment.getStorageId());
                        Storager fileService = featurerService.getFeaturer(storage.getStorageFeaturer(), Storager.class);
                        var bytes = fileService.getFileBytes(attachment.getStorageFileKey(), storage.getStoragerParams());
                        fieldValue.setName(attachment.getTitle());
                        fieldValue.setBase64Content(Base64.encodeBase64String(bytes));
                    } catch (Exception e) {
                        throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Error converting attachment to base64: " + e.getMessage());
                    }
                }
            }

        }

        return fieldValue;
    }

    public UUID getRestrictionId(HashMap<String, String> fieldTyperParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<>());
        return restrictionId.extract(properties);
    }

}
