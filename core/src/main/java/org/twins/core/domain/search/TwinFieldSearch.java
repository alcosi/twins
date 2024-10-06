package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.FieldTyper;

@Data
@Accessors(chain = true)
public abstract class TwinFieldSearch {

    private TwinClassFieldEntity twinClassFieldEntity;
    private FieldTyper<?, ?, ?, TwinFieldSearch> fieldTyper;

}
