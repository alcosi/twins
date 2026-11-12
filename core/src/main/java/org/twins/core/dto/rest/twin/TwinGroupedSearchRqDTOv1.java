package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.twin.TwinGroupSort;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinGroupedSearchRqV1")
public class TwinGroupedSearchRqDTOv1 extends Request {
    @Schema(description = "Twin filter. Sort does not belong here")
    public TwinSearchDTOv1 search;

    @NotNull
    @Schema(description = "Twin class field id to group by. Same fields as twin count: basic columns and storages whose countGroup is set")
    public UUID groupFieldId;

    @Schema(description = "Order of groups. Default is NAME_ASC for assignee, owner, creator and head, KEY_ASC otherwise")
    public TwinGroupSort groupSort;

    @Valid
    @Size(max = 2)
    @Schema(description = "Order of twins inside every group. Empty means createdAt DESC, then id ASC. Basic fields only")
    public List<TwinSortDTOv1> memberSorts;
}
