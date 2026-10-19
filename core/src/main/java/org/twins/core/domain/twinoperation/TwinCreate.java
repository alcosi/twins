package org.twins.core.domain.twinoperation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.twinlink.TwinLinkCreate;
import org.twins.core.enums.twin.TwinCreateStrategy;

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

    /** Convenience for factory fillers and other producers of plain entities. */
    public TwinCreate addLink(TwinLinkEntity link) {
        TwinLinkCreate linkCreate = new TwinLinkCreate();
        linkCreate.setTwinLink(link);
        return addLink(linkCreate);
    }

    /** Entity view over {@link #linksCreateList} for consumers working with plain entities (fillers, lookupers). */
    public List<TwinLinkEntity> getLinksEntityList() {
        return linksCreateList == null ? null : linksCreateList.stream().map(TwinLinkCreate::getTwinLink).toList();
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
