package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.attachment.AttachmentCudDTOv1;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;
import org.twins.core.dto.rest.link.TwinLinkUpdateDTOv1;

import java.util.*;

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

    public TwinUpdateDTOv1 putFieldsItem(String key, String item) {
        if (this.fields == null) this.fields = new HashMap<>();
        this.fields.put(key, item);
        return this;
    }

    public TwinUpdateDTOv1 addTwinLinksAddItem(TwinLinkAddDTOv1 item) {
        CollectionUtils.safeAdd(twinLinksAdd, item);
        return this;
    }

    public TwinUpdateDTOv1 addTwinLinksDeleteItem(UUID item) {
        CollectionUtils.safeAdd(twinLinksDelete, item);
        return this;
    }

    public TwinUpdateDTOv1 addTwinLinksUpdateItem(TwinLinkUpdateDTOv1 item) {
        CollectionUtils.safeAdd(twinLinksUpdate, item);
        return this;
    }
}
