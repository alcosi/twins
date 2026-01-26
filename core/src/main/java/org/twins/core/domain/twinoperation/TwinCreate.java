package org.twins.core.domain.twinoperation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
import org.twins.core.dao.twin.TwinLinkEntity;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinCreate extends TwinSave {
    private List<TwinAttachmentEntity> attachmentEntityList;
    private List<TwinLinkEntity> linksEntityList;
    private List<TwinFieldAttributeEntity> twinFieldAttributeEntityList;
    private boolean checkCreatePermission = false;
    private boolean sketchMode = false;

    public TwinCreate addLink(TwinLinkEntity link) {
        linksEntityList = CollectionUtils.safeAdd(linksEntityList, link);
        return this;
    }

    public TwinCreate addAttachment(TwinAttachmentEntity attachment) {
        attachmentEntityList = CollectionUtils.safeAdd(attachmentEntityList, attachment);
        return this;
    }
    @Override
    public UUID nullifyUUID() {
        return null;
    }
}
