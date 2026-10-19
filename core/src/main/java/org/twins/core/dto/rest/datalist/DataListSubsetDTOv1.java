package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DataListSubsetV1")
public class DataListSubsetDTOv1 {

    @Schema(description = "data list subset id")
    private UUID id;

    @Schema(description = "data list id")
    @RelatedObject(type = DataListDTOv1.class, name = "dataList")
    private UUID dataListId;

    @Schema(description = "data list subset name")
    private String name;

    @Schema(description = "data list subset description")
    private String description;

    @Schema(description = "data list subset key")
    private String key;

    @Schema(description = "creation time")
    private LocalDateTime createdAt;

    @Schema(description = "created by user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
    public UUID createdByUserId;
}
