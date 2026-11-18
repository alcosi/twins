package org.twins.core.dto.rest.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ResourceUpdateRqV1")
public class ResourceUpdateRqDTOv1 extends Request {
    @Valid
    @Schema(description = "resources update list")
    public List<ResourceUpdateDTOv1> resources;
}
