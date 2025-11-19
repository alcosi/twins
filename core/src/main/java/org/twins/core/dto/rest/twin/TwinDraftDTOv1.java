package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.attachment.AttachmentCreateDTOv1;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinDraftV1")
public class TwinDraftDTOv1 extends Request {
    @Schema(description = "class Id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID classId;

    @Schema(description = "head twin id, if selected class had to be linked to some head twin", example = DTOExamples.HEAD_TWIN_ID)
    public UUID headTwinId;

    @Schema(description = "name", example = "Oak")
    public String name;

    @Schema(description = "assigner user id", example = DTOExamples.USER_ID)
    public UUID assignerUserId;

    @Schema(description = "description", example = "The biggest tree")
    public String description;

    @Schema(description = "fields")
    public Map<String, String> fields;

    @Schema(description = "attachments")
    public List<AttachmentCreateDTOv1> attachments;

    @Schema(description = "links list")
    public List<TwinLinkAddDTOv1> links;

    @Schema(description = "tags list")
    public TwinTagAddDTOv1 tags;

    @Schema(description = "external id")
    public String externalId;

    @Schema(description = "is sketch being created")
    public Boolean isSketch;

    @Schema(description = "field attributes")
    public List<TwinFieldAttributeCreateDTOv1> fieldAttributes;

    public TwinDraftDTOv1 putFieldsItem(String key, String item) {
        if (this.fields == null) this.fields = new HashMap<>();
        this.fields.put(key, item);
        return this;
    }

    public TwinDraftDTOv1 addAttachmentsItem(AttachmentCreateDTOv1 item) {
        this.attachments = CollectionUtils.safeAdd(this.attachments, item);
        return this;
    }

    public TwinDraftDTOv1 addLinksItem(TwinLinkAddDTOv1 item) {
        this.links = CollectionUtils.safeAdd(this.links, item);
        return this;
    }

}


