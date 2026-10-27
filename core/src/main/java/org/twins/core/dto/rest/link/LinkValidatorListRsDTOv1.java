package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "LinkValidatorListRsV1")
public class LinkValidatorListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "link validator list")
    public List<LinkValidatorDTOv1> linkValidators;
}
