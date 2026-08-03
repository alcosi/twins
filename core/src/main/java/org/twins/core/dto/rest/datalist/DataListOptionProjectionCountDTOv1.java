package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.projection.ProjectionTypeDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "DataListOptionProjectionCountV1")
public class DataListOptionProjectionCountDTOv1 extends CountDTOv1 {
    @Schema(description = "projection type id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = ProjectionTypeDTOv1.class, name = "projectionType")
    public UUID projectionTypeId;

    @Schema(description = "src data list option id", example = DTOExamples.DATA_LIST_OPTION_ID)
    @RelatedObject(type = DataListOptionDTOv1.class, name = "srcDataListOption")
    public UUID srcDataListOptionId;

    @Schema(description = "dst data list option id", example = DTOExamples.DATA_LIST_OPTION_ID)
    @RelatedObject(type = DataListOptionDTOv1.class, name = "dstDataListOption")
    public UUID dstDataListOptionId;

    @Schema(description = "saved by user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "savedByUser")
    public UUID savedByUserId;
}
