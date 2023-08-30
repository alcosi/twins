package org.twins.core.mappers.rest;

import org.cambium.common.exception.ServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class RestListDTOMapper<T, S> implements RestDTOMapper<T, S> {
    public List<S> convertList(List<T> srcList) throws Exception {
        List<S> ret = new ArrayList<>();
        for (T src : srcList) {
            ret.add(this.convert(src));
        }
        return ret;
    }
}
