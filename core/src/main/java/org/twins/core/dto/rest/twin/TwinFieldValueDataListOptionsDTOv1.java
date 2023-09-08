package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinFieldValueDataListOptionsDTOv1")
public class TwinFieldValueDataListOptionsDTOv1 implements TwinFieldValueDTO {
    public static final String KEY = "listOptionsV1";
    public String fieldType = KEY;
    @Schema(description = "Some simple text", example = "Hello world")
    public List<DataListOptionDTOv1> selectedOptions = new ArrayList<>();

    public TwinFieldValueDataListOptionsDTOv1 add(DataListOptionDTOv1 dataListOption) {
        selectedOptions.add(dataListOption);
        return this;
    }
}
