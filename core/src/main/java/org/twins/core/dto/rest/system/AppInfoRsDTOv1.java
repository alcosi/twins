package org.twins.core.dto.rest.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AppInfoRsV1")
public class AppInfoRsDTOv1 extends Response {

    @Schema(description = "Info attributes key/value")
    public Map<String, String> attributes;

}
