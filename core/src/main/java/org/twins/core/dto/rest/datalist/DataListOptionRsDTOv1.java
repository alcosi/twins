package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.UUID;

@Deprecated
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListOptionRsV1")
public class DataListOptionRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Deprecated
    @Schema(description = "id", example = DTOExamples.DATA_LIST_ID)
    @RelatedObject(type = DataListDTOv1.class, name = "dataList")
    public UUID dataListId;

    @Schema(description = "data lists option")
    public DataListOptionDTOv1 option;
}
