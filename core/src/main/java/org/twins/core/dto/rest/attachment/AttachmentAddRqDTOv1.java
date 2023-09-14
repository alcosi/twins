package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "AttachmentAddRqV1")
public class AttachmentAddRqDTOv1 extends Request {
    @Schema(description = "External storage link", example = DTOExamples.ATTACHMENT_STORAGE_LINK)
    public String storageLink;
}
