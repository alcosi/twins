package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.FieldTyper;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class TwinFieldRangeSearch {
    private LocalDateTime date;
    private TwinClassFieldEntity fromTwinClassFieldEntity;
    private TwinClassFieldEntity toTwinClassFieldEntity;
    private boolean includeFrom;
    private boolean includeTo;
}
