package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "AttachmentCreateValidateRqV1")
public class AttachmentCreateValidateRqDTOv1 {
    @Schema(description = "Twin class id")
    public UUID twinClassId;

    @Schema(description = "Attachments for adding")
    public List<AttachmentCreateDTOv1> create;

    public AttachmentCreateValidateRqDTOv1 addCreateItem(AttachmentCreateDTOv1 item) {
        this.create = CollectionUtils.safeAdd(this.create, item);
        return this;
    }
}
