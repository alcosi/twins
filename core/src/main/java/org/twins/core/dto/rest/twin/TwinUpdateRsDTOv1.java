package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinUpdateRsV1")
public class TwinUpdateRsDTOv1 extends Response {
    @Schema(description = "new twin id")
    public UUID twinId;

    @Schema(description = "twin alias list. Alias is unique in business account scope")
    public List<String> businessAccountAliasList;

    @Schema(description = "twin alias list. Alias is unique in domain scope")
    public List<String> domainAliasList;
}


