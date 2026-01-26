package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.HashMap;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "LinkSaveV1")
public class LinkSaveDTOv1 extends Request {

    @Schema(description = "Forward name i18n (if target twin-class as src)")
    public I18nSaveDTOv1 forwardNameI18n;

    @Schema(description = "Backward name i18n (if target twin-class as dst)")
    public I18nSaveDTOv1 backwardNameI18n;

    @Schema(description = "Link type (Many-to-one, Many-to-many, One-to-one)")
    public LinkType type;

    @Schema(description = "Link strength (MANDATORY, OPTIONAL, OPTIONAL_BUT_DELETE_CASCADE)")
    public LinkStrength linkStrength;

    @Schema(description = "[optional] an id of linker featurer", example = "")
    public Integer linkerFeaturerId;

    @Schema(description = "[optional] linker featurer params", example = "")
    public HashMap<String, String> linkerParams;

}
