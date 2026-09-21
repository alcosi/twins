package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListSubsetUpdateV1")
public class DataListSubsetUpdateDTOv1 extends DataListSubsetSaveDTOv1 {
    @NotNull
    @Schema(description = "data list subset id", example = DTOExamples.UUID_ID)
    public UUID id;
}
