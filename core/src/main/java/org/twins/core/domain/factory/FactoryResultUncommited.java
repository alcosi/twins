package org.twins.core.domain.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.domain.TwinOperation;

import java.util.List;

@Data
@Accessors(chain = true)
public class FactoryResultUncommited {
    List<TwinOperation> operations;
}
