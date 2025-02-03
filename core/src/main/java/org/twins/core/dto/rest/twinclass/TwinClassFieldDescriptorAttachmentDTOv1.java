package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinClassFieldDescriptorAttachmentV1")
public class TwinClassFieldDescriptorAttachmentDTOv1 implements TwinClassFieldDescriptorDTO {
    public static final String KEY = "attachmentFieldV1";
    public String fieldType = KEY;

    @Schema(description = "Min count of files to upload", example = "1")
    public Integer minCount;

    @Schema(description = "Max count of files to upload", example = "1")
    public Integer maxCount;

    @Schema(description = "Allowed extensions", example = "[\"jpg\", \"jpeg\", \"png\"]")
    public List<String> extensions = new ArrayList<>();

    @Schema(description = "Filename must match this regexp", example = ".*")
    public String filenameRegExp;

    @Schema(description = "Filesize limit(per file)", example = "8")
    public Integer fileSizeMbLimit;
}
