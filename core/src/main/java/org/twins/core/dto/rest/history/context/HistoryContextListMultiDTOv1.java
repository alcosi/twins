package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "HistoryContextListMultiV1")
public class HistoryContextListMultiDTOv1 implements HistoryContextDTO {
    public static final String KEY = "listV1";
    public String contextType = KEY;

    @Schema(description = "From data list option id")
    public UUID fromDatalistOptionId;

    @Schema(description = "From data list option")
    public DataListOptionDTOv1 fromDatalistOption;

    @Schema(description = "To data list option id")
    public UUID toDatalistOptionId;

    @Schema(description = "To data list option")
    public DataListOptionDTOv1 toDatalistOption;


}
