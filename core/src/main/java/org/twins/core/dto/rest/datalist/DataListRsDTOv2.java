package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListRsV2")
public class DataListRsDTOv2 extends DataListRsDTOv1 {

    @Schema(description = "data list options")
    @RelatedObject(type = DataListOptionDTOv1.class, name = "options")
    public Set<UUID> options;
}
