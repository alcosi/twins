package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorAttachment;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageAttachment;
import org.twins.core.featurer.fieldtyper.value.FieldValueInvisible;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsAttachmentRestrictionId;
import org.twins.core.service.attachment.AttachmentRestrictionService;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_1316,
        name = "Attachment",
        description = "Allow the field to have an attachment")
public class FieldTyperAttachment extends FieldTyper<FieldDescriptorAttachment, FieldValueInvisible, TwinFieldStorageAttachment, TwinFieldSearchNotImplemented> {

    private final AttachmentRestrictionService attachmentRestrictionService;

    @FeaturerParam(name = "Restriction Id", description = "Id of field typer restrictions", order = 1, optional = true)
    public static final FeaturerParamUUIDTwinsAttachmentRestrictionId restrictionId = new FeaturerParamUUIDTwinsAttachmentRestrictionId("restrictionId");

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

    @Deprecated
    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueInvisible value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Deprecated
    @Override
    protected FieldValueInvisible deserializeValue(Properties properties, TwinField twinField) {
        return new FieldValueInvisible(twinField.getTwinClassField());
    }

    public UUID getRestrictionId(HashMap<String, String> fieldTyperParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<>());
        return restrictionId.extract(properties);
    }

}
