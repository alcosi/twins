package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.link.LinkEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class LinkUpdate extends LinkEntity {
    private EntityRelinkOperation srcTwinClassUpdate;
    private EntityRelinkOperation dstTwinClassUpdate;

    @Override
    public LinkUpdate setId(UUID id) {
        super.setId(id);
        return this;
    }
}
