package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.PaginationBean;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "TwinSearchAndPaginationPsv1")
public class TwinSearchAndPaginationPsDTOv1 {
    @Schema(description = "Pagination bean (page, count entries on page, total entries)")
    public PaginationBean<Long> paginationBean;

    @Schema(description = "Twin list")
    public List<TwinEntity> response;
}
