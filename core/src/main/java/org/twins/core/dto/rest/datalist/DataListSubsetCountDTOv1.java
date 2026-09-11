package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "DataListSubsetCountV1")
public class DataListSubsetCountDTOv1 extends CountDTOv1 {
    @Schema(description = "data list id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = DataListDTOv1.class, name = "dataList")
    public UUID dataListId;
}
