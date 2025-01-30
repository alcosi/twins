package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinTagAddV1")
public class TwinTagAddDTOv1 {
    @Schema(description = "add already existing tags by their ids", example = DTOExamples.UUID_COLLECTION)
    public Set<UUID> existingTags;

    @Schema(description = "add new tags by name (in current locale). If tag with given name is already exist, it will be used", example = DTOExamples.STRING_COLLECTION)
    public Set<String> newTags;
}
