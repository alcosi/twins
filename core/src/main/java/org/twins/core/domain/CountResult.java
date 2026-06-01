package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CountResult<E> {
    private E entity;
    private Long count;
}
