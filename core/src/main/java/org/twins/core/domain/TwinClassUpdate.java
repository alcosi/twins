package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassEntity;

@Data
@Accessors(chain = true)
public class TwinClassUpdate {
    private TwinClassEntity updateTwinClassEntity;
    private TwinClassEntity dbTwinClassEntity;
    private ReplaceOperation markersReplace;
    private ReplaceOperation tagsReplace;
}
