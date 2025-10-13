package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "PermissionGrantUserSearchRsV1")
public class PermissionGrantUserSearchRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;

    @Schema(description = "permission grant user list")
    public List<PermissionGrantUserDTOv1> permissionGrantUsers;
}
