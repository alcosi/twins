package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListCreateRqV1")
public class DataListCreateRqDTOv1 extends Request {

    @Valid
    @NotEmpty
    @Size(max = 50)
    @Schema(description = "data list create list")
    public List<DataListCreateDTOv1> dataLists;
}
