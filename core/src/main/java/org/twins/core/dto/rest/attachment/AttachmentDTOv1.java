package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.Instant;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "AttachmentV1")
public class AttachmentDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "id", example = DTOExamples.TWIN_ID)
    public UUID twinId;

    @Schema(description = "external file link")
    public String storageLink;

    @Schema(description = "created at", example = "1549632759")
    public Instant createdAt;

    @Schema(description = "author")
    public UserDTOv1 authorUser;
}
