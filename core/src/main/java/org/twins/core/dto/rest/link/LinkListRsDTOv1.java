package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassLinkListRsV1")
public class LinkListRsDTOv1 extends Response {
    @Schema()
    public Map<UUID, LinkDTOv1> forwardLinkMap;

    @Schema()
    public Map<UUID, LinkDTOv1> backwardLinkMap;
}
