package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.SortDTOv1;
import org.twins.core.enums.sort.CommentSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CommentSortV1")
public class CommentSortDTOv1 extends SortDTOv1 {
    @NotNull
    @Schema(description = "Sort field. Default: createdAt")
    public CommentSortField sortField;
}
