package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "AttachmentRestrictionDeleteRqV1")
public class AttachmentRestrictionDeleteRqDTOv1 extends Request {
    @Schema(description = "attachment restriction id list to delete")
    public Set<UUID> attachmentRestrictionIdList;
}
