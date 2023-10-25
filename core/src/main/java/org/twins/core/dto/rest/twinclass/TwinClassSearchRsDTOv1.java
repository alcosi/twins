package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassListRsV1")
public class TwinClassSearchRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - twin class list")
    public List<TwinClassDTOv1> twinClassList;
}
