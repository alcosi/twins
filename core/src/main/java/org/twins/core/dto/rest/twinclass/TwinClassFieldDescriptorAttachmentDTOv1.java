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
    public static final String KEY = "attachmentFieldDescriptorV1";
    public String fieldType = KEY;

    @Schema(description = "Multiple file-choice support", example = "true")
    public Boolean multiple;

    @Schema(description = "Allowed extensions", example = "[\"jpg\", \"jpeg\", \"png\"]")
    public List<String> extensions = new ArrayList<>();

    @Schema(description = "Filename must match this regexp", example = ".*")
    public String filenameRegExp;

    @Schema(description = "Filesize limit(per file)", example = "8")
    public Integer fileSizeMbLimit;
}
