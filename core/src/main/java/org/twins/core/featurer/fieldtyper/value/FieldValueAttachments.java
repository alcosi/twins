package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueAttachments extends FieldValue {
    private List<TwinAttachmentEntity> attachments = new ArrayList<>();

    public FieldValueAttachments(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    public FieldValueAttachments add(TwinAttachmentEntity attachment) {
        attachments.add(attachment);
        return this;
    }

    @Override
    public boolean isFilled() {
        return CollectionUtils.isNotEmpty(attachments);
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueAttachments clone = new FieldValueAttachments(newTwinClassFieldEntity);
        clone.getAttachments().addAll(attachments);
        return clone;
    }

    @Override
    public void nullify() {
        attachments = Collections.EMPTY_LIST;
    }


    @Override
    public boolean isNullified() {
        return attachments != null && attachments.isEmpty();
    }

    @Override
    public boolean hasValue(String value) {
        if (CollectionUtils.isEmpty(attachments)) {
            return false;
        }
        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }
        for (TwinAttachmentEntity attachment : attachments) {
            if (attachment.getId() != null && attachment.getId().equals(valueUUID)) {
                return true;
            }
        }
        return false;
    }
}
