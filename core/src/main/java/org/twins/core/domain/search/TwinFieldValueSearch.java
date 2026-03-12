package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.featurer.fieldtyper.FieldTyper;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public abstract class TwinFieldValueSearch extends TwinFieldSearch {
    private FieldTyper<?, ?, ?, TwinFieldValueSearch> fieldTyper;
}

