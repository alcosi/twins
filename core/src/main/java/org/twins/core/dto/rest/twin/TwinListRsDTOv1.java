package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.PaginationBean;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinListRsV4")
public class TwinListRsDTOv1 extends Response {
    @Schema(description = "pagination data")
    public PaginationBean pagination;

    @Schema(description = "results - transfers list")
    public List<TwinDTOv1> twinList;
}
