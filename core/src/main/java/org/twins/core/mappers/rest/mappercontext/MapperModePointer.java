/*
 * Copyright (c)
 * created:2021 - 5 - 13
 * by Yan Tayanouski
 * ESAS Ltd. La propriété, c'est le vol!
 */

package org.twins.core.mappers.rest.mappercontext;

public interface MapperModePointer<T extends MapperMode> extends MapperMode{
    T point();
}
