package org.twins.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentsCount {
    private Integer direct;
    private Integer fromTransitions;
    private Integer fromComments;
    private Integer fromFields;

    public Integer getAll() {
        return ObjectUtils.defaultIfNull(direct,0) +
                ObjectUtils.defaultIfNull(fromTransitions,0) +
                ObjectUtils.defaultIfNull(fromComments,0) +
                ObjectUtils.defaultIfNull(fromFields,0);
    }
}
