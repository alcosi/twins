package org.twins.core.domain.twinoperation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.twinlink.TwinLinkCreate;
import org.twins.core.enums.twin.TwinCreateStrategy;
import org.twins.core.service.link.LinkService;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinCreate extends TwinSave {
    private List<TwinAttachmentEntity> attachmentEntityList;
    /** Links to create — composition objects (entity + creation-only relation twin fields). */
    private List<TwinLinkCreate> linksCreateList;
    private List<TwinFieldAttributeEntity> twinFieldAttributeEntityList;
    private boolean checkCreatePermission = false;
    private Boolean sketchMode; // this flag will be set after processing createStrategy
    private TwinCreateStrategy createStrategy = TwinCreateStrategy.STRICT;

    public TwinCreate addLink(TwinLinkCreate linkCreate) {
        linksCreateList = CollectionUtils.safeAdd(linksCreateList, linkCreate);
        return this;
    }

    /**
     * Convenience for factory fillers and other producers of already-oriented rows: derives the declarative
     * intent from the row (the anchor is this create's twin — src for forward rows, dst for backward rows).
     */
    public TwinCreate addLink(TwinLinkEntity link) {
        boolean forward = link.getSrcTwinId() != null && link.getSrcTwinId().equals(twinEntity.getId());
        TwinEntity farTwin = forward ? link.getDstTwin() : link.getSrcTwin();
        UUID farTwinId = forward ? link.getDstTwinId() : link.getSrcTwinId();
        TwinLinkCreate linkCreate = new TwinLinkCreate()
                .setTwin(twinEntity)
                .setLink(link.getLink())
                .setLinkDirection(forward ? LinkService.LinkDirection.forward : LinkService.LinkDirection.backward)
                .setUniqForSrcRelink(link.isUniqForSrcRelink());
        if (farTwin != null)
            linkCreate.addToTwin(farTwin);
        else if (farTwinId != null)
            linkCreate.addToTwin(new TwinEntity().setId(farTwinId)); // id stub — prepare loads the real twin
        return addLink(linkCreate);
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
