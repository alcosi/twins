package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.attachment.AttachmentCudDTOv1;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;
import org.twins.core.dto.rest.link.TwinLinkUpdateDTOv1;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinUpdateV1")
public class TwinUpdateDTOv1 extends Request {
    @Schema(description = "Head twin id, if selected class had to be linked to some head twin", example = DTOExamples.HEAD_TWIN_ID)
    public UUID headTwinId;

    @Schema(description = "name", example = "Oak")
    public String name;

    @Schema(description = "assigner user id", example = DTOExamples.USER_ID)
    public UUID assignerUserId;

    @Schema(description = "external id")
    public String externalId;

    @Schema(description = "description", example = "The biggest tree")
    public String description;

    @Schema(description = "fields")
    public Map<String, String> fields;

    @Schema(description = "Attachments add/update/delete operations")
    public AttachmentCudDTOv1 attachments;

    @Schema(description = "TwinLinks for adding")
    public List<TwinLinkAddDTOv1> twinLinksAdd;

    @Schema(description = "TwinLinks id list for deleting")
    public Set<UUID> twinLinksDelete;

    @Schema(description = "TwinLinks for updating")
    public List<TwinLinkUpdateDTOv1> twinLinksUpdate;

    @Schema(description = "TwinTags for updating")
    public TwinTagManageDTOv1 tagsUpdate;

    @JsonIgnore
    public UUID twinId;
}
