package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;

import java.util.*;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorListV1")
public class TwinClassFieldDescriptorListDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "selectListV1";
    @Override
    public String fieldType() {
        return KEY;
    }

    @Schema(description = "Can custom option be entered", example = "false")
    public Boolean supportCustom;

    @Schema(description = "Multiple choice support", example = "true")
    public Boolean multiple;

    @Schema(description = "Valid options", example = "")
    public List<DataListOptionDTOv1> options = new ArrayList<>();

    @Schema(description = "Valid option id list", example = "")
    public Set<UUID> optionIdList = new HashSet<>();

}
