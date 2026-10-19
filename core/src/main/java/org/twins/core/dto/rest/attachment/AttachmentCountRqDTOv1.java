package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.AttachmentGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "AttachmentCountRqV1")
public class AttachmentCountRqDTOv1 extends Request {
    @Schema(description = "search params")
    public AttachmentSearchDTOv1 search;

    @Size(max = 2)
    @Schema(description = "Group by fields")
    public Set<AttachmentGroupField> groupFields;
}
