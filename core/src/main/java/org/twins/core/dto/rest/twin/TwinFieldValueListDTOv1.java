package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "TwinFieldValueDataListOptionsV1")
public class TwinFieldValueListDTOv1 extends TwinFieldValueDTO {
    public static final String KEY = "listOptionsV1";
    public String valueType = KEY;
    @Schema(description = "Selected option", example = "")
    public List<DataListOptionDTOv1> selectedOptions = new ArrayList<>();

    public TwinFieldValueListDTOv1 add(DataListOptionDTOv1 dataListOption) {
        selectedOptions.add(dataListOption);
        return this;
    }
}
