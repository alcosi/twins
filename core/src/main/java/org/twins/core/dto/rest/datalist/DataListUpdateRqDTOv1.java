package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListUpdateRqV1")
public class DataListUpdateRqDTOv1 extends Request {

    @Valid
    @Schema(description = "data list update list")
    public List<DataListUpdateDTOv1> dataLists;
}
