package org.twins.core.dto.rest.twinpointer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinPointerDeleteRqV1")
public class TwinPointerDeleteRqDTOv1 extends Request {
    @Schema(description = "twin pointer id list to delete")
    public Set<UUID> twinPointerIdList;
}
