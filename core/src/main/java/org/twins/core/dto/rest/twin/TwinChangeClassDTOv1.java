package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinChangeClassV1")
public class TwinChangeClassDTOv1 extends Request {

    @Schema(description = "New twin class id for twin", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "newTwinClass")
    public UUID newTwinClassId;

    @Schema(description = "New head twin id for twin", example = DTOExamples.HEAD_TWIN_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "newHeadTwin")
    public UUID newHeadTwinId;

    @Schema(description = "map [old twin class field id -> new twin class field id]")
    public Map<UUID, UUID> fieldsReplaceMap;

    @Schema(description = "Behavior of update process")
    public List<TwinChangeClassStrategy> behavior;

}


