package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TwinSortV1", description = "Sort direction: ASC or DESC")
public enum SortDirectionDTOv1 {
    ASC,
    DESC
}
