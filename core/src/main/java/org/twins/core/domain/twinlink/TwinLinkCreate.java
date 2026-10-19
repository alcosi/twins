package org.twins.core.domain.twinlink;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinLinkEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Domain object for twin_link creation — the create-path sibling of {@link TwinLinkUpdate}, both carrying
 * the shared {@link TwinLinkSave} fields. Produced by TwinLinkAddRestDTOReverseMapper /
 * TwinLinkAddTemporalRestDTOReverseMapper, carried natively by
 * {@link org.twins.core.domain.twinoperation.TwinCreate#getLinksCreateList()} and
 * {@link TwinLinkCUD#getCreateList()}, consumed by {@link org.twins.core.service.twinlink.TwinLinkService#addLinks}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinLinkCreate extends TwinLinkSave {

    /** Wraps plain entities into composition objects (no relation twin fields); null-safe — null stays null. */
    public static List<TwinLinkCreate> wrapAll(Collection<TwinLinkEntity> twinLinks) {
        if (twinLinks == null)
            return null;
        List<TwinLinkCreate> result = new ArrayList<>(twinLinks.size());
        for (TwinLinkEntity twinLink : twinLinks) {
            TwinLinkCreate linkCreate = new TwinLinkCreate();
            linkCreate.setTwinLink(twinLink);
            result.add(linkCreate);
        }
        return result;
    }
}
