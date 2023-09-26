package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.Instant;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "AttachmentViewV1")
public class AttachmentViewDTOv1 extends AttachmentAddDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "created at", example = "1549632759")
    public Instant createdAt;

    @Schema(description = "author")
    public UserDTOv1 authorUser;
}
