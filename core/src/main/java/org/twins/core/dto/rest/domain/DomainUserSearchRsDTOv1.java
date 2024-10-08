package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainUserSearchRsV1")
public class DomainUserSearchRsDTOv1 extends Response {
    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;

    @Schema(description = "user list")
    public List<DomainUserDTOv1> users;
}
