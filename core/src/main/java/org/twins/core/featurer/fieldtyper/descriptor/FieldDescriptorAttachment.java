package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinAttachmentEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorAttachment extends FieldDescriptor {
    private boolean multiple;
    private UUID userFilterId;
    private List<TwinAttachmentEntity> attachments = new ArrayList<>();

    public FieldDescriptorAttachment add(TwinAttachmentEntity attachment) {
        attachments.add(attachment);
        return this;
    }
}
