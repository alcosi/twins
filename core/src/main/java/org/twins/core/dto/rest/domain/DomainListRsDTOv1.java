package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "DomainListRsV1")
public class DomainListRsDTOv1 extends Response {
    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;

    @Schema(description = "domain list")
    List<DomainViewDTOv1> domainList;
}
