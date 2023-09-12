package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinDTOv1;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassV1")
public class TwinClassDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID id;

    @Schema(description = "key", example = "PROJECT")
    public String key;

    @Schema(description = "name", example = "Project")
    public String name;

    @Schema(description = "description", example = "Projects business objects")
    public String description;

    @Schema(description = "created at", example = "1549632759")
    public Instant createdAt;

    @Schema(description = "logo", example = "http://twins.org/t/class/project.png")
    public String logo;

    @Schema(description = "space class id or empty if class is not linked to any space", example = DTOExamples.TWIN_CLASS_SPACE_CLASS_ID)
    public UUID spaceClassId;

    @Schema(description = "list of available spaces")
    public List<TwinDTOv1> validSpaces;

    @Schema(description = "Class fields list")
    public List<TwinClassFieldDTOv1> fields;
}
