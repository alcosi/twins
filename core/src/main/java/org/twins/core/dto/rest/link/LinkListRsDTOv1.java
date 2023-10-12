package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassLinkListRsV1")
public class LinkListRsDTOv1 extends Response {
    @Schema()
    public List<LinkDTOv1> forwardLinkList;

    @Schema()
    public List<LinkDTOv1> backwardLinkList;
}
