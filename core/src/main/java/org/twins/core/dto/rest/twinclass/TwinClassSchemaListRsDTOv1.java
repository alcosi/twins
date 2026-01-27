package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassSchemaListRsV1")
public class TwinClassSchemaListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "twin class schema list")
    public List<TwinClassSchemaDTOv1> twinClassSchemas;
}