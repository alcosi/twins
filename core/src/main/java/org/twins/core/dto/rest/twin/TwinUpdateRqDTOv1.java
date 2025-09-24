package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;
import org.twins.core.dto.rest.link.TwinLinkUpdateDTOv1;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinUpdateRqV1")
public class TwinUpdateRqDTOv1 extends TwinUpdateDTOv1 {
    @Schema
    public String comment;

    @Override
    public TwinUpdateRqDTOv1 putFieldsItem(String key, String item) {
        if (this.fields == null) this.fields = new HashMap<>();
        this.fields.put(key, item);
        return this;
    }

    @Override
    public TwinUpdateRqDTOv1 addTwinLinksAddItem(TwinLinkAddDTOv1 item) {
        CollectionUtils.safeAdd(twinLinksAdd, item);
        return this;
    }

    @Override
    public TwinUpdateRqDTOv1 addTwinLinksDeleteItem(UUID item) {
        CollectionUtils.safeAdd(twinLinksDelete, item);
        return this;
    }

    @Override
    public TwinUpdateRqDTOv1 addTwinLinksUpdateItem(TwinLinkUpdateDTOv1 item) {
        CollectionUtils.safeAdd(twinLinksUpdate, item);
        return this;
    }
}
