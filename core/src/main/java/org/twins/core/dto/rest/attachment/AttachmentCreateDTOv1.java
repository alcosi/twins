package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AttachmentCreateV1")
public class AttachmentCreateDTOv1 extends AttachmentSaveDTOv1 {
    @Schema(description = "link to the field to which attachment was added (if any)")
    public UUID twinClassFieldId;

    @Schema(description = "link to the comment to which attachment was added (if any)")
    public UUID commentId;

    @Override
    public AttachmentCreateDTOv1 putModificationsItem(String key, String item) {
        if (this.modifications == null) this.modifications = new HashMap<>();
        this.modifications.put(key, item);
        return this;
    }
}
