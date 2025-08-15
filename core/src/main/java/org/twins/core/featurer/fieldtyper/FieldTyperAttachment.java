package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorAttachment;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageAttachment;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsAttachmentRestrictionId;
import org.twins.core.service.attachment.AttachmentRestrictionService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.storage.StorageService;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

@Slf4j
public abstract class FieldTyperAttachment<T extends FieldValue> extends FieldTyper<FieldDescriptorAttachment, T, TwinFieldStorageAttachment, TwinFieldSearchNotImplemented> {
    @FeaturerParam(name = "Restriction Id", description = "Id of field typer restrictions", order = 1, optional = true)
    public static final FeaturerParamUUIDTwinsAttachmentRestrictionId restrictionId = new FeaturerParamUUIDTwinsAttachmentRestrictionId("restrictionId");

    @FeaturerParam(name = "Storage Id", description = "Storage id. If not set domain attachment storage will be used", order = 3, optional = true)
    public static final FeaturerParamUUID storageId = new FeaturerParamUUID("storageId");

    @Autowired
    @Lazy
    protected AttachmentRestrictionService attachmentRestrictionService;

    @Autowired
    @Lazy
    protected AuthService authService;

    @Autowired
    @Lazy
    protected StorageService storageService;

    @Override
    public FieldDescriptorAttachment getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        if (restrictionId.extract(properties) == null) {
            return new FieldDescriptorAttachment();
        }
        TwinAttachmentRestrictionEntity restriction = attachmentRestrictionService.findEntitySafe(restrictionId.extract(properties));
        return new FieldDescriptorAttachment()
                .minCount(restriction.getMinCount())
                .maxCount(restriction.getMaxCount())
                .extensions(restriction.getFileExtensionLimit())
                .fileSizeMbLimit(restriction.getFileSizeMbLimit())
                .filenameRegExp(restriction.getFileNameRegexp());
    }

    public UUID getRestrictionId(HashMap<String, String> fieldTyperParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<>());
        return restrictionId.extract(properties);
    }

    public UUID getStorageId(HashMap<String, String> fieldTyperParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<>());
        return getStorageId(properties);
    }

    public UUID getStorageId(Properties properties) throws ServiceException {
        UUID extractedStorageId = storageId.extract(properties);
        if (extractedStorageId != null) {
            return extractedStorageId;
        }
        return authService.getApiUser().getDomain().getAttachmentsStorageId();
    }

    public StorageEntity getStorage(Properties properties) throws ServiceException {
        return storageService.findEntitySafe(getStorageId(properties));
    }
}