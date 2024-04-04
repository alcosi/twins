package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "LocaleViewV1")
public class LocaleListRsDTOv1 extends Response {
    @Schema(description = "locales in domain")
    public List<LocaleDTOv1> localeList;
}
