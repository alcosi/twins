package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinLinkEntity;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueLink extends FieldValue {
    private boolean forwardLink;
    private List<TwinLinkEntity> twinLinks = new ArrayList<>();

    public FieldValueLink add(TwinLinkEntity twinLinkEntity) {
        twinLinks.add(twinLinkEntity);
        return this;
    }
}
