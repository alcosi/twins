package org.twins.face.dto.rest.widget.wt009;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.action.TwinAction;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceWT009ActionV1", description = "WT009 action dto")
public class FaceWT009ActionDTOv1 {

    private UUID faceWT009Id;
    private TwinAction actionId;
    private String label;
}
