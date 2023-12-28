package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.time.Instant;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassBaseV1")
public class TwinClassBaseDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID id;

    @Schema(description = "key", example = "PROJECT")
    public String key;

    @Schema(description = "name", example = "Project")
    public String name;

    @Schema(description = "description", example = "Projects business objects")
    public String description;

    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public Instant createdAt;

    @Schema(description = "logo", example = "http://twins.org/t/class/project.png")
    public String logo;

    @Schema(description = "if class is abstract no twin of it can be created. Some child class must be used")
    public boolean abstractClass;

    @Schema(description = "head class")
    public TwinClassBaseDTOv1 headClass;

    @Schema(description = "head class id or empty if class is not linked to any head", example = DTOExamples.TWIN_CLASS_HEAD_CLASS_ID)
    public UUID headClassId;

    @Schema(description = "some markers for twins. Are domain level and not editable by user")
    public UUID markersDataListId;

    @Schema(description = "some tags for twins. Can be business account level and editable by user")
    public UUID tagsDataListId;
}
