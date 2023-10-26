package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinLinkAddRqV1")
public class TwinLinkAddRqDTOv1 extends Request {
    @Schema(description = "Links list")
    public List<TwinLinkAddDTOv1> links;
}
