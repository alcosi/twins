package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.projection.ProjectionTypeDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "DataListOptionProjectionV1")
public class DataListOptionProjectionDTOv1 {
    @Schema
    public UUID id;

    @Schema(description = "projection type id")
    @RelatedObject(type = ProjectionTypeDTOv1.class, name = "projectionType")
    public UUID projectionTypeId;

    @Schema(description = "src data list option id")
    @RelatedObject(type = DataListOptionDTOv1.class, name = "srcDataListOption")
    public UUID srcDataListOptionId;

    @Schema(description = "dst data list option id")
    @RelatedObject(type = DataListOptionDTOv1.class, name = "dstDataListOption")
    public UUID dstDataListOptionId;

    @Schema(description = "saved by user id")
    @RelatedObject(type = UserDTOv1.class, name = "savedByUser")
    public UUID savedByUserId;

    @Schema(description = "changed at")
    public LocalDateTime changedAt;
}
