package org.twins.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinLinkEntity;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinCreate extends TwinOperation {
    private List<TwinAttachmentEntity> attachmentEntityList;
    private List<TwinLinkEntity> linksEntityList;

    @Override
    public UUID nullifyUUID() {
        return null;
    }
}
