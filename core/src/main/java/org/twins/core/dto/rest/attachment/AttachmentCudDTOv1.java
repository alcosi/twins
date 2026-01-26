package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "AttachmentCudV1")
public class AttachmentCudDTOv1 {
    @Schema(description = "Attachments for adding")
    public List<AttachmentCreateDTOv1> create;

    @Schema(description = "Attachments for updating")
    public List<AttachmentUpdateDTOv1> update;

    @Schema(description = "Attachments id list for deleting")
    public Set<UUID> delete;

    public AttachmentCudDTOv1 addCreateItem(AttachmentCreateDTOv1 item) {
        this.create = CollectionUtils.safeAdd(this.create, item);
        return this;
    }

    public AttachmentCudDTOv1 addUpdateItem(AttachmentUpdateDTOv1 item) {
        this.update = CollectionUtils.safeAdd(this.update, item);
        return this;
    }

    public AttachmentCudDTOv1 addDeleteItem(UUID item) {
        this.delete = CollectionUtils.safeAdd(this.delete, item);
        return this;
    }

}
