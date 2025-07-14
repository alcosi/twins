package org.twins.face.dto.rest.twidget.tw006;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.action.TwinAction;
import org.twins.core.dao.i18n.I18nEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceTW0006ActionV1", description = "TW006 action dto")
public class FaceTW006ActionDTOv1 {

    private UUID faceTW006Id;
    private String actionId;
    private String label;
}
