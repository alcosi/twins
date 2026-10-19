package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "AttachmentRestrictionCreateV1")
public class AttachmentRestrictionCreateDTOv1 {
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
