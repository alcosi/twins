package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.DataListOptionGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListOptionCountRqV1")
public class DataListOptionCountRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public DataListOptionSearchDTOv1 search;

    @Size(max = 2)
    @Schema(description = "Group by fields")
    public Set<DataListOptionGroupField> groupFields;
}
