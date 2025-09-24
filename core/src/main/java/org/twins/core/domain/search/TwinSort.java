package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.query.SortDirection;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinSort {

    UUID twinClassFieldId;
    //TODO for collection load
    TwinClassFieldEntity twinClassField;
    SortDirection direction;
}
