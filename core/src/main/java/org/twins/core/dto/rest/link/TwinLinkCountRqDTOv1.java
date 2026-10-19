package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.TwinLinkGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinLinkCountRqV1")
public class TwinLinkCountRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public TwinLinkSearchDTOv1 search;

    @Size(max = 4)
    @Schema(description = "Group by fields")
    public Set<TwinLinkGroupField> groupFields;
}
