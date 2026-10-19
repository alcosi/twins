package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryListRsV1")
public class FactoryListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - factory list")
    public List<FactoryDTOv1> factoryList;
}
