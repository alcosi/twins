package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinFieldValueDataListOptions")
public class TwinFieldValueDataListOptions implements TwinFieldValue {
    @Schema(description = "discriminator", requiredMode = Schema.RequiredMode.REQUIRED)
    public String fieldType = TwinFieldValueDataListOptions.class.getSimpleName();
    @Schema(description = "Some simple text", example = "Hello world")
    public List<DataListOptionDTOv1> selectedOptions = new ArrayList<>();

    public TwinFieldValueDataListOptions add(DataListOptionDTOv1 dataListOption) {
        selectedOptions.add(dataListOption);
        return this;
    }
}
