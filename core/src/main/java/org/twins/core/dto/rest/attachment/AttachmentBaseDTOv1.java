package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "AttachmentBaseV1")
public class AttachmentBaseDTOv1 {
    @Schema(description = "twin id", example = DTOExamples.TWIN_ID)
    public UUID twinId;

    @Schema(description = "External storage links map by key", example = DTOExamples.ATTACHMENT_STORAGE_LINKS_MAP)
    public Map<String, String> storageLinksMap;

    @Schema(description = "External id", example = DTOExamples.ATTACHMENT_EXTERNAL_ID)
    public String externalId;

    @Schema(description = "Title", example = DTOExamples.ATTACHMENT_TITLE)
    public String title;

    @Schema(description = "Description", example = DTOExamples.ATTACHMENT_TITLE)
    public String description;

    @Schema(description = "File size in bytes")
    public Long size;
}
