package org.twins.core.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.SortDTOv1;
import org.twins.core.enums.sort.UserSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserSortV1")
public class UserSortDTOv1 extends SortDTOv1 {
    @NotNull
    @Schema(description = "Sort field. Default: name")
    public UserSortField sortField;
}
