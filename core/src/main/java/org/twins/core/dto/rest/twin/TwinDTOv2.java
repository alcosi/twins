package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.attachment.AttachmentViewDTOv1;
import org.twins.core.dto.rest.link.TwinLinkListDTOv1;
import org.twins.core.dto.rest.link.TwinLinkViewDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "TwinV2")
public class TwinDTOv2 extends TwinBaseDTOv3 {
    @Schema(description = "fields")
    public Map<String, String> fields;
}
