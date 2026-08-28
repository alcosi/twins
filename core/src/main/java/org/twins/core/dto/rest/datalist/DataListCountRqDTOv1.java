package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.DataListGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListCountRqV1")
public class DataListCountRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public DataListSearchDTOv1 search;

    @Size(max = 1)
    @Schema(description = "Group by fields")
    public Set<DataListGroupField> groupFields;
}
