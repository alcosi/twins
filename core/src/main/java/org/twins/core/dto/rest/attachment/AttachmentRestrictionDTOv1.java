package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "AttachmentRestrictionV1")
public class AttachmentRestrictionDTOv1 {

    @Schema(description = "attachment restriction id")
    private UUID id;

    @Schema(description = "Min amount of files")
    private int minCount;

    @Schema(description = "Max amount of files")
    private int maxCount;

    @Schema(description = "File size limit")
    private int fileSizeMbLimit;

    @Schema(description = "List of possible file extensions")
    private String fileExtensionLimit;

    @Schema(description = "Regexp for file name")
    private String fileNameRegexp;
}
