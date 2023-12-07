
package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinLinkCudV1")
public class TwinLinkCudDTOv1 {
    @Schema(description = "TwinLinks for adding")
    public List<TwinLinkAddDTOv1> create;

    @Schema(description = "TwinLinks for updating")
    public List<TwinLinkUpdateDTOv1> update;

    @Schema(description = "TwinLinks id list for deleting")
    public List<UUID> delete;
}
