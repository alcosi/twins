package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.LinkGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "LinkCountRqV1")
public class LinkCountRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public LinkSearchDTOv1 search;

    @Size(max = 4)
    @Schema(description = "Group by fields")
    public Set<LinkGroupField> groupFields;
}
