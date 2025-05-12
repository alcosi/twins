package org.twins.core.dto.rest.datalist;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DataListV1")
public class DataListDTOv1 {
    @Schema(description = "id", example = DTOExamples.DATA_LIST_ID)
    public UUID id;

    @Schema(description = "name", example = "Country list")
    public String name;

    @Schema(description = "description", example = "Supported country list")
    public String description;

    @Schema(description = "key", example = DTOExamples.DATA_LIST_KEY)
    public String key;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "updated at", example = DTOExamples.INSTANT)
    public LocalDateTime updatedAt;

    @Schema(description = "attribute1")
    public DataListAttributeDTOv1 attribute1;

    @Schema(description = "attribute2")
    public DataListAttributeDTOv1 attribute2;

    @Schema(description = "attribute3")
    public DataListAttributeDTOv1 attribute3;

    @Schema(description = "attribute4")
    public DataListAttributeDTOv1 attribute4;

    @Schema(description = "attribute5")
    public DataListAttributeDTOv1 attribute5;

    @Schema(description = "external id")
    public String externalId;
}
