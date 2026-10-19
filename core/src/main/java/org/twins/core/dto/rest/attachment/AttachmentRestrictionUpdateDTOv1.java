package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "AttachmentRestrictionUpdateV1")
public class AttachmentRestrictionUpdateDTOv1 {
    @NotNull
    @Schema(description = "attachment restriction id", example = DTOExamples.UUID_ID)
    public UUID id;

    @Schema(description = "Min amount of files")
    public int minCount;

    @Schema(description = "Max amount of files")
    public int maxCount;

    @Schema(description = "File size limit")
    public int fileSizeMbLimit;

    @Schema(description = "List of possible file extensions")
    public String fileExtensionLimit;

    @Schema(description = "Regexp for file name")
    public String fileNameRegexp;
}
