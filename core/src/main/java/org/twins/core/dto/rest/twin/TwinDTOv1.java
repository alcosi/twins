package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.Instant;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinV1")
public class TwinDTOv1 {
    @Schema(description = "id", example = "c2a7f81f-d7da-43e8-a1d3-18d6f632878b")
    public UUID id;

    @Schema(description = "externalId", example = "934599502DFFAE")
    public String externalId;

    @Schema(description = "created at", example = "1549632759")
    public Instant createdAt;

    @Schema(description = "name", example = "Oak")
    public String name;

    @Schema(description = "description", example = "The biggest tree")
    public String description;

    @Schema(description = "status")
    public TwinStatusDTOv1 status;

    @Schema(description = "class")
    public TwinClassDTOv1 twinClass;

    @Schema(description = "current assigner")
    public UserDTOv1 assignerUser;

    @Schema(description = "author")
    public UserDTOv1 authorUser;


}
