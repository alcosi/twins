package org.twins.core.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AttachmentsCount {
    private Integer direct;
    private Integer fromTransitions;
    private Integer fromComments;
    private Integer fromFields;

    public AttachmentsCount(Integer direct, Integer fromTransitions, Integer fromComments, Integer fromFields) {
        this.direct = direct;
        this.fromTransitions = fromTransitions;
        this.fromComments = fromComments;
        this.fromFields = fromFields;
    }

    public Integer getAll() {
        if (direct == null || fromTransitions == null || fromComments == null || fromFields == null)
            return null;
        return direct + fromTransitions + fromComments + fromFields;
    }
}
