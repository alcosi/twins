package org.twins.core.domain.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinAttachmentEntity;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class AttachmentCud {
    @Schema(description = "Attachments for adding")
    public List<TwinAttachmentEntity> create;

    @Schema(description = "Attachments for updating")
    public List<TwinAttachmentEntity> update;

    @Schema(description = "Attachments id list for deleting")
    public List<UUID> delete;
}
