package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.query.SortDirection;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinSort {
    UUID twinClassFieldId;
    SortDirection direction;
}
