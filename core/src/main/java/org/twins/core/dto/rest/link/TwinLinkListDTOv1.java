package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "TwinLinkListV1")
public class TwinLinkListDTOv1 extends TwinLinkAddDTOv1 {
    @Schema(description = "links")
    public Map<UUID, TwinLinkViewDTOv1> forwardLinks;

    @Schema(description = "links")
    public Map<UUID, TwinLinkViewDTOv1> backwardLinks;
}
