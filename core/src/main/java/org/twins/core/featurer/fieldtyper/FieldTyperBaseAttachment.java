package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.value.FieldValueAttachments;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.twins.core.service.SystemEntityService.*;

@Component
@Featurer(id = FeaturerTwins.ID_1333,
        name = "BaseAttachments",
        description = "Field typer for base attachments twin field")
public class FieldTyperBaseAttachment extends FieldTyper<FieldDescriptorImmutable, FieldValueAttachments, TwinEntity, TwinFieldSearchNotImplemented> {

    @Override
    protected FieldDescriptorImmutable getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorImmutable();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueAttachments value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_IMMUTABLE, "attachments change is not allowed.");
    }

    @Override
    protected FieldValueAttachments deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        UUID fieldId = twinField.getTwinClassField().getId();
        List<TwinAttachmentEntity> attachments = twin.getAttachmentKit().getList();

        FieldValueAttachments value = new FieldValueAttachments(twinField.getTwinClassField());
        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_ATTACHMENT_ALL_FIELDS)) {
            return value.setAttachments(attachments.stream().filter(a -> a.getTwinClassFieldId() != null).toList());
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_ATTACHMENT_TRANSITION)) {
            return value.setAttachments(attachments.stream().filter(a -> a.getTwinflowTransitionId() != null).toList());
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_ATTACHMENT_COMMENT)) {
            return value.setAttachments(attachments.stream().filter(a -> a.getTwinCommentId() != null).toList());
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_ATTACHMENT)) {
            return value.setAttachments(attachments.stream().filter(a ->
                    a.getTwinClassFieldId() == null &&
                    a.getTwinCommentId() == null &&
                    a.getTwinflowTransitionId() == null).toList());
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                "Field [" + twinField.getTwinClassField().logShort() + "] is not a supported base field for " + twin.logNormal());
    }
}
