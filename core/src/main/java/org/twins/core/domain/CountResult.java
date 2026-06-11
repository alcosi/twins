package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@Accessors(chain = true)
public class CountResult<E, GF> {
    private E entity;
    private Long count;
    private Set<GF> groupFields;
}
