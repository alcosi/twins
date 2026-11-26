package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.SearchConfiguredRqDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CommentSearchConfiguredRqV1")
public class CommentSearchConfiguredRqDTOv1 extends SearchConfiguredRqDTOv1 {
    @Schema(description = "search narrow")
    public CommentSearchDTO narrow;

    @Valid
    @Size(max = 2)
    @Schema(description = "Sort rules. Max 2 fields; list order defines precedence. Default: createdAt ASC")
    public List<CommentSortDTOv1> sorts;
}
