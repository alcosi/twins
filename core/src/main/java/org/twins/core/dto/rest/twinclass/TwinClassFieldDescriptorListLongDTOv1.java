package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorListLongV1")
public class TwinClassFieldDescriptorListLongDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "selectLongV1";
    @Override
    public String fieldType() {
        return KEY;
    }

    @Schema(description = "Can custom option be entered", example = "false")
    public Boolean supportCustom;

    @Schema(description = "Multiple choice support", example = "true")
    public Boolean multiple;

    @Schema(description = "Data list id for grabbing valid options", example = "")
    @RelatedObject(type = DataListDTOv1.class, name = "dataList")
    public UUID dataListId;

    @Schema(description = "Valid option id list", example = "")
    public Set<UUID> dataListOptionIdList;

    @Schema(description = "Excluded option id list", example = "")
    public Set<UUID> dataListOptionIdExcludeList;

    @Schema(description = "Valid subset id list", example = "")
    public Set<UUID> dataListSubsetIdList;

    @Schema(description = "Excluded subset id list", example = "")
    public Set<UUID> dataListSubsetIdExcludeList;

}


