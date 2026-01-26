package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dto.rest.attachment.AttachmentCreateDTOv1;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;

import java.util.HashMap;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinCreateRqV2")
public class TwinCreateRqDTOv2 extends TwinDraftDTOv1 {
    @Override
    public TwinCreateRqDTOv2 putFieldsItem(String key, String item) {
        if (this.fields == null) this.fields = new HashMap<>();
        this.fields.put(key, item);
        return this;
    }

    @Override
    public TwinCreateRqDTOv2 addAttachmentsItem(AttachmentCreateDTOv1 item) {
        this.attachments = CollectionUtils.safeAdd(this.attachments, item);
        return this;
    }

    @Override
    public TwinCreateRqDTOv2 addLinksItem(TwinLinkAddDTOv1 item) {
        this.links = CollectionUtils.safeAdd(this.links, item);
        return this;
    }
}
