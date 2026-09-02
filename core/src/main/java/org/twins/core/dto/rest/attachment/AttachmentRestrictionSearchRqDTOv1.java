package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.AttachmentRestrictionSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "AttachmentRestrictionSearchRqV1")
public class AttachmentRestrictionSearchRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public AttachmentRestrictionSearchDTOv1 search;

    @Schema(description = "Sort field. Default: minCount")
    public AttachmentRestrictionSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
