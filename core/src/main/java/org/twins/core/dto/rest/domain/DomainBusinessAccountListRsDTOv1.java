package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainBusinessAccountListRsV1")
public class DomainBusinessAccountListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - domain business account list")
    List<DomainBusinessAccountDTOv1> businessAccounts;
}
