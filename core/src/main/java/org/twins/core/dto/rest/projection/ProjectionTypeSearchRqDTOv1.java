package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "ProjectionTypeSearchRqV1")
public class ProjectionTypeSearchRqDTOv1 extends Request {
    @Schema
    public ProjectionTypeSearchDTOv1 search;
}