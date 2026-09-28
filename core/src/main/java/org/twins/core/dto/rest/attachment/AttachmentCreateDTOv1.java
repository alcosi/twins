package org.twins.core.dto.rest.attachment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.file.FileData;
import org.twins.core.dto.rest.DTOExamples;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "AttachmentCreateV1")
public class AttachmentCreateDTOv1 {
    @Schema(description = "twin id", example = DTOExamples.TWIN_ID)
    public UUID twinId;

    @NotBlank
    @Schema(description = "External storage link. Use multipart scheme to send file in the same multipart request. Example : multipart://file[0], where file[0] - multipart filed name", example = DTOExamples.ATTACHMENT_STORAGE_LINK)
    public String storageLink;

    @Schema(description = "External storage links map by key", example = DTOExamples.ATTACHMENT_STORAGE_LINKS_MAP)
    public Map<String, String> modifications;

    @Schema(description = "External id", example = DTOExamples.ATTACHMENT_EXTERNAL_ID)
    public String externalId;

    @Schema(description = "Title", example = DTOExamples.ATTACHMENT_TITLE)
    public String title;

    @Schema(description = "Description", example = DTOExamples.ATTACHMENT_TITLE)
    public String description;

    @Schema(description = "File size in bytes", example = DTOExamples.INTEGER)
    public Long size;

    @Schema(description = "Order", example = DTOExamples.INTEGER)
    public Integer order;

    @Schema(description = "link to the field to which attachment was added (if any)")
    public UUID twinClassFieldId;

    @Schema(description = "link to the comment to which attachment was added (if any)")
    public UUID commentId;

    @Schema(hidden = true)
    @JsonIgnore
    public boolean isExternalLink;

    @Schema(hidden = true)
    @JsonIgnore
    public FileData domainFile;

    @Schema(hidden = true)
    @JsonIgnore
    public boolean fileChanged = false;

    public AttachmentCreateDTOv1 putModificationsItem(String key, String item) {
        if (this.modifications == null) this.modifications = new HashMap<>();
        this.modifications.put(key, item);
        return this;
    }
}
