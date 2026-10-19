package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.AttachmentSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "AttachmentSearchRqV2")
public class AttachmentSearchRqDTOv2 extends Request {
    @Schema(description = "search params")
    public AttachmentSearchDTOv1 search;

    @Schema(description = "Sort field. Default: createdAt")
    public AttachmentSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
