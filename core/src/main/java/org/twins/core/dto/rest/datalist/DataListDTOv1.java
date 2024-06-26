package org.twins.core.dto.rest.datalist;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "DataListV1")
public class DataListDTOv1 {
    @Schema(description = "id", example = DTOExamples.DATA_LIST_ID)
    public UUID id;

    @Schema(description = "name", example = "Country list")
    public String name;

    @Schema(description = "description", example = "Supported country list")
    public String description;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "updated at", example = DTOExamples.INSTANT)
    public LocalDateTime updatedAt;

    @Schema(description = "List of option ids")
    public Set<UUID> optionIdList;

    @Schema(description = "List options")
    public Map<UUID, DataListOptionDTOv1> options;
}
