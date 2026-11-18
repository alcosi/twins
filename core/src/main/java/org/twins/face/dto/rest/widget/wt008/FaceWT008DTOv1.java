package org.twins.face.dto.rest.widget.wt008;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.face.dto.rest.widget.FaceWidgetDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class FaceWT008DTOv1 extends FaceWidgetDTOv1 {
    @Schema(description = "widget key")
    public String key;

    @Schema(description = "widget label, empty falls back to FE default \"Comments\"")
    public String label;

    @Schema(description = "configured comment search id; comments are fetched via /private/comment/search/{searchId}/v1 with narrow.twinIdList = [pointedTwinId]")
    public UUID twinCommentSearchId;

    @Schema(description = "styles, converted to css classes")
    public Set<String> styleClasses;
}
